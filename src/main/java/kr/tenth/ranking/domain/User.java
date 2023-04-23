package kr.tenth.ranking.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@DynamicUpdate
@DynamicInsert
@Getter
@NoArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private String githubUsername;
    private String accessToken;

    public User(String githubUsername, String accessToken) {
        this.githubUsername = githubUsername;
        this.accessToken = accessToken;
    }
}
