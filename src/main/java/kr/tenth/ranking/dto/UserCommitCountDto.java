package kr.tenth.ranking.dto;

import kr.tenth.ranking.domain.UserCommitCount;
import kr.tenth.ranking.enu.DateRange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCommitCountDto {
    private Long userId;
    private DateRange dateRange;
    private int commitCount;

    public static UserCommitCountDto convertToDto(UserCommitCount userCommitCount) {
        return new UserCommitCountDto(
                userCommitCount.getUser().getId(),
                userCommitCount.getDateRange(),
                userCommitCount.getCommitCount()
        );
    }
}
