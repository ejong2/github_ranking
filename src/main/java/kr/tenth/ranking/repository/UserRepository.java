package kr.tenth.ranking.repository;

import kr.tenth.ranking.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByGithubUsername(String githubUsername);

    @Query("SELECT u.githubUsername FROM User u JOIN u.repositories r WHERE r.repoName = :repoName")
    List<String> findUsernamesByRepoName(@Param("repoName") String repoName);
}
