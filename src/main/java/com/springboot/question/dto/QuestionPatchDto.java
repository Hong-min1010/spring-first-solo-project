package com.springboot.question.dto;

import com.springboot.question.entity.Question;
import lombok.Getter;

@Getter
public class QuestionPatchDto {

    private Long questionId;

    private Question.QuestionStatus questionStatus;

    private Question.QuestionVisibility questionVisibility;

    private int view;

    private String questionContext;
}
