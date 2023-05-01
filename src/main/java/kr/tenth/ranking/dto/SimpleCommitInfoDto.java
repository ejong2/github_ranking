package kr.tenth.ranking.dto;

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
}
