package kr.tenth.ranking.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.tenth.ranking.config.GithubConfig;
import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class GithubAppController {

    private final GithubConfig gitHubConfig;
    private final UserRepository userRepository;
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code, Model model) {
        model.addAttribute("code", code);
        return "callback";
    }

    @GetMapping("/login-success")
//    @ResponseBody
    public String loginSuccess(@RequestParam("username") String username, Model model) {
        model.addAttribute(username);
        return "login_success";
    }

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

        // 액세스 토큰을 얻고, 사용자 정보를 저장합니다.
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode tokenNode;
        try {
            tokenNode = objectMapper.readTree(response.getBody());
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
            userNode = objectMapper.readTree(userResponse.getBody());
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보 처리 중 오류가 발생했습니다.");
        }

        String githubUsername = userNode.get("login").asText();

        // 기존 사용자가 있는지 확인하고, 액세스 토큰을 업데이트합니다.
        Optional<User> existingUser = userRepository.findByGithubUsername(githubUsername);
        if (existingUser.isPresent()) {
            User userToUpdate = existingUser.get();
            userToUpdate.setAccessToken(accessToken);
            userRepository.save(userToUpdate);
        } else {
            // 새로운 사용자를 저장합니다.
            User newUser = new User(githubUsername, accessToken);
            userRepository.save(newUser);
        }

//        String githubUsername = userNode.get("login").asText();
//        User newUser = new User(githubUsername, accessToken);
//        userRepository.save(newUser);

        // 로그인 성공 페이지로 리디렉션하는 대신 응답에 github_username을 포함합니다.
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("access_token", accessToken);
        responseBody.put("github_username", githubUsername);
        return ResponseEntity.ok(responseBody);

//        return ResponseEntity.ok("사용자 정보 저장 성공");
    }
}
