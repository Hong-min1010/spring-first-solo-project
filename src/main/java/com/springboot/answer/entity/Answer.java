package com.springboot.answer.entity;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @Column(nullable = false)
    private String answerContext;

    @OneToOne
    @JoinColumn(name = "QUESTION_ID")
    private Question question;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    public void setQuestion(Question question) {
        this.question = question;

        if (question.getAnswer() != this) {
            question.setAnswer(this);
        }
    }

    private AnswerStatus answerStatus = AnswerStatus.ANSWER_PUBLIC;

    public enum AnswerStatus {
        ANSWER_PUBLIC("공개 답글"),
        ANSWER_SECRET("비공개 답글");

        @Getter
        private String status;

        AnswerStatus(String status) {
            this.status = status;
        }
    }
}
