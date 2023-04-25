package kr.tenth.ranking.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

// 커밋 정보를 표현하는 클래스
// 사용자, 커밋 메시지, 저장소 이름, 커밋 날짜를 속성으로 가집니다.
@Entity
@Getter
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id")
    private RepositoryInfo repository;
    private String commitMessage;
    private String repoName;
    @Column(columnDefinition = "DATETIME(0)")
    private LocalDateTime commitDate;
    private String sha;
    private String committerName;
    private String committerEmail;
    private String commitUrl;
    private int additions;
    private int deletions;
    private int changedFiles;

    public CommitInfo(User user, String commitMessage, String repoName, LocalDateTime commitDate, String sha, String committerName, String committerEmail, String commitUrl, int additions, int deletions, int changedFiles, RepositoryInfo repository) {
        this.user = user;
        this.commitMessage = commitMessage;
        this.repoName = repoName;
        this.commitDate = commitDate;
        this.sha = sha;
        this.committerName = committerName;
        this.committerEmail = committerEmail;
        this.commitUrl = commitUrl;
        this.additions = additions;
        this.deletions = deletions;
        this.changedFiles = changedFiles;
        this.repository = repository;
    }

    public void updateCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }
}
