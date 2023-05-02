package kr.tenth.ranking.domain;

import kr.tenth.ranking.enu.DateRange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCommitCount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private DateRange dateRange;

    private int commitCount;

    public void setCommitCount(int commitCount) {
        this.commitCount = commitCount;
    }
    public UserCommitCount(User user, DateRange dateRange, int commitCount) {
        this.user = user;
        this.dateRange = dateRange;
        this.commitCount = commitCount;
    }
}
