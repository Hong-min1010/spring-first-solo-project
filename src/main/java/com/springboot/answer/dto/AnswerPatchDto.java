package com.springboot.answer.dto;

import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;

public class AnswerPatchDto {

    private Long answerId;

    @NotBlank
    private String answerContext;

}
