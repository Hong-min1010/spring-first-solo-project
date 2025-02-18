package com.springboot.user.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.springboot.answer.entity.Answer;
import com.springboot.audit.BaseEntity;
import com.springboot.like.entity.Like;
import com.springboot.question.entity.Question;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
// user가 DB에서 사용하는 예약어이기때문에 따로 설정
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    private String password;

    // User마다 권한 부여 -> 자동 테이블 생성(h2)
    @Column(nullable = false)
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Question> questions = new ArrayList<>();

    public void quitUser() {
        this.userStatus = UserStatus.USER_QUIT;

        if (questions != null) {
            questions.forEach(question -> question.setQuestionStatus(Question.QuestionStatus.QUESTION_DEACTIVED));
        }
    }

    private UserStatus userStatus = UserStatus.USER_ACTIVE;

    public enum UserStatus {
        USER_ACTIVE,
        USER_QUIT
    }

    public void setQuestions(Question question) {
        questions.add(question);

        if (question.getUser() != this) {
            question.setUser(this);
        }

    }
}
