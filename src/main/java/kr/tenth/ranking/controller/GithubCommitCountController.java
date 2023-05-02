package kr.tenth.ranking.controller;

import kr.tenth.ranking.common.Result;
import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.dto.UserCommitCountDto;
import kr.tenth.ranking.enu.DateRange;
import kr.tenth.ranking.service.GithubCommitCountService;
import kr.tenth.ranking.service.GithubUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/commit-count")
public class GithubCommitCountController {

    private final GithubUserService githubUserService;

    private final GithubCommitCountService githubCommitCountService;
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCommitCount(
            @RequestParam String githubUsername) {

        User user = githubUserService.findUserByGithubUsername(githubUsername);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        List<UserCommitCountDto> todayCommitCounts = githubCommitCountService.getUserCommitCountsByDateRange(user, DateRange.ONE_DAY);
        List<UserCommitCountDto> oneWeekCommitCounts = githubCommitCountService.getUserCommitCountsByDateRange(user, DateRange.ONE_WEEK);
        List<UserCommitCountDto> oneMonthCommitCounts = githubCommitCountService.getUserCommitCountsByDateRange(user, DateRange.ONE_MONTH);

        Result result = new Result();
        result.addItem("todayCommitCounts", todayCommitCounts);
        result.addItem("oneWeekCommitCounts", oneWeekCommitCounts);
        result.addItem("oneMonthCommitCounts", oneMonthCommitCounts);
        return ResponseEntity.ok(result.getData());
    }
}
