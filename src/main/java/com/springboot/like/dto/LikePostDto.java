package com.springboot.like.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
public class LikePostDto {
    @Positive
    // 좋아요를 누른 질문
    private Long userId;
    @Positive
    // 좋아요를 누른 사용자
    private Long questionId;
}
