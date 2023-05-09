package kr.tenth.ranking.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryActivityDto {
    private int ranking;
    private String repoName;
    private int commitCount;
    private List<String> usernames;
}
