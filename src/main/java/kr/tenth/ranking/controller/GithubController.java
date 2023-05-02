package kr.tenth.ranking.controller;

import kr.tenth.ranking.common.Result;
import kr.tenth.ranking.domain.CommitInfo;
import kr.tenth.ranking.domain.User;
import kr.tenth.ranking.dto.SimpleCommitInfoDto;
import kr.tenth.ranking.repository.UserRepository;
import kr.tenth.ranking.service.GithubCommitService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        List<CommitInfo> commitInfos = commitService.getCommitsEntities(user, fromDate, toDate);
        List<SimpleCommitInfoDto> simpleCommitInfoDtos = commitInfos.stream()
                .map(SimpleCommitInfoDto::convertToSimpleDto)
                .collect(Collectors.toList());

        result.addItem("commitList", simpleCommitInfoDtos);
        result.addItem("totalCount", simpleCommitInfoDtos.size());
        return ResponseEntity.ok(result.getData());
    }

    @PostMapping
    public ResponseEntity<Void> updateCommits() throws IOException {
        commitService.updateAllUsersCommits();
        return ResponseEntity.ok().build();
    }
}