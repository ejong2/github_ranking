package kr.tenth.ranking.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@DynamicUpdate
@DynamicInsert
@Getter @Setter
@NoArgsConstructor
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String username;
    private int commitCount;
    private int codeReviewCount;
    private int pullRequestCount;

    public User(String username, int commitCount, int codeReviewCount, int pullRequestCount) {
        this.username = username;
        this.commitCount = commitCount;
        this.codeReviewCount = codeReviewCount;
        this.pullRequestCount = pullRequestCount;
    }
}
