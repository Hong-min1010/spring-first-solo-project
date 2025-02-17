package com.springboot.answer.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Getter
@Builder
@AllArgsConstructor
public class AnswerResponseDto {
    // 어느 질문에 대한 Response인지 확인하기 위해 사용
    private Long answerId;
    private Long userId;
    private String userName;
    private String answerContext;
    private Long questionId;
}
