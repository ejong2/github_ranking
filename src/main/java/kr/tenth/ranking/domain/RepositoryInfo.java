package kr.tenth.ranking.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class RepositoryInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String repoName;
    private boolean isPrivate;
    private String mainLanguage;

    public RepositoryInfo(User user, String repoName, boolean isPrivate, String mainLanguage) {
        this.user = user;
        this.repoName = repoName;
        this.isPrivate = isPrivate;
        this.mainLanguage = mainLanguage;
    }
}
