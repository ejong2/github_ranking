package kr.tenth.ranking.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class RepoCommitRankingDto {
    private int rank;
    private String githubUsername;
    private String repoName;
    private Integer commitCount;

    public RepoCommitRankingDto(int rank, String githubUsername, String repoName, Integer commitCount) {
        this.rank = rank;
        this.githubUsername = githubUsername;
        this.repoName = repoName;
        this.commitCount = commitCount;
    }
}
