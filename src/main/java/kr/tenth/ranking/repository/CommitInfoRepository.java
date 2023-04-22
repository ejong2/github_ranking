package kr.tenth.ranking.repository;

import kr.tenth.ranking.domain.CommitInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitInfoRepository extends JpaRepository<CommitInfo, Long> {
}