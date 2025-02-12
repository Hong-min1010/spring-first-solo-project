package com.springboot.user.entity;

import com.springboot.audit.BaseEntity;
import com.springboot.like.entity.Like;
import com.springboot.question.entity.Question;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    @OneToMany(mappedBy = "user")
    private List<Question> questions = new ArrayList<>();

    private UserStatus userStatus = UserStatus.USER_ACTIVE;

    public enum UserStatus {
        USER_ACTIVE,
        USER_QUIT
    }
}
