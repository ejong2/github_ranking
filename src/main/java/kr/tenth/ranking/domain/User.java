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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepositoryInfo> repositories = new ArrayList<>();

    private String githubUsername;

    private String accessToken;

    private String profileImageUrl;

    private LocalDate accountCreatedDate;

    //    @Column(name = "last_saved_commit_date")
    private LocalDateTime lastSavedCommitDate;

    public User(String githubUsername, String accessToken, LocalDate accountCreatedDate, String profileImageUrl) {
        this.githubUsername = githubUsername;
        this.accessToken = accessToken;
        this.accountCreatedDate = accountCreatedDate;
        this.profileImageUrl = profileImageUrl;
    }

    public void addRepository(RepositoryInfo repositoryInfo) {
        this.repositories.add(repositoryInfo);
        repositoryInfo.setUser(this);
    }

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
