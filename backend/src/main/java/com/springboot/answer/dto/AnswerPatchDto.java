package com.springboot.answer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
public class AnswerPatchDto {
    // 어느 질문을 수정 했는지 알기 위해 사용
    @Positive
    private Long answerId;

    @NotBlank
    private String answerContext;
}
