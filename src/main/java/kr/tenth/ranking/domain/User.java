package kr.tenth.ranking.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@DynamicUpdate
@DynamicInsert
@Getter
@NoArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<Organization> organizations = new ArrayList<>();
    private String githubUsername;
    private String accessToken;
    private String profileImageUrl;
    private LocalDate accountCreatedDate;
    @Column(name = "last_saved_commit_date")
    private LocalDateTime lastSavedCommitDate;

    public User(String githubUsername, String accessToken, LocalDate accountCreatedDate, String profileImageUrl) {
        this.githubUsername = githubUsername;
        this.accessToken = accessToken;
        this.accountCreatedDate = accountCreatedDate;
        this.profileImageUrl = profileImageUrl;
    }

    // 액세스 토큰을 설정하는 세터 메서드를 추가합니다.
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public void setLastSavedCommitDate(LocalDateTime lastSavedCommitDate) {
        this.lastSavedCommitDate = lastSavedCommitDate;
    }
    public void addOrganization(Organization organization) {
        this.organizations.add(organization);
    }
}
