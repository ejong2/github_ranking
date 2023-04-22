package kr.tenth.ranking.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@DynamicUpdate
@DynamicInsert
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ContributionInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String repoName;
    private int forks;
    private int watchers;
    private int stars;
//    private LocalDateTime createdDate;

    public ContributionInfo(String username, String repoName, int forks, int watchers, int stars, LocalDateTime createdDate) {
        this.username = username;
        this.repoName = repoName;
        this.forks = forks;
        this.watchers = watchers;
        this.stars = stars;
//        this.createdDate = createdDate;
    }
}
