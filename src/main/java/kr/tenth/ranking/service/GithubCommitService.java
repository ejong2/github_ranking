package kr.tenth.ranking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.tenth.ranking.domain.CommitInfo;
import kr.tenth.ranking.domain.RepositoryInfo;
import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.dto.CommitInfoDto;
import kr.tenth.ranking.repository.CommitInfoRepository;
import kr.tenth.ranking.repository.UserRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GithubCommitService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CommitInfoRepository commitInfoRepository;
    private final UserRepository userRepository;
    private final GitHubRepositoryService gitHubRepositoryService;

    // 모든 사용자의 커밋 정보를 10분마다 업데이트하는 스케줄링 메서드
    // 데이터베이스에 저장된 모든 사용자의 깃허브 커밋 정보를 조회하여 업데이트합니다.
    @Scheduled(fixedRate = 60000) // 10분마다 실행 - 현재 테스트용 6초 설정
    public void updateAllUsersCommits() throws IOException {
        List<User> users = userRepository.findAll();

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        for (User user : users) {
            LocalDate accountCreatedDate = user.getAccountCreatedDate(); // 계정 생성 시점 가져오기
            LocalDate lastSavedCommitDate = user.getLastSavedCommitDate() != null ? user.getLastSavedCommitDate().toLocalDate() : null;
            LocalDate fromDate = user.getLastSavedCommitDate() == null ? accountCreatedDate : lastSavedCommitDate.plusDays(1);
            List<CommitInfoDto> commitInfosDto = getCommits(user, fromDate, today);

            for (CommitInfoDto commitInfoDto : commitInfosDto) {
                saveCommit(commitInfoDto);
                // 가장 최근에 저장된 커밋 날짜를 업데이트합니다.
                if (user.getLastSavedCommitDate() == null || user.getLastSavedCommitDate().isBefore(commitInfoDto.getCommitDate())) {
                    user.setLastSavedCommitDate(commitInfoDto.getCommitDate());
                    userRepository.save(user);
                }
            }
        }
    }

    // 사용자의 모든 저장소에서 fromDate부터 toDate까지의 커밋 정보를 가져오는 메서드
    // 깃허브 API를 사용하여 사용자의 모든 저장소를 조회한 후 각 저장소의 커밋 정보를 가져옵니다.
    public List<CommitInfoDto> getCommits(User user, LocalDate fromDate, LocalDate toDate) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

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

    public List<CommitInfo> getCommitsEntities(User user, LocalDate fromDate, LocalDate toDate) throws IOException {
        List<CommitInfoDto> commitInfosDto = getCommits(user, fromDate, toDate);
        return commitInfosDto.stream()
                .map(CommitInfo::convertToEntity)
                .collect(Collectors.toList());
    }

    // 특정 저장소에서 fromDate부터 toDate까지의 사용자의 커밋 정보를 가져오는 메서드
    private List<CommitInfoDto> getCommitsFromRepo(User user, String repoName, LocalDate fromDate, LocalDate toDate) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

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
    public CommitInfoDto convertToDto(CommitInfo commitInfo) {
        return CommitInfoDto.builder()
                .userId(commitInfo.getUser())
                .repositoryId(commitInfo.getRepository())
                .commitMessage(commitInfo.getCommitMessage())
                .repoName(commitInfo.getRepoName())
                .commitDate(commitInfo.getCommitDate())
                .sha(commitInfo.getSha())
                .committerName(commitInfo.getCommitterName())
                .committerEmail(commitInfo.getCommitterEmail())
                .commitUrl(commitInfo.getCommitUrl())
                .additions(commitInfo.getAdditions())
                .deletions(commitInfo.getDeletions())
                .changedFiles(commitInfo.getChangedFiles())
                .build();
    }
}