package kr.tenth.ranking.service;

import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.dto.RepoCommitRankingDto;
import kr.tenth.ranking.dto.UserRankingDto;
import kr.tenth.ranking.repository.CommitInfoRepository;
import kr.tenth.ranking.util.DateRangeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class GithubRankingService {
    private final GithubUserService githubUserService;
    private final GithubCommitService commitService;
    private final CommitInfoRepository commitInfoRepository;

    public List<UserRankingDto> getRankingByCommitCount(String period) {
        List<User> users = githubUserService.findAll();

        List<UserRankingDto> userRankingDtos = users.stream()
                .map(user -> {
                    int commitCount = commitService.getCommitCountByPeriod(user, period);
                    return UserRankingDto.builder()
                            .githubUsername(user.getGithubUsername())
                            .commitCount(commitCount)
                            .build();
                })
                .sorted(Comparator.comparingInt(UserRankingDto::getCommitCount).reversed())
                .collect(Collectors.toList());

        userRankingDtos = addRanking(userRankingDtos);

        return userRankingDtos;
    }

    public List<RepoCommitRankingDto> getRepoCommitRanking(String githubUsername, String period) throws IllegalAccessException {
        List<User> users;
        if (githubUsername == null) {
            users = githubUserService.findAll();
        } else {
            Optional<User> optionalUser = githubUserService.findByGithubUsername(githubUsername);
            if (optionalUser.isPresent()) {
                users = Collections.singletonList(optionalUser.get());
            } else {
                throw new IllegalAccessException("해당 사용자를 찾을 수 없습니다.");
            }
        }

        List<RepoCommitRankingDto> repoCommitRanking = new ArrayList<>();
        for (User user : users) {
            Map<String, Integer> commitCountByRepo = getCommitCountByRepo(user, period);
            int rank = 1;
            for (Map.Entry<String, Integer> entry : commitCountByRepo.entrySet()) {
                RepoCommitRankingDto dto = new RepoCommitRankingDto(rank, user.getGithubUsername(), entry.getKey(), entry.getValue());
                repoCommitRanking.add(dto);
                rank++;
            }
        }

        return repoCommitRanking;
    }

    public Map<String, Integer> getCommitCountByRepo(User user, String period) {
        Map<String, Integer> commitCountByRepo = new HashMap<>();

        LocalDate startDate = getStartDateByPeriod(period);
        LocalDateTime startDateTime =  commitService.convertToDateInUtc(startDate, LocalTime.MIDNIGHT);

        LocalDateTime endDate = LocalDateTime.now();

        List<Object[]> repoNameAndCommitCount = commitInfoRepository.findRepoNameAndCommitCountByUserAndCommitDateBetween(user, startDateTime, endDate);
        for (Object[] obj : repoNameAndCommitCount) {
            commitCountByRepo.put((String) obj[0], ((Number) obj[1]).intValue());
        }

        return commitCountByRepo;
    }

    private LocalDate getStartDateByPeriod(String period) {
        LocalDate today = LocalDate.now();

        switch (period.toLowerCase()) {
            case "daily":
                return today.minusDays(1);
            case "weekly":
                return DateRangeUtils.getFirstDayOfWeek(today);
            case "monthly":
                return DateRangeUtils.getFirstDayOfMonth(today);
            case "yearly":
                return today.minusYears(1).withDayOfYear(1);
            default:
                throw new IllegalArgumentException("지원하지 않는 기간입니다.");
        }
    }

    private List<UserRankingDto> addRanking(List<UserRankingDto> userRankingDtos) {
        int rank = 1;
        int prevCommitCount = -1;
        int sameRankCount = 0;

        for (UserRankingDto userRankingDto : userRankingDtos) {
            if (userRankingDto.getCommitCount() == prevCommitCount) {
                userRankingDto.setRank(rank - ++sameRankCount);
            } else {
                userRankingDto.setRank(rank++);
                sameRankCount = 0;
            }
            prevCommitCount = userRankingDto.getCommitCount();
        }

        return userRankingDtos;
    }
}
