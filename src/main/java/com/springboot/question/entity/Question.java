package com.springboot.question.entity;

import com.springboot.answer.entity.Answer;
import com.springboot.audit.BaseEntity;
import com.springboot.like.entity.Like;
import com.springboot.user.entity.User;
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
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(nullable = false)
    private String questionContext;

    @Column(nullable = false)
    private int viewCount;

    @OneToOne(mappedBy = "question", cascade = CascadeType.REMOVE)
    private Answer answer;

    @JoinColumn(name = "USER_ID")
    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private QuestionStatus questionStatus = QuestionStatus.QUESTION_REGISTERED;

    public enum QuestionStatus {
        // 질문 등록
        QUESTION_REGISTERED,
        QUESTION_ANSWERED,
        QUESTION_DELETED,
        // 회원이 탈퇴, 비활성화 時 질문 비활성화
        QUESTION_DEACTIVED
    }

    @Enumerated(EnumType.STRING)
    private QuestionVisibility questionVisibility = QuestionVisibility.QUESTION_PUBLIC;

    public enum QuestionVisibility {
        QUESTION_PUBLIC,
        QUESTION_SECRET
    }

    @Column(nullable = false)
    private int likeCount;

    public void setUser(User user) {
        this.user = user;

        if (!user.getQuestions().contains(this)) {
            user.getQuestions().add(this);
        }
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;

        if (answer.getQuestion() != this) {
            answer.setQuestion(this);
        }
    }
}
