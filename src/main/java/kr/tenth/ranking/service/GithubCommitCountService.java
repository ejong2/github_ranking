package kr.tenth.ranking.service;

import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.domain.UserCommitCount;
import kr.tenth.ranking.dto.UserCommitCountDto;
import kr.tenth.ranking.enu.DateRange;
import kr.tenth.ranking.repository.UserCommitCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GithubCommitCountService {
    private final UserCommitCountRepository userCommitCountRepository;

    // 커밋 카운트를 업데이트하는 메서드
    public void updateCommitCount(User user, LocalDateTime commitDate) {
        DateRange dateRange = DateRange.getDateRange(commitDate.toLocalDate());
        List<UserCommitCount> userCommitCountList = userCommitCountRepository.findByUserAndDateRange(user, dateRange);
        UserCommitCount userCommitCount;

        if (userCommitCountList.isEmpty()) {
            userCommitCount = new UserCommitCount(user, dateRange, 0);
        } else {
            userCommitCount = userCommitCountList.get(0);
        }

        userCommitCount.setCommitCount(userCommitCount.getCommitCount() + 1);
        userCommitCountRepository.save(userCommitCount);
    }

    public List<UserCommitCountDto> getUserCommitCountsByDateRange(User user, DateRange dateRange) {
        List<UserCommitCount> userCommitCounts = userCommitCountRepository.findByUserAndDateRange(user, dateRange);
        return userCommitCounts.stream()
                .map(UserCommitCountDto::convertToDto)
                .collect(Collectors.toList());
    }
}
