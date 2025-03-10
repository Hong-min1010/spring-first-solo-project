package com.springboot.question.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    private int viewCount = 0;

    @OneToOne(mappedBy = "question", cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
    @JsonManagedReference
    private Answer answer;

    @JoinColumn(name = "USER_ID")
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    private User user;

    @Enumerated(EnumType.STRING)
    private QuestionStatus questionStatus;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(length = 255)
    private String fileName; // 원본 파일명

    @Column(length = 500)
    private String fileUrl; // S3에서 접근 가능한 파일 URL

    @ElementCollection
    @CollectionTable(name = "question_image_urls", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();  // 이미지 URL 리스트

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

    // 파일 URL 설정 메서드 추가
    public void setFileUrl(String fileUrl) {
        if (fileUrl != null && fileUrl.startsWith("https://")) { // URL 형식 검증
            this.fileUrl = fileUrl;
        } else {
            throw new IllegalArgumentException("유효한 URL이 아닙니다.");
        }
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
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
