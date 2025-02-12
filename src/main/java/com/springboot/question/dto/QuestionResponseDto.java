package com.springboot.question.dto;

import com.springboot.answer.entity.Answer;
import com.springboot.like.entity.Like;
import lombok.Getter;

import java.util.List;

@Getter
public class QuestionResponseDto {
    private Long questionId;

    private String questionContext;

    private int view;

    private Answer answer;

    private List<Like> likes;
}
