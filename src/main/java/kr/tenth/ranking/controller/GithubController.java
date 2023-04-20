package kr.tenth.ranking.controller;

import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/github")
public class GithubController {

    private final GithubService githubService;

    @GetMapping("/user/{username}")
    public ResponseEntity<String> getUserInfo(@PathVariable String username) {
        return githubService.getUserInfo(username);
    }

    @PostMapping("/user/{username}")
    public ResponseEntity<User> saveUser(@PathVariable String username) {
        User user = githubService.saveUser(username);
        githubService.saveUserStats(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/user/{username}/stats")
    public ResponseEntity<User> getUserStats(@PathVariable String username) {
        User user = githubService.getUserStats(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/user/{username}/today-commits")
    public ResponseEntity<Integer> getTodayCommitCount(@PathVariable String username) {
        int todayCommitCount = githubService.getTodayCommitCount(username);
        return ResponseEntity.ok(todayCommitCount);
    }
}