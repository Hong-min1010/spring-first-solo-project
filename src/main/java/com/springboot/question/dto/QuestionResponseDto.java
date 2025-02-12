package com.springboot.question.dto;

import com.springboot.answer.entity.Answer;
import com.springboot.like.entity.Like;
import com.springboot.question.entity.Question;
import lombok.Getter;

import java.util.List;

@Getter
public class QuestionResponseDto {
    private Long questionId;

    private String questionContext;

    private int viewCount;

    private Question.QuestionStatus questionStatus;

    private Question.QuestionVisibility questionVisibility;

//    private Answer answer;
//
//    private List<Like> likes;
}
