package com.springboot.question.dto;

import com.springboot.question.entity.Question;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Positive;
@Setter
@Getter
@NoArgsConstructor
public class QuestionPatchDto {

    @Positive
    private Long questionId;

    private String title;

    private String questionContext;

    @Enumerated(EnumType.STRING)
    private Question.QuestionStatus questionStatus;

    @Enumerated(EnumType.STRING)
    private Question.QuestionVisibility questionVisibility;
}
