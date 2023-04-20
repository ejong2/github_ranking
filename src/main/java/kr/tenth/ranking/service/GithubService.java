package kr.tenth.ranking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GithubService {
    private final UserRepository userRepository;
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private final RestTemplate restTemplate;
    @Value("${github.token}")
    private String githubToken;

    public User saveUser(String username) {
        return userRepository.save(new User(username, 0, 0, 0));
    }

    public User saveUserStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
        int commitCount = getCommitCount(username);
        int codeReviewCount = getCodeReviewCount(username);
        int pullRequestCount = getPullRequestCount(username);

        user.setCommitCount(commitCount);
        user.setCodeReviewCount(codeReviewCount);
        user.setPullRequestCount(pullRequestCount);

        return userRepository.save(user);
    }

    public User getUserStats(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
    }
    public HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        return headers;
    }

    public ResponseEntity<String> getUserInfo(String username) {
        String url = GITHUB_API_BASE_URL + "/users/" + username;
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    public int getTodayCommitCount(String username) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedToday = today.format(formatter);

        String publicUrl = GITHUB_API_BASE_URL + "/search/commits?q=author:" + username + "+committer-date:" + formattedToday + "+is:public";
        String privateUrl = GITHUB_API_BASE_URL + "/search/commits?q=author:" + username + "+committer-date:" + formattedToday + "+is:private";
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setAccept(Collections.singletonList(MediaType.valueOf("application/vnd.github.cloak-preview")));
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

        // 공개 레포지토리 커밋 수 가져오기
        ResponseEntity<String> publicResponse = restTemplate.exchange(publicUrl, HttpMethod.GET, requestEntity, String.class);
        JsonNode publicRoot = null;
        try {
            publicRoot = new ObjectMapper().readTree(publicResponse.getBody());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        int publicCommitCount = publicRoot.path("total_count").asInt();

        // 비공개 레포지토리 커밋 수 가져오기
        ResponseEntity<String> privateResponse = restTemplate.exchange(privateUrl, HttpMethod.GET, requestEntity, String.class);
        JsonNode privateRoot = null;
        try {
            privateRoot = new ObjectMapper().readTree(privateResponse.getBody());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        int privateCommitCount = privateRoot.path("total_count").asInt();

        // 공개 및 비공개 레포지토리 커밋 수 합계
        return publicCommitCount + privateCommitCount;
    }

    private int getCommitCount(String username) {
        // 예시로, 해당 사용자의 모든 커밋 수를 가져오는 것으로 가정합니다.
        String url = GITHUB_API_BASE_URL + "/search/commits?q=author:" + username;
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        JsonNode root = null;
        try {
            root = new ObjectMapper().readTree(response.getBody());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return root.path("total_count").asInt();
    }

    private int getCodeReviewCount(String username) {
        String url = GITHUB_API_BASE_URL + "/search/issues?q=type:pr+reviewed-by:" + username;
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        JsonNode root = null;
        try {
            root = new ObjectMapper().readTree(response.getBody());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return root.path("total_count").asInt();
    }

    private int getPullRequestCount(String username) {
        String url = GITHUB_API_BASE_URL + "/search/issues?q=type:pr+author:" + username;
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        JsonNode root = null;
        try {
            root = new ObjectMapper().readTree(response.getBody());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return root.path("total_count").asInt();
    }
}
