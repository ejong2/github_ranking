package kr.tenth.ranking.service;

import kr.tenth.ranking.domain.Organization;
import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GithubUserService {

    private final UserRepository userRepository;

    public GithubUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
}
