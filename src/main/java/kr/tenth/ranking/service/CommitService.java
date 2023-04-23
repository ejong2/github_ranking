package kr.tenth.ranking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.tenth.ranking.domain.CommitInfo;
import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.repository.CommitInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class CommitService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CommitInfoRepository commitInfoRepository;

    public List<CommitInfo> getCommits(User user, LocalDate fromDate, LocalDate toDate) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.github.com/user/repos",
                HttpMethod.GET,
                entity,
                String.class);

        JsonNode repositories = objectMapper.readTree(response.getBody());

        List<CommitInfo> commits = new ArrayList<>();

        for (JsonNode repo : repositories) {
            String repoName = repo.get("full_name").asText();
            boolean isMemberRepo = repo.get("permissions").get("pull").asBoolean();
            if (!isMemberRepo) {
                continue;
            }
            List<CommitInfo> repoCommits = getCommitsFromRepo(user, repoName, fromDate, toDate);
            commits.addAll(repoCommits);
        }

//        commitInfoRepository.saveAll(commits);

        return commits;
    }

    private List<CommitInfo> getCommitsFromRepo(User user, String repoName, LocalDate fromDate, LocalDate toDate) throws IOException {
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
        List<CommitInfo> commitInfos = new ArrayList<>();

        for (JsonNode jsonCommit : jsonCommits) {
            JsonNode commit = jsonCommit.get("commit");
            JsonNode author = commit.get("author");
            JsonNode committer = commit.get("committer");

            String message = commit.get("message").asText();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime date = LocalDateTime.parse(committer.get("date").asText(), formatter).atZone(githubTimeZone).withZoneSameInstant(kst).toLocalDateTime();

            CommitInfo commitInfo = new CommitInfo(user, message, repoName, date);
            commitInfos.add(commitInfo);
        }
        return commitInfos;
    }
}