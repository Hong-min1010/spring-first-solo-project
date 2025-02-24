package com.springboot.answer.dto;

import lombok.*;


@Getter
@Builder
@AllArgsConstructor
public class AnswerResponseDto {
    // 어느 질문에 대한 Response인지 확인하기 위해 사용
    private Long answerId;
    private String answerContext;
}
