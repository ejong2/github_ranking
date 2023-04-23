package kr.tenth.ranking.repository;

import kr.tenth.ranking.domain.CommitInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CommitInfoRepository extends JpaRepository<CommitInfo, Long> {
    Optional<CommitInfo> findByUsernameAndRepoNameAndCommitMessageAndCommitDate(String username, String repoName, String commitMessage, LocalDateTime commitDate);
}