package kr.tenth.ranking.repository;

import kr.tenth.ranking.domain.RepositoryInfo;
import kr.tenth.ranking.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositoryInfoRepository extends JpaRepository<RepositoryInfo, Long> {
    Optional<RepositoryInfo> findByUserAndRepoName(User user, String repoName);
}
