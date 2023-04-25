package kr.tenth.ranking.controller;

import kr.tenth.ranking.domain.*;
import kr.tenth.ranking.dto.CommitInfoDto;
import kr.tenth.ranking.repository.UserRepository;
import kr.tenth.ranking.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class GithubController {
    private final GithubCommitService commitService;
    private final ContributionService contributionService;
    private final IssueService issueService;
    private final PullRequestService pullRequestService;
    private final ReviewService reviewService;
    private final UserRepository userRepository;

    // 사용자의 깃허브 커밋 정보를 조회하는 API
    // githubUsername, fromDate, toDate를 기준으로 사용자의 커밋 정보를 반환합니다.
    @GetMapping("/commits")
    public ResponseEntity<List<CommitInfoDto>> getCommits(
            @RequestParam String githubUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) throws Exception {
        Optional<User> optionalUser = userRepository.findByGithubUsername(githubUsername);
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        User user = optionalUser.get();
        List<CommitInfo> commitInfos = commitService.getCommits(user, fromDate, toDate);
        List<CommitInfoDto> commitInfoDto = commitInfos.stream()
                .map(commitInfo -> CommitInfoDto.builder()
                        .userId(commitInfo.getUser().getId())
                        .commitMessage(commitInfo.getCommitMessage())
                        .repoName(commitInfo.getRepoName())
                        .commitDate(commitInfo.getCommitDate())
                        .sha(commitInfo.getSha())
                        .committerName(commitInfo.getCommitterName())
                        .committerEmail(commitInfo.getCommitterEmail())
                        .commitUrl(commitInfo.getCommitUrl())
                        .additions(commitInfo.getAdditions())
                        .deletions(commitInfo.getDeletions())
                        .changedFiles(commitInfo.getChangedFiles())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(commitInfoDto);
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