package com.springboot.question.dto;

import com.springboot.answer.entity.Answer;
import com.springboot.like.entity.Like;
import com.springboot.question.entity.Question;
import com.springboot.user.entity.User;
import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Positive;
import java.util.List;

@Setter
@NoArgsConstructor
@Getter
public class QuestionPostDto {

    @Positive
    // 누가 question을 작성했는지 식별하기 위한 Id
    private Long userId;

    @NotNull
    private String title;

    @NotNull
    private String questionContext;

    @Enumerated(EnumType.STRING)
    private Question.QuestionStatus questionStatus;

    @Enumerated(EnumType.STRING)
    private Question.QuestionVisibility questionVisibility;
}
