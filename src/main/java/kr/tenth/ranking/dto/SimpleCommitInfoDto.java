package kr.tenth.ranking.dto;

import kr.tenth.ranking.domain.CommitInfo;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private String commitDate;

    public static SimpleCommitInfoDto convertToSimpleDto(CommitInfo commitInfo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime commitDateTime = commitInfo.getCommitDate().minusHours(9);
        String commitDate = commitDateTime.format(formatter);

        return SimpleCommitInfoDto.builder()
                .githubUsername(commitInfo.getUser().getGithubUsername())
                .profileImageUrl(commitInfo.getUser().getProfileImageUrl())
                .commitMessage(commitInfo.getCommitMessage())
                .repoName(commitInfo.getRepoName())
                .commitDate(commitDate)
                .build();
    }
}