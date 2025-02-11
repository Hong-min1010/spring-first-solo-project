package com.springboot.question.entity;

import com.springboot.answer.entity.Answer;
import com.springboot.like.entity.Like;
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
public class Question {

    @Id
    private Long questionId;

    @Column(nullable = false)
    private String questionContext;

    @Column(nullable = false)
    private String writer;

    @Column(nullable = false)
    private int view;

    @OneToOne(mappedBy = "question", cascade = CascadeType.REMOVE)
    private Answer answer;

    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    private List<Like> likes = new ArrayList<>();

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
