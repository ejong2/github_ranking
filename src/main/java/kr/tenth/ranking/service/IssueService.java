package kr.tenth.ranking.service;

import com.fasterxml.jackson.databind.JsonNode;
import kr.tenth.ranking.domain.IssueInfo;
import kr.tenth.ranking.repository.IssueInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueService {
    private final IssueInfoRepository issueInfoRepository;
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private final RestTemplate restTemplate;
    @Value("${github.token}")
    private String githubToken;

    public List<IssueInfo> getUserIssues(String username, LocalDate fromDate, LocalDate toDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = GITHUB_API_BASE_URL + "/search/issues?q=author:" + username + "+type:issue+sort:created";
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        List<IssueInfo> issueInfos = new ArrayList<>();
        JsonNode items = response.getBody().get("items");
        for (JsonNode item : items) {
            String title = item.get("title").asText();
            String repoName = item.get("repository_url").asText().replace("https://api.github.com/repos/", "");
            LocalDateTime issueDate = LocalDateTime.parse(item.get("created_at").asText());
            IssueInfo issueInfo = new IssueInfo();
            issueInfo.setUsername(username);
            issueInfo.setIssueTitle(title);
            issueInfo.setRepoName(repoName);
            issueInfo.setIssueDate(issueDate);
            issueInfos.add(issueInfo);
            issueInfoRepository.save(issueInfo);
        }

        return issueInfos.stream()
                .filter(issueInfo -> isDateInRange(issueInfo.getIssueDate().toLocalDate(), fromDate, toDate))
                .collect(Collectors.toList());
    }

    private boolean isDateInRange(LocalDate targetDate, LocalDate fromDate, LocalDate toDate) {
        return (fromDate == null || !targetDate.isBefore(fromDate)) && (toDate == null || !targetDate.isAfter(toDate));
    }
}
