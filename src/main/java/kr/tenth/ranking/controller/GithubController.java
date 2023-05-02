package kr.tenth.ranking.controller;

import kr.tenth.ranking.domain.*;
import kr.tenth.ranking.dto.CommitInfoDto;
import kr.tenth.ranking.dto.SimpleCommitInfoDto;
import kr.tenth.ranking.repository.UserRepository;
import kr.tenth.ranking.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/commits")
public class GithubController {
    private final GithubCommitService commitService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<SimpleCommitInfoDto>> getCommits(
            @RequestParam String githubUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) throws Exception {
        Optional<User> optionalUser = userRepository.findByGithubUsername(githubUsername);
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        User user = optionalUser.get();
        List<CommitInfo> commitInfos = commitService.getCommitsEntities(user, fromDate, toDate);
        List<SimpleCommitInfoDto> simpleCommitInfoDtos = commitInfos.stream()
                .map(SimpleCommitInfoDto::convertToSimpleDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(simpleCommitInfoDtos);
    }

    @PostMapping
    public ResponseEntity<Void> updateCommits() throws IOException {
        commitService.updateAllUsersCommits();
        return ResponseEntity.ok().build();
    }
}