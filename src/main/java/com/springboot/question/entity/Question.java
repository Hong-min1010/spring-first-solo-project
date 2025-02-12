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
    private Long questionId;

    @Column(nullable = false)
    private String questionContext;

    @Column(nullable = false)
    private int view;

    @OneToOne(mappedBy = "question", cascade = CascadeType.REMOVE)
    private Answer answer;

    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    private List<Like> likes = new ArrayList<>();

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
    private QuestionVisibility questionVisibility = QuestionVisibility.PUBLIC;

    public enum QuestionVisibility {
        PUBLIC,
        SECRET
    }

}
