package kr.tenth.ranking.repository;

import kr.tenth.ranking.domain.ContributionInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContributionInfoRepository extends JpaRepository<ContributionInfo, Long> {
}
