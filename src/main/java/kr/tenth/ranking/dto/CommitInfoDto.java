package kr.tenth.ranking.dto;

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
//    private Long id;
    private Long userId;
    private String commitMessage;
    private String repoName;
    private LocalDateTime commitDate;
}
