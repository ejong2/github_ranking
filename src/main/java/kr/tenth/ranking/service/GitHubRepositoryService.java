package kr.tenth.ranking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.tenth.ranking.domain.RepositoryInfo;
import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.repository.RepositoryInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GitHubRepositoryService {
    private final RestTemplate restTemplate;
    private final RepositoryInfoRepository repositoryInfoRepository;

    public RepositoryInfo getRepositoryInfo(User user, String repoName) {
        String url = "https://api.github.com/repos/" + repoName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonRepo = mapper.readTree(responseBody);

            boolean isPrivate = jsonRepo.get("private").asBoolean();
            String mainLanguage = jsonRepo.get("language").asText();

            // 중복 처리 로직 추가
            Optional<RepositoryInfo> existingRepositoryInfo = repositoryInfoRepository.findByUserAndRepoName(user, repoName);
            if (existingRepositoryInfo.isPresent()) {
                return existingRepositoryInfo.get();
            }

            RepositoryInfo repositoryInfo = new RepositoryInfo(user, repoName, isPrivate, mainLanguage);
            repositoryInfoRepository.save(repositoryInfo); // 저장소 정보를 데이터베이스에 저장

            return repositoryInfo;

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("Repository not found: " + repoName, e);
            }
            throw e;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Repository info parsing error", e);
        }
    }
}
