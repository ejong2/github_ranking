package kr.tenth.ranking.controller;

import kr.tenth.ranking.domain.*;
import kr.tenth.ranking.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GithubController {
    private final CommitService commitService;
    private final ContributionService contributionService;
    private final IssueService issueService;
    private final PullRequestService pullRequestService;
    private final ReviewService reviewService;

    @GetMapping("/commits")
    public ResponseEntity<List<CommitInfo>> getCommits(
            @RequestParam String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) throws Exception {
        List<CommitInfo> commitInfos = commitService.getUserCommits(username, fromDate, toDate);
        return ResponseEntity.ok(commitInfos);
    }
    @GetMapping("/contributions")
    public ResponseEntity<List<ContributionInfo>> getContributions(
            @RequestParam String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<ContributionInfo> contributionInfos = contributionService.getUserContributions(username, fromDate, toDate);
        return ResponseEntity.ok(contributionInfos);
    }
    @GetMapping("/issues")
    public ResponseEntity<List<IssueInfo>> getIssues(
            @RequestParam String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<IssueInfo> issueInfos = issueService.getUserIssues(username, fromDate, toDate);
        return ResponseEntity.ok(issueInfos);
    }
    @GetMapping("/pullrequests")
    public ResponseEntity<List<PullRequestInfo>> getPullRequests(
            @RequestParam String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<PullRequestInfo> pullRequestInfos = pullRequestService.getUserPullRequests(username, fromDate, toDate);
        return ResponseEntity.ok(pullRequestInfos);
    }
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewInfo>> getReviews(
            @RequestParam String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<ReviewInfo> reviewInfos = reviewService.getUserReviews(username, fromDate, toDate);
        return ResponseEntity.ok(reviewInfos);
    }
}