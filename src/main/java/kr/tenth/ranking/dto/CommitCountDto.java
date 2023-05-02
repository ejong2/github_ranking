package kr.tenth.ranking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitCountDto {
    private String githubUsername;
    private int todayCommitCount;
    private int weeklyCommitCount;
    private int monthlyCommitCount;

    public Map<String, Object> toMap() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(this, Map.class);
    }
}