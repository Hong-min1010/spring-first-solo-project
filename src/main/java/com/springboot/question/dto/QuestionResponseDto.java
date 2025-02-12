package com.springboot.question.dto;

import com.springboot.answer.entity.Answer;
import com.springboot.like.entity.Like;
import com.springboot.question.entity.Question;
import com.springboot.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@NoArgsConstructor
@Getter
public class QuestionResponseDto {
    private Long questionId;

    private String questionContext;

    private int viewCount;

    private int likeCount;

    private Question.QuestionStatus questionStatus;

    private Question.QuestionVisibility questionVisibility;

    private String userName;
}
