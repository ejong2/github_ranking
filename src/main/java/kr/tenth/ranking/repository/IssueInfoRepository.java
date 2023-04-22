package kr.tenth.ranking.repository;

import kr.tenth.ranking.domain.IssueInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueInfoRepository extends JpaRepository<IssueInfo, Long> {
}
