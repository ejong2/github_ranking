package kr.tenth.ranking.repository;

import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.domain.UserCommitCount;
import kr.tenth.ranking.enu.DateRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCommitCountRepository extends JpaRepository<UserCommitCount, Long> {
    List<UserCommitCount> findByUserAndDateRange(User user, DateRange dateRange);
}