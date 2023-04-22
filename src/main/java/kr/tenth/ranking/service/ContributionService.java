package kr.tenth.ranking.service;

import com.fasterxml.jackson.databind.JsonNode;
import kr.tenth.ranking.domain.ContributionInfo;
import kr.tenth.ranking.repository.ContributionInfoRepository;
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
public class ContributionService {
    private final ContributionInfoRepository contributionInfoRepository;
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private final RestTemplate restTemplate;
    @Value("${github.token}")
    private String githubToken;

    public List<ContributionInfo> getUserContributions(String username, LocalDate fromDate, LocalDate toDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = GITHUB_API_BASE_URL + "/users/" + username + "/repos";
        ResponseEntity<JsonNode[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode[].class);

        List<ContributionInfo> contributionInfos = new ArrayList<>();
        JsonNode[] repositories = response.getBody();
        for (JsonNode repo : repositories) {
            String repoName = repo.get("full_name").asText();
            int forks = repo.get("forks_count").asInt();
            int watchers = repo.get("watchers_count").asInt();
            int stars = repo.get("stargazers_count").asInt();

            ContributionInfo contributionInfo = new ContributionInfo();
            contributionInfo.setUsername(username);
            contributionInfo.setRepoName(repoName);
            contributionInfo.setForks(forks);
            contributionInfo.setWatchers(watchers);
            contributionInfo.setStars(stars);
//            contributionInfo.setCreatedDate(LocalDateTime.now());
            contributionInfos.add(contributionInfo);
            contributionInfoRepository.save(contributionInfo);
        }
//
//        return contributionInfos.stream()
//                .filter(contributionInfo -> isDateInRange(contributionInfo.getCreatedDate().toLocalDate(), fromDate, toDate))
//                .collect(Collectors.toList());

        return contributionInfos.stream()
                .collect(Collectors.toList());
    }

//    private boolean isDateInRange(LocalDate targetDate, LocalDate fromDate, LocalDate toDate) {
//        return (fromDate == null || !targetDate.isBefore(fromDate)) && (toDate == null || !targetDate.isAfter(toDate));
//    }
}
