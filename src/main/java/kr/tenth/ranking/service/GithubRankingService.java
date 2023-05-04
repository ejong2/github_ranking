package kr.tenth.ranking.service;

import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.dto.UserRankingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class GithubRankingService {
    private final GithubUserService githubUserService;
    private final GithubCommitService commitService;

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
