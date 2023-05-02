package kr.tenth.ranking.dto;

import kr.tenth.ranking.domain.CommitInfo;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleCommitInfoDto {
    private String githubUsername;
    private String profileImageUrl;
    private String commitMessage;
    private String repoName;
    private LocalDateTime commitDate;

    public static SimpleCommitInfoDto convertToSimpleDto(CommitInfo commitInfo) {
        return SimpleCommitInfoDto.builder()
                .githubUsername(commitInfo.getUser().getGithubUsername())
                .profileImageUrl(commitInfo.getUser().getProfileImageUrl())
                .commitMessage(commitInfo.getCommitMessage())
                .repoName(commitInfo.getRepoName())
                .commitDate(commitInfo.getCommitDate())
                .build();
    }
}
