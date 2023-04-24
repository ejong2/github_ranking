package kr.tenth.ranking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitInfoDto {
//    private Long id;
    private Long userId;
    private String commitMessage;
    private String repoName;
    private LocalDateTime commitDate;
}
