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
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private final RestTemplate restTemplate;
    private final CommitInfoRepository commitInfoRepository;

    @Value("${github.token}")
    private String githubToken;

    public List<CommitInfo> getUserCommits(String username, LocalDate fromDate, LocalDate toDate) throws Exception {
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

    private List<CommitInfo> getUserCommitsFromOtherRepos(String username, LocalDate fromDate, LocalDate toDate) throws Exception {
        List<CommitInfo> commitInfos = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 사용자가 속한 조직 가져오기
        String userOrgsUrl = GITHUB_API_BASE_URL + "/users/" + username + "/orgs";
        ResponseEntity<JsonNode[]> userOrgsResponse = restTemplate.exchange(userOrgsUrl, HttpMethod.GET, entity, JsonNode[].class);
        JsonNode[] userOrgs = userOrgsResponse.getBody();

        if (userOrgs.length == 0) {
            throw new Exception("사용자가 속한 조직이 없습니다.");
        }

        // 조직의 저장소 목록 가져오기
        for (JsonNode org : userOrgs) {
            String orgName = org.get("login").asText();
            String orgReposUrl = GITHUB_API_BASE_URL + "/orgs/" + orgName + "/repos";
            ResponseEntity<JsonNode[]> orgReposResponse = restTemplate.exchange(orgReposUrl, HttpMethod.GET, entity, JsonNode[].class);
            JsonNode[] orgRepos = orgReposResponse.getBody();

            if (orgRepos.length == 0) {
                throw new Exception("조직의 저장소 목록을 가져올 수 없습니다.");
            }

            // 각 저장소에서 커밋 목록 가져오기
            for (JsonNode repo : orgRepos) {
                String repoName = repo.get("full_name").asText();
                String commitsUrl = GITHUB_API_BASE_URL + "/repos/" + repoName + "/commits?since=" + fromDate + "&until=" + toDate;

                ResponseEntity<JsonNode[]> commitsResponse = restTemplate.exchange(commitsUrl, HttpMethod.GET, entity, JsonNode[].class);
                JsonNode[] commits = commitsResponse.getBody();

                // 커밋 정보 추출 및 저장]
                for (JsonNode commit : commits) {
                    String commitMessage = commit.get("commit").get("message").asText();
                    LocalDateTime commitDate = LocalDateTime.parse(commit.get("commit").get("committer").get("date").asText(), DateTimeFormatter.ISO_DATE_TIME);

                    if (username.equalsIgnoreCase(commit.get("committer").get("login").asText()) && !isCommitInfoExists(username, repoName, commitMessage, commitDate)) {
                        commitInfos.add(new CommitInfo(username, commitMessage, repoName, commitDate));
                        saveCommitInfoToDatabase(username, commitMessage, repoName, commitDate);
                    }
                }
            }
        }

        return commitInfos;
    }
    private void saveCommitInfoToDatabase(String username, String commitMessage, String repoName, LocalDateTime commitDate) {
        CommitInfo commitInfo = new CommitInfo();
        commitInfo.setUsername(username);
        commitInfo.setCommitMessage(commitMessage);
        commitInfo.setRepoName(repoName);
        commitInfo.setCommitDate(commitDate);
        commitInfoRepository.save(commitInfo);
    }

    private boolean isCommitInfoExists(String username, String repoName, String commitMessage, LocalDateTime commitDate) {
        return commitInfoRepository.findByUsernameAndRepoNameAndCommitMessageAndCommitDate(username, repoName, commitMessage, commitDate).isPresent();
    }

    private boolean isDateInRange(LocalDate targetDate, LocalDate fromDate, LocalDate toDate) {
        return (fromDate == null || !targetDate.isBefore(fromDate)) && (toDate == null || !targetDate.isAfter(toDate));
    }
}