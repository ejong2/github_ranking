package kr.tenth.ranking.repository;

import kr.tenth.ranking.domain.CommitInfo;
import kr.tenth.ranking.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CommitInfoRepository extends JpaRepository<CommitInfo, Long> {
    Optional<CommitInfo> findByUserAndRepoNameAndCommitDate(User user, String repoName, LocalDateTime commitDate);
}