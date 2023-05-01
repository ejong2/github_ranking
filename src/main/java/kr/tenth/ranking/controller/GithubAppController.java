package kr.tenth.ranking.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.tenth.ranking.config.GithubConfig;
import kr.tenth.ranking.domain.Organization;
import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.repository.UserRepository;
import kr.tenth.ranking.service.GithubUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// 깃허브 애플리케이션 컨트롤러
// 깃허브 로그인 및 콜백 처리를 담당합니다.
@Controller
@RequiredArgsConstructor
public class GithubAppController {

    private final GithubConfig gitHubConfig;
    private final GithubUserService githubUserService;
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";

    // 인덱스 페이지를 반환하는 메서드
    @GetMapping
    public String index() {
        return "index";
    }

//     깃허브 콜백 처리를 위한 메서드
//     깃허브에서 전달받은 코드를 사용해 액세스 토큰을 얻고, 사용자 정보를 저장합니다.
    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code, Model model) {
        model.addAttribute("code", code);
        return "callback";
    }

    // 로그인 성공 페이지를 반환하는 메서드
    @GetMapping("/login-success")
    public String loginSuccess(@RequestParam("username") String username, Model model) {
        model.addAttribute(username);
        return "login_success";
    }

    // 깃허브 앱 콜백 처리를 위한 메서드
    // 깃허브에서 전달받은 코드를 사용해 액세스 토큰을 얻고, 사용자 정보를 저장합니다.
    @PostMapping("/github-app-callback")
    public ResponseEntity<?> handleCallback(@RequestParam("code") String code) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", gitHubConfig.getClientId());
        requestBody.add("client_secret", gitHubConfig.getClientSecret());
        requestBody.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, request, String.class);

        JsonNode tokenNode;
        try {
            tokenNode = githubUserService.getJsonNode(response.getBody());
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("액세스 토큰 처리 중 오류가 발생했습니다.");
        }

        String accessToken = tokenNode.get("access_token").asText();
        HttpHeaders userRequestHeaders = new HttpHeaders();
        userRequestHeaders.setBearerAuth(accessToken);

        HttpEntity<Void> userRequestEntity = new HttpEntity<>(null, userRequestHeaders);

        ResponseEntity<String> userResponse = restTemplate.exchange("https://api.github.com/user", HttpMethod.GET, userRequestEntity, String.class);
        JsonNode userNode;
        try {
            userNode = githubUserService.getJsonNode(userResponse.getBody());
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보 처리 중 오류가 발생했습니다.");
        }

        String apiUrl = "https://api.github.com/user/orgs";
        ResponseEntity<String> orgsResponse = restTemplate.exchange(apiUrl, HttpMethod.GET, userRequestEntity, String.class);
        JsonNode orgsNode;
        try {
            orgsNode = githubUserService.getJsonNode(orgsResponse.getBody());
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("조직 정보 처리 중 오류가 발생했습니다.");
        }

        List<Organization> organizations = githubUserService.getOrganizations(orgsNode);

        String githubUsername = userNode.get("login").asText();
        String profileImageUrl = userNode.get("avatar_url").asText();

        String accountCreatedDateString = userNode.get("created_at").asText();
        ZonedDateTime accountCreatedDateTime = ZonedDateTime.parse(accountCreatedDateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        LocalDate accountCreatedDate = accountCreatedDateTime.toLocalDate();

        User user = githubUserService.saveOrUpdateUser(githubUsername, accessToken, accountCreatedDate, profileImageUrl, organizations);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("access_token", accessToken);
        responseBody.put("github_username", githubUsername);
        return ResponseEntity.ok(responseBody);
    }
}
