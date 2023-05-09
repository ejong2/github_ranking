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

    @Query("SELECT c FROM CommitInfo c WHERE c.user.githubUsername = :githubUsername AND c.commitDate >= :fromDate AND c.commitDate <= :toDate ORDER BY c.commitDate DESC")
    List<CommitInfo> findAllByGithubUsernameAndDateRange(
            @Param("githubUsername") String githubUsername,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query("SELECT ci FROM CommitInfo ci WHERE ci.commitDate BETWEEN :fromDateTime AND :toDateTime")
    List<CommitInfo> findAllByDateRange(LocalDateTime fromDateTime, LocalDateTime toDateTime);

    @Query("SELECT ci.repoName, COUNT(ci) FROM CommitInfo ci WHERE ci.user = :user AND ci.commitDate BETWEEN :startDate AND :endDate GROUP BY ci.repoName HAVING COUNT(ci) > 0 ORDER BY COUNT(ci) DESC")
    List<Object[]> findRepoNameAndCommitCountByUserAndCommitDateBetween(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r.repoName, COUNT(c) as commitCount FROM RepositoryInfo r JOIN CommitInfo c ON r.id = c.repository.id WHERE c.commitDate >= :oneMonthAgo GROUP BY r.repoName ORDER BY commitCount DESC")
    List<Object[]> findRepoNameAndCommitCountWithRecentCommitsOrderByCommitCountDesc(@Param("oneMonthAgo") LocalDateTime oneMonthAgo);

}