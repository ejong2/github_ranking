package kr.tenth.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommitInfoDto {
    private Long id;
    private Long userId;
    private String commitMessage;
    private String repoName;
    private LocalDateTime commitDate;
}
