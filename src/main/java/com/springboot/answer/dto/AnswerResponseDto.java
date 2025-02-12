package com.springboot.answer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
public class AnswerResponseDto {
    // 어느 질문에 대한 Response인지 확인하기 위해 사용
    @Positive
    private Long answerId;

    @NotBlank
    private String answerContext;
}
