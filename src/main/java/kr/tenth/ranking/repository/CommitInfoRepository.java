package kr.tenth.ranking.repository;

import kr.tenth.ranking.domain.CommitInfo;
import kr.tenth.ranking.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// 사용자의 커밋 정보를 저장하는 리포지토리 인터페이스
// 사용자, 저장소 이름, 커밋 날짜를 기준으로 커밋 정보를 조회할 수 있는 메서드를 정의합니다.
public interface CommitInfoRepository extends JpaRepository<CommitInfo, Long> {
    Optional<CommitInfo> findByUserAndRepoNameAndCommitDate(User user, String repoName, LocalDateTime commitDate);

    @Query("SELECT c FROM CommitInfo c WHERE c.user.githubUsername = :githubUsername AND c.commitDate >= :fromDate AND c.commitDate <= :toDate")
    List<CommitInfo> findAllByGithubUsernameAndDateRange(
            @Param("githubUsername") String githubUsername,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
}