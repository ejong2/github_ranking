package kr.tenth.ranking.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRankingDto {
    private String githubUsername;
    private int commitCount;
    private int rank;
}
