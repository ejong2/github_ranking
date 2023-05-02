package kr.tenth.ranking.controller;

import kr.tenth.ranking.common.Result;
import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.dto.SimpleCommitInfoDto;
import kr.tenth.ranking.service.GithubCommitService;
import kr.tenth.ranking.service.GithubUserService;
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
    private final GithubUserService githubUserService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCommits(
            @RequestParam(required = false) String githubUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) throws Exception {
        Result result = new Result();
        List<SimpleCommitInfoDto> simpleCommitInfoDtos;

        if (githubUsername == null || githubUsername.isEmpty()) {
            simpleCommitInfoDtos = commitService.getAllUsersCommitsEntities(fromDate, toDate);
        } else {
            Optional<User> optionalUser = githubUserService.findByGithubUsername(githubUsername);

            if (!optionalUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            User user = optionalUser.get();
            simpleCommitInfoDtos = commitService.getCommitsEntities(user, fromDate, toDate);
        }

        result.addItem("commitList", simpleCommitInfoDtos);
        result.addItem("totalCount", simpleCommitInfoDtos.size());
        return ResponseEntity.ok(result.getData());
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCommitCountByPeriod(
            @RequestParam(required = false) String githubUsername) {

        List<User> users;
        if (githubUsername == null) {
            users = githubUserService.findAll();
        } else {
            Optional<User> optionalUser = githubUserService.findByGithubUsername(githubUsername);
            if (optionalUser.isPresent()) {
                users = Collections.singletonList(optionalUser.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        List<Map<String, Object>> commitList = commitService.getCommitCountListByPeriod(users);

        Map<String, Object> response = new HashMap<>();
        response.put("commitList", commitList);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> updateCommits() throws IOException {
        commitService.updateAllUsersCommits();
        return ResponseEntity.ok().build();
    }
}