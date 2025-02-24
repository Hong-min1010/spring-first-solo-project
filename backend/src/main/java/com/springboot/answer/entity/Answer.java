package com.springboot.answer.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.springboot.audit.BaseEntity;
import com.springboot.question.entity.Question;
import com.springboot.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Answer extends BaseEntity {

    @Id
    // ID는 DB로 권한 넘기기 자동 생성
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @Column(nullable = false)
    private String answerContext;

    @OneToOne
    // Question_ID를 기준으로 Join
    @JoinColumn(name = "QUESTION_ID")
    // Question과 순환참조 방지 애너테이션
    @JsonBackReference
    private Question question;

    @ManyToOne(fetch = FetchType.EAGER)
    // User_ID 기준으로 Join
    @JoinColumn(name = "USER_ID")
    private User user;

    public void setQuestion(Question question) {
        this.question = question;

        if (question.getAnswer() != this) {
            question.setAnswer(this);
        }

    }
}

