package kr.tenth.ranking.controller;

import kr.tenth.ranking.common.Result;
import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.dto.CommitCountDto;
import kr.tenth.ranking.dto.SimpleCommitInfoDto;
import kr.tenth.ranking.repository.UserRepository;
import kr.tenth.ranking.service.GithubCommitService;
import kr.tenth.ranking.util.DateRangeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/commits")
public class GithubController {
    private final GithubCommitService commitService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCommits(
            @RequestParam String githubUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) throws Exception {
        Optional<User> optionalUser = userRepository.findByGithubUsername(githubUsername);
        Result result = new Result();
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        User user = optionalUser.get();
        List<SimpleCommitInfoDto> simpleCommitInfoDtos = commitService.getCommitsEntities(user, fromDate, toDate);

        result.addItem("commitList", simpleCommitInfoDtos);
        result.addItem("totalCount", simpleCommitInfoDtos.size());
        return ResponseEntity.ok(result.getData());
    }

    @PostMapping
    public ResponseEntity<Void> updateCommits() throws IOException {
        commitService.updateAllUsersCommits();
        return ResponseEntity.ok().build();
    }


    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCommitCountByPeriod(
            @RequestParam(required = false) String githubUsername) {

        List<User> users;
        if (githubUsername == null) {
            users = userRepository.findAll();
        } else {
            Optional<User> optionalUser = userRepository.findByGithubUsername(githubUsername);
            if (!optionalUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            users = Collections.singletonList(optionalUser.get());
        }

        LocalDate today = LocalDate.now();
        System.out.println("Today: " + today.getDayOfMonth()); // 로그로 출력

        List<Map<String, Object>> commitList = new ArrayList<>();

        for (User user : users) {
            LocalDate firstDayOfWeek = DateRangeUtils.getFirstDayOfWeek(today);
            LocalDate firstDayOfMonth = DateRangeUtils.getFirstDayOfMonth(today);
            LocalDate lastDayOfMonth = DateRangeUtils.getLastDayOfMonth(today);

            int todayCommitCount = commitService.getCommitsEntities(user, today, today).size();
            int weeklyCommitCount = commitService.getCommitsEntities(user, firstDayOfWeek, today).size();
            int monthlyCommitCount = commitService.getCommitsEntities(user, firstDayOfMonth, lastDayOfMonth).size();

            CommitCountDto commitCountDto = CommitCountDto.builder()
                    .githubUsername(user.getGithubUsername())
                    .todayCommitCount(todayCommitCount)
                    .weeklyCommitCount(weeklyCommitCount)
                    .monthlyCommitCount(monthlyCommitCount)
                    .build();

            commitList.add(commitCountDto.toMap());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("commitCountList", commitList);
        result.put("totalCount", commitList.size());

        return ResponseEntity.ok(result);
    }
}