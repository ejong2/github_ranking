package kr.tenth.ranking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.tenth.ranking.domain.Organization;
import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class GithubUserService {
    private final UserRepository userRepository;

    public User saveOrUpdateUser(String githubUsername, String accessToken, LocalDate accountCreatedDate, String profileImageUrl, List<Organization> organizations) {
        Optional<User> existingUser = userRepository.findByGithubUsername(githubUsername);
        if (existingUser.isPresent()) {
            User userToUpdate = existingUser.get();
            userToUpdate.setAccessToken(accessToken);
            return userRepository.save(userToUpdate);
        } else {
            User newUser = new User(githubUsername, accessToken, accountCreatedDate, profileImageUrl);
            for (Organization organization : organizations) {
                newUser.addOrganization(organization);
            }
            return userRepository.save(newUser);
        }
    }

    public JsonNode getJsonNode(String responseBody) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(responseBody);
    }

    public List<Organization> getOrganizations(JsonNode orgsNode) {
        List<Organization> organizations = new ArrayList<>();
        for (JsonNode orgNode : orgsNode) {
            String orgName = orgNode.get("login").asText();
            Organization organization = new Organization(orgName);
            organizations.add(organization);
        }
        return organizations;
    }
    public Optional<User> findByGithubUsername(String githubUsername) {
        return userRepository.findByGithubUsername(githubUsername);
    }
    public List<User> findAll() {
        return userRepository.findAll();
    }
}
