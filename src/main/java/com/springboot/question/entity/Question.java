package com.springboot.question.entity;

import com.springboot.answer.entity.Answer;
import com.springboot.audit.BaseEntity;
import com.springboot.like.entity.Like;
import com.springboot.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

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
    private String title;

    @Column(nullable = false)
    private String questionContext;

    @Column(nullable = false)
    private int viewCount;

    @OneToOne(mappedBy = "question", cascade = CascadeType.PERSIST)
    private Answer answer;

    @JoinColumn(name = "USER_ID")
    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @Enumerated(EnumType.STRING)
    private QuestionStatus questionStatus;

    public enum QuestionStatus {
        // 질문 등록
        QUESTION_REGISTERED("질문 등록 상태"),
        QUESTION_ANSWERED("답변 완료 상태"),
        QUESTION_DELETED("질문 삭제 상태"),
        // 회원이 탈퇴, 비활성화 時 질문 비활성화
        QUESTION_DEACTIVED("질문 비활성화 상태");

        @Getter
        private final String status;

        QuestionStatus(String status) {
            this.status = status;
        }
    }

    @Enumerated(EnumType.STRING)
    private QuestionVisibility questionVisibility;

    public enum QuestionVisibility {
        QUESTION_PUBLIC,
        QUESTION_SECRET
    }

    @Column(nullable = false)
    private int likeCount = 0;

    public void setUser(User user) {
        this.user = user;

        if (!user.getQuestions().contains(this)) {
            user.setQuestions(this);
        }

    }

    public void setAnswer(Answer answer) {
        this.answer = answer;

        if (answer.getQuestion() != this) {
            answer.setQuestion(this);
        }
    }
    public void addLikeCount() {
        // addLikeCount가 호출 될때마다 +1씩 증가
        this.likeCount++;
    }
    public void decreaseLikeCount() {
        // decreaseLikeCount가 호출 될때마다 LikeCount 1씩 감소 (최소값 = 0)
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

    // Entity가 저장되기 전 강제로 값 설정하는 애너테이션
    @PrePersist
    public void prePersist() {
        if (this.questionStatus == null) {
            this.questionStatus = QuestionStatus.QUESTION_REGISTERED;
        }

        if (this.questionVisibility == null) {
            this.questionVisibility = QuestionVisibility.QUESTION_PUBLIC;
        }
    }
}
