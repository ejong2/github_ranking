package kr.tenth.ranking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.tenth.ranking.domain.CommitInfo;
import kr.tenth.ranking.domain.RepositoryInfo;
import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.dto.CommitCountDto;
import kr.tenth.ranking.dto.CommitInfoDto;
import kr.tenth.ranking.dto.SimpleCommitInfoDto;
import kr.tenth.ranking.repository.CommitInfoRepository;
import kr.tenth.ranking.repository.UserRepository;
import kr.tenth.ranking.util.DateRangeUtils;
import kr.tenth.ranking.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static kr.tenth.ranking.dto.CommitInfoDto.convertToDto;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GithubCommitService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CommitInfoRepository commitInfoRepository;
    private final UserRepository userRepository;
    private final GitHubRepositoryService gitHubRepositoryService;

    @Scheduled(fixedRate = 60000)
    public void updateAllUsersCommits() {
        // 모든 사용자 정보를 가져옵니다.
        List<User> users = userRepository.findAll();
        LocalDate fromDate;
        LocalDate toDate = LocalDate.now();

        // 각 사용자의 커밋 정보를 업데이트합니다.
        for (User user : users) {
            if (user.getLastSavedCommitDate() == null) {
                fromDate = user.getAccountCreatedDate();
            } else {
                fromDate = user.getLastSavedCommitDate().toLocalDate();
            }
            try {
                updateUserCommits(user, fromDate, toDate);
            } catch (IOException e) {
                // 에러 발생 시, 로그를 남기고 다음 사용자의 정보 업데이트를 진행합니다.
                log.error("사용자 커밋 정보 업데이트 중 오류 발생: 사용자 아이디 = {}", user.getId(), e);
            }
        }
    }

    private void updateUserCommits(User user, LocalDate fromDate, LocalDate toDate) throws IOException {
        List<CommitInfoDto> commitInfosDto = getCommits(user, fromDate, toDate);

        for (CommitInfoDto commitInfoDto : commitInfosDto) {
            saveCommit(commitInfoDto);
            // 가장 최근에 저장된 커밋 날짜를 업데이트합니다.
            if (user.getLastSavedCommitDate() == null || user.getLastSavedCommitDate().isBefore(commitInfoDto.getCommitDate())) {
                user.setLastSavedCommitDate(commitInfoDto.getCommitDate());
                userRepository.save(user);
            }
        }
    }

    // 사용자의 모든 저장소에서 fromDate부터 toDate까지의 커밋 정보를 가져오는 메서드
    // 깃허브 API를 사용하여 사용자의 모든 저장소를 조회한 후 각 저장소의 커밋 정보를 가져옵니다.
    public List<CommitInfoDto> getCommits(User user, LocalDate fromDate, LocalDate toDate) throws IOException {
        HttpEntity<String> entity = createHttpEntityWithAccessToken(user.getAccessToken());

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.github.com/user/repos",
                HttpMethod.GET,
                entity,
                String.class);

        JsonNode repositories = objectMapper.readTree(response.getBody());

        List<CommitInfoDto> commitsDto = new ArrayList<>();

        for (JsonNode repo : repositories) {
            String repoName = repo.get("full_name").asText();
            boolean isMemberRepo = repo.get("permissions").get("pull").asBoolean();
            if (!isMemberRepo) {
                continue;
            }
            List<CommitInfoDto> repoCommits = getCommitsFromRepo(user, repoName, fromDate, toDate);
            commitsDto.addAll(repoCommits);
        }

        return commitsDto;
    }

    public List<SimpleCommitInfoDto> getCommitsEntities(User user, LocalDate fromDate, LocalDate toDate) {
        // fromDate가 null인 경우 오늘 날짜로 설정
        if (fromDate == null) {
            fromDate = LocalDate.now();
        }
        LocalDateTime fromDateTime = convertToDateInUtc(fromDate, LocalTime.MIDNIGHT);

        // toDate가 null인 경우 오늘 날짜로 설정
        if (toDate == null) {
            toDate = LocalDate.now();
        }
        LocalDateTime toDateTime = convertToDateInUtc(toDate, LocalTime.of(23, 59, 59));

        List<CommitInfo> commitInfos = commitInfoRepository.findAllByGithubUsernameAndDateRange(user.getGithubUsername(), fromDateTime, toDateTime);
        return commitInfos.stream()
                .map(SimpleCommitInfoDto::convertToSimpleDto)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getCommitCountListByPeriod(List<User> users) {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> commitList = new ArrayList<>();

        for (User user : users) {
            LocalDate firstDayOfWeek = DateRangeUtils.getFirstDayOfWeek(today);
            LocalDate firstDayOfMonth = DateRangeUtils.getFirstDayOfMonth(today);
            LocalDate lastDayOfMonth = DateRangeUtils.getLastDayOfMonth(today);

            int todayCommitCount = getCommitsEntities(user, today, today).size();
            int weeklyCommitCount = getCommitsEntities(user, firstDayOfWeek, today).size();
            int monthlyCommitCount = getCommitsEntities(user, firstDayOfMonth, lastDayOfMonth).size();

            CommitCountDto commitCountDto = CommitCountDto.builder()
                    .githubUsername(user.getGithubUsername())
                    .todayCommitCount(todayCommitCount)
                    .weeklyCommitCount(weeklyCommitCount)
                    .monthlyCommitCount(monthlyCommitCount)
                    .build();

            commitList.add(commitCountDto.toMap());
        }

        return commitList;
    }

    public List<SimpleCommitInfoDto> getAllUsersCommitsEntities(LocalDate fromDate, LocalDate toDate) {
        // fromDate가 null인 경우 오늘 날짜로 설정
        if (fromDate == null) {
            fromDate = LocalDate.now();
        }
        LocalDateTime fromDateTime = convertToDateInUtc(fromDate, LocalTime.MIDNIGHT);

        // toDate가 null인 경우 오늘 날짜로 설정
        if (toDate == null) {
            toDate = LocalDate.now();
        }
        LocalDateTime toDateTime = convertToDateInUtc(toDate, LocalTime.of(23, 59, 59));

        List<CommitInfo> commitInfos = commitInfoRepository.findAllByDateRange(fromDateTime, toDateTime);
        return commitInfos.stream()
                .map(SimpleCommitInfoDto::convertToSimpleDto)
                .collect(Collectors.toList());
    }

    public int getCommitCountByPeriod(User user, String period) {
        LocalDate today = LocalDate.now();
        LocalDate fromDate = null;
        LocalDate toDate = today;

        if ("daily".equals(period)) {
            fromDate = today;
        } else if ("weekly".equals(period)) {
            fromDate = DateRangeUtils.getFirstDayOfWeek(today);
        } else if ("monthly".equals(period)) {
            fromDate = DateRangeUtils.getFirstDayOfMonth(today);
        } else {
            throw new IllegalArgumentException("Invalid period: " + period);
        }

        return getCommitsEntities(user, fromDate, toDate).size();
    }

    // 특정 저장소에서 fromDate부터 toDate까지의 사용자의 커밋 정보를 가져오는 메서드
    private List<CommitInfoDto> getCommitsFromRepo(User user, String repoName, LocalDate fromDate, LocalDate toDate) throws IOException {
        HttpEntity<String> entity = createHttpEntityWithAccessToken(user.getAccessToken());

        ZoneId githubTimeZone = ZoneId.of("Z");
        ZoneId kst = ZoneId.of("Asia/Seoul");
        ZonedDateTime fromZdt = fromDate != null ? fromDate.atStartOfDay(kst).withZoneSameInstant(githubTimeZone) : null;
        ZonedDateTime toZdt = toDate != null ? toDate.atStartOfDay(kst).plusDays(1).withZoneSameInstant(githubTimeZone) : null;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://api.github.com/repos/" + repoName + "/commits")
                .queryParam("since", fromZdt != null ? fromZdt.format(DateTimeFormatter.ISO_DATE_TIME) : null)
                .queryParam("until", toZdt != null ? toZdt.format(DateTimeFormatter.ISO_DATE_TIME) : null);

        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class);

        JsonNode jsonCommits = objectMapper.readTree(response.getBody());
        List<CommitInfoDto> commitInfosDto = new ArrayList<>();

        for (JsonNode jsonCommit : jsonCommits) {
            JsonNode author = jsonCommit.get("author");
            if (author != null) {
                JsonNode login = author.get("login");
                if (login != null && user.getGithubUsername().equalsIgnoreCase(login.asText())) {
                    JsonNode commit = jsonCommit.get("commit");
                    JsonNode committer = commit.get("committer");

                    String message = commit.get("message").asText();
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                    LocalDateTime date = LocalDateTime.parse(committer.get("date").asText(), formatter).atZone(githubTimeZone).withZoneSameInstant(kst).toLocalDateTime().plusHours(9);

                    // 커밋의 SHA 값 가져오기
                    String sha = jsonCommit.get("sha").asText();

                    // 커밋 작성자 및 커미터 정보 가져오기
                    String committerName = committer.get("name").asText();
                    String committerEmail = committer.get("email").asText();

                    // 커밋 URL 가져오기
                    String commitUrl = jsonCommit.get("html_url").asText();

                    // 커밋 통계 정보 가져오기
                    JsonNode stats = jsonCommit.get("stats");
                    int additions = 0;
                    int deletions = 0;
                    int changedFiles = 0;
                    if (stats != null) {
                        additions = stats.get("additions").asInt();
                        deletions = stats.get("deletions").asInt();
                        changedFiles = stats.get("total").asInt();
                    }

                    // 부모 커밋의 SHA 값 가져오기
                    List<String> parentShas = new ArrayList<>();
                    JsonNode parents = jsonCommit.get("parents");
                    if (parents != null) {
                        for (JsonNode parent : parents) {
                            parentShas.add(parent.get("sha").asText());
                        }
                    }

                    // 저장소 정보 가져오기
                    RepositoryInfo repositoryInfo = gitHubRepositoryService.getRepositoryInfo(user, repoName);

                    CommitInfoDto commitInfoDto = convertToDto(new CommitInfo(user, message, repoName, date, sha, committerName, committerEmail, commitUrl, additions, deletions, changedFiles, repositoryInfo));
                    commitInfosDto.add(commitInfoDto);
                }
            }
        }
        return commitInfosDto;
    }
    // 사용자의 커밋 정보를 저장하거나 기존 커밋 정보를 업데이트하는 메서드
    private void saveCommit(CommitInfoDto  commitInfoDto) {
        String truncatedMessage = commitInfoDto.getCommitMessage().substring(0, Math.min(commitInfoDto.getCommitMessage().length(), 50));
        if (commitInfoDto.getCommitMessage().length() > 50) {
            truncatedMessage += "...";
        }
        CommitInfo existingCommit = commitInfoRepository.findByUserAndRepoNameAndCommitDate(commitInfoDto.getUserId(), commitInfoDto.getRepoName(), commitInfoDto.getCommitDate())
                .orElse(null);

        if (existingCommit == null) {
            CommitInfo newCommit = new CommitInfo(commitInfoDto.getUserId(), truncatedMessage, commitInfoDto.getRepoName(), DateTimeUtils.formatWithoutMilliseconds(commitInfoDto.getCommitDate()), commitInfoDto.getSha(), commitInfoDto.getCommitterName(), commitInfoDto.getCommitterEmail(), commitInfoDto.getCommitUrl(), commitInfoDto.getAdditions(), commitInfoDto.getDeletions(), commitInfoDto.getChangedFiles(), commitInfoDto.getRepositoryId());

            commitInfoRepository.save(newCommit);

        } else if (!existingCommit.getCommitMessage().equals(truncatedMessage)) {
            existingCommit.updateCommitMessage(truncatedMessage);
            commitInfoRepository.save(existingCommit);
        }
    }
    private HttpEntity<String> createHttpEntityWithAccessToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(headers);
    }
    public LocalDateTime convertToDateInUtc(LocalDate date, LocalTime time) {
        return date.atTime(time).atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }
}