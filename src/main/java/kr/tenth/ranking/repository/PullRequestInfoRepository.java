package kr.tenth.ranking.repository;

import kr.tenth.ranking.domain.PullRequestInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PullRequestInfoRepository extends JpaRepository<PullRequestInfo, Long>{

}
