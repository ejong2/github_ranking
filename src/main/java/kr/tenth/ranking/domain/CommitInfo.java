package kr.tenth.ranking.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class CommitInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private String commitMessage;
    private String repoName;
    private LocalDateTime commitDate;

    public CommitInfo(User user, String commitMessage, String repoName, LocalDateTime commitDate) {
        this.user = user;
        this.commitMessage = commitMessage;
        this.repoName = repoName;
        this.commitDate = commitDate;
    }
}
