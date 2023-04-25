package kr.tenth.ranking.dto;

import kr.tenth.ranking.domain.RepositoryInfo;
import kr.tenth.ranking.domain.User;
import lombok.*;

import java.time.LocalDateTime;

// 커밋 정보를 전달하기 위한 DTO 클래스
// 커밋 정보를 클라이언트에 전달할 때 사용합니다.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitInfoDto {
    private User userId;
    private RepositoryInfo repositoryId;
    private String commitMessage;
    private String repoName;
    private LocalDateTime commitDate;
    private String sha;
    private String authorName;
    private String authorEmail;
    private String committerName;
    private String committerEmail;
    private String commitUrl;
    private int additions;
    private int deletions;
    private int changedFiles;
}
