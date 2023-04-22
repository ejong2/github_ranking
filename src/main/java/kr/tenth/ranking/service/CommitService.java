package kr.tenth.ranking.service;

import com.fasterxml.jackson.databind.JsonNode;
import kr.tenth.ranking.domain.CommitInfo;
import kr.tenth.ranking.repository.CommitInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommitService {
    private final CommitInfoRepository commitInfoRepository;
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private final RestTemplate restTemplate;
    @Value("${github.token}")
    private String githubToken;

    public List<CommitInfo> getUserCommits(String username, LocalDate fromDate, LocalDate toDate) {
        List<CommitInfo> commitInfos = getUserCommitsFromOwnRepos(username, fromDate, toDate);
        commitInfos.addAll(getUserCommitsFromOtherRepos(username, fromDate, toDate));
        return commitInfos;
    }

    private List<CommitInfo> getUserCommitsFromOwnRepos(String username, LocalDate fromDate, LocalDate toDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = GITHUB_API_BASE_URL + "/search/commits?q=author:" + username + "+sort:author-date";
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        List<CommitInfo> commitInfos = new ArrayList<>();
        JsonNode items = response.getBody().get("items");
        for (JsonNode item : items) {
            String message = item.get("commit").get("message").asText();
            String repoName = item.get("repository").get("full_name").asText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            LocalDateTime commitDate = LocalDateTime.parse(item.get("commit").get("author").get("date").asText(), formatter);
            ZoneId serverZone = ZoneId.systemDefault();
            ZonedDateTime commitDateZoned = ZonedDateTime.of(commitDate, serverZone);
            LocalDateTime commitDateInServerZone = commitDateZoned.toLocalDateTime();
            CommitInfo commitInfo = new CommitInfo();
            commitInfo.setUsername(username);
            commitInfo.setCommitMessage(message);
            commitInfo.setRepoName(repoName);
            commitInfo.setCommitDate(commitDateInServerZone);
            if (isDateInRange(commitInfo.getCommitDate().toLocalDate(), fromDate, toDate)) {
                commitInfos.add(commitInfo);
                commitInfoRepository.save(commitInfo);
            }
        }

        return commitInfos;
    }
    private List<CommitInfo> getUserCommitsFromOtherRepos(String username, LocalDate fromDate, LocalDate toDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<CommitInfo> commitInfos = new ArrayList<>();

        String userReposUrl = GITHUB_API_BASE_URL + "/users/" + username + "/repos";
        ResponseEntity<JsonNode[]> userReposResponse = restTemplate.exchange(userReposUrl, HttpMethod.GET, entity, JsonNode[].class);
        JsonNode[] userRepos = userReposResponse.getBody();

        for (JsonNode repo : userRepos) {
            String repoName = repo.get("full_name").asText();
            String commitsUrl = GITHUB_API_BASE_URL + "/repos/" + repoName + "/commits?since=" + fromDate + "&until=" + toDate;

            ResponseEntity<JsonNode[]> commitsResponse = restTemplate.exchange(commitsUrl, HttpMethod.GET, entity, JsonNode[].class);
            JsonNode[] commits = commitsResponse.getBody();

            for (JsonNode commit : commits) {
                String message = commit.get("commit").get("message").asText();
                String authorName = commit.get("commit").get("author").get("name").asText();
                LocalDateTime commitDate = LocalDateTime.parse(commit.get("commit").get("author").get("date").asText());
                CommitInfo commitInfo = new CommitInfo();
                commitInfo.setUsername(authorName);
                commitInfo.setCommitMessage(message);
                commitInfo.setRepoName(repoName);
                commitInfo.setCommitDate(commitDate);
                if (isDateInRange(commitInfo.getCommitDate().toLocalDate(), fromDate, toDate)) {
                    commitInfos.add(commitInfo);
                    commitInfoRepository.save(commitInfo);
                }
            }
        }
        return commitInfos;
    }
    private boolean isDateInRange(LocalDate targetDate, LocalDate fromDate, LocalDate toDate) {
        return (fromDate == null || !targetDate.isBefore(fromDate)) && (toDate == null || !targetDate.isAfter(toDate));
    }
}


//@Service
//@RequiredArgsConstructor
//public class CommitService {
//    private final CommitInfoRepository commitInfoRepository;
//    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
//    private final RestTemplate restTemplate;
//    @Value("${github.token}")
//    private String githubToken;
//
//    public List<CommitInfo> getUserCommits(String username, LocalDate fromDate, LocalDate toDate) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(githubToken);
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        String url = GITHUB_API_BASE_URL + "/search/commits?q=author:" + username + "+sort:author-date";
//        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
//
//        List<CommitInfo> commitInfos = new ArrayList<>();
//        JsonNode items = response.getBody().get("items");
//        for (JsonNode item : items) {
//            String message = item.get("commit").get("message").asText();
//            String repoName = item.get("repository").get("full_name").asText();
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
//            LocalDateTime commitDate = LocalDateTime.parse(item.get("commit").get("author").get("date").asText(), formatter);
//            ZoneId serverZone = ZoneId.systemDefault();
//            ZonedDateTime commitDateZoned = ZonedDateTime.of(commitDate, serverZone);
//            LocalDateTime commitDateInServerZone = commitDateZoned.toLocalDateTime();
//            CommitInfo commitInfo = new CommitInfo();
//            commitInfo.setUsername(username);
//            commitInfo.setCommitMessage(message);
//            commitInfo.setRepoName(repoName);
//            commitInfo.setCommitDate(commitDateInServerZone);
//            if (isDateInRange(commitInfo.getCommitDate().toLocalDate(), fromDate, toDate)) {
//                commitInfos.add(commitInfo);
//                commitInfoRepository.save(commitInfo);
//            }
//        }
//
//        return commitInfos;
//    }
//
//    private boolean isDateInRange(LocalDate targetDate, LocalDate fromDate, LocalDate toDate) {
//        return (fromDate == null || !targetDate.isBefore(fromDate)) && (toDate == null || !targetDate.isAfter(toDate));
//    }
//}
