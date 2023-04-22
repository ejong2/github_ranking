package kr.tenth.ranking.repository;

import kr.tenth.ranking.domain.ReviewInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewInfoRepository extends JpaRepository<ReviewInfo, Long> {
}
