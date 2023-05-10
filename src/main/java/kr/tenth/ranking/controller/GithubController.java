package kr.tenth.ranking.controller;

import kr.tenth.ranking.common.Result;
import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.dto.RepoCommitRankingDto;
import kr.tenth.ranking.dto.RepositoryActivityDto;
import kr.tenth.ranking.dto.SimpleCommitInfoDto;
import kr.tenth.ranking.dto.UserRankingDto;
import kr.tenth.ranking.exception.UserNotFoundException;
import kr.tenth.ranking.service.GitHubRepositoryService;
import kr.tenth.ranking.service.GithubCommitService;
import kr.tenth.ranking.service.GithubRankingService;
import kr.tenth.ranking.service.GithubUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/commits")
public class GithubController {
    private final GithubCommitService commitService;
    private final GithubUserService githubUserService;
    private final GithubRankingService githubRankingService;
    private final GitHubRepositoryService gitHubRepositoryService;

    /*
     * getCommits API는 입력된 깃허브 사용자 이름(githubUsername)과 날짜 범위(fromDate, toDate)에 대한 커밋 정보를 조회하는 API입니다.
     * 사용자 이름이 입력되지 않으면 모든 사용자의 커밋 정보를 조회합니다.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCommits(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) throws Exception {
        Result result = new Result();
        List<SimpleCommitInfoDto> simpleCommitInfoDtos;

        if (username == null || username.isEmpty()) {
            simpleCommitInfoDtos = commitService.getAllUsersCommitsEntities(fromDate, toDate);
        } else {
            Optional<User> optionalUser = githubUserService.findByGithubUsername(username);

            if (!optionalUser.isPresent()) {
                throw new UserNotFoundException("해당하는 깃허브 사용자가 없습니다: " + username);
            }
            User user = optionalUser.get();
            simpleCommitInfoDtos = commitService.getCommitsEntities(user, fromDate, toDate);
        }

        result.addItem("commitList", simpleCommitInfoDtos);
        result.addItem("totalCount", simpleCommitInfoDtos.size());
        return ResponseEntity.ok(result.getData());
    }

    /*
     * count API는 입력된 깃허브 사용자 이름(githubUsername)에 대한 일일, 주간, 월간 커밋 횟수를 조회하는 API입니다.
     * 사용자 이름이 입력되지 않으면 모든 사용자에 대한 커밋 횟수를 조회합니다.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCommitCountByPeriod(
            @RequestParam(required = false) String username) {

        List<User> users;
        if (username == null) {
            users = githubUserService.findAll();
        } else {
            Optional<User> optionalUser = githubUserService.findByGithubUsername(username);
            if (optionalUser.isPresent()) {
                users = Collections.singletonList(optionalUser.get());
            } else {
                throw new UserNotFoundException("해당하는 깃허브 사용자가 없습니다: " + username);
            }
        }

        List<Map<String, Object>> commitList = commitService.getCommitCountListByPeriod(users);

        Map<String, Object> response = new HashMap<>();
        response.put("commitList", commitList);

        return ResponseEntity.ok(response);
    }

    /*
    * ranking API는 입력된 기간(period)에 따른 사용자들의 커밋 횟수 랭킹을 조회하는 API입니다.
    * 여기서 period는 daily, weekly, monthly 중 하나를 선택할 수 있습니다.
    */
    @GetMapping("/ranking")
    public ResponseEntity<Map<String, Object>> getRankingByCommitCount(@RequestParam String period) {
        if (!period.equalsIgnoreCase("daily") && !period.equalsIgnoreCase("weekly") && !period.equalsIgnoreCase("monthly")) {
            throw new IllegalArgumentException("유효하지 않은 기간(period)입니다. 올바른 값은 daily, weekly, monthly 중 하나입니다.");
        }
        List<UserRankingDto> userRankingDtos = githubRankingService.getRankingByCommitCount(period);

        Result result = new Result();
        result.addItem("ranking", userRankingDtos);

        return ResponseEntity.ok(result.getData());
    }

    /*
     * repo-ranking API는 입력된 깃허브 사용자 이름(githubUsername)과 기간(period)에 따른 저장소 별 커밋 횟수 랭킹을 조회하는 API입니다.
     * 사용자 이름이 입력되지 않으면 모든 사용자의 저장소 별 커밋 횟수 랭킹을 조회합니다.
     * 여기서 period는 daily, weekly, monthly 중 하나를 선택할 수 있습니다.
     */
    @GetMapping("/repo-ranking")
    public ResponseEntity<Map<String, Object>> getRepoCommitRanking(@RequestParam(value = "username", required = false) String username, @RequestParam(value = "period", defaultValue = "weekly") String period) throws IllegalAccessException {
        if (!period.equalsIgnoreCase("daily") && !period.equalsIgnoreCase("weekly") && !period.equalsIgnoreCase("monthly")) {
            throw new IllegalArgumentException("유효하지 않은 기간(period)입니다. 올바른 값은 daily, weekly, monthly 중 하나입니다.");
        }
        List<RepoCommitRankingDto> repoCommitRanking = githubRankingService.getRepoCommitRanking(username, period);

        Result result = new Result();
        result.addItem("repoCommitRanking", repoCommitRanking);


        return ResponseEntity.ok(result.getData());
    }

    /*
     * active-repositories API는 입력된 기간(period)에 따른 활성화된 저장소 목록을 조회하는 API입니다.
     * 여기서 period는 daily, weekly, monthly 중 하나를 선택할 수 있습니다.
     */
    @GetMapping("/active-repositories")
    public ResponseEntity<Map<String, Object>> getActiveRepositories(@RequestParam String period) {
        if (!period.equalsIgnoreCase("daily") && !period.equalsIgnoreCase("weekly") && !period.equalsIgnoreCase("monthly")) {
            throw new IllegalArgumentException("유효하지 않은 기간(period)입니다. 올바른 값은 daily, weekly, monthly 중 하나입니다.");
        }
        List<RepositoryActivityDto> repositoryActivityDtos = gitHubRepositoryService.getActiveRepositories(period);

        Result result = new Result();
        result.addItem("activeRepositories", repositoryActivityDtos);

        return ResponseEntity.ok(result.getData());
    }

    /*
     * updateCommits API는 사용자들의 저장소 정보를 업데이트하는 API입니다.
     * 이 API를 호출하면, 사용자들의 커밋 정보가 업데이트됩니다.
     */
    @PostMapping
    public ResponseEntity<Void> updateCommits() {
        commitService.updateAllUsersCommits();
        return ResponseEntity.ok().build();
    }
}