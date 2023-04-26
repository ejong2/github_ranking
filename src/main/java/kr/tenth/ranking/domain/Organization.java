package kr.tenth.ranking.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor
public class Organization {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    public Organization(String name) {
        this.name = name;
    }
}
