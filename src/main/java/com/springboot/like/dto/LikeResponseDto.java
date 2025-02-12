package com.springboot.like.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
public class LikeResponseDto {
    @Positive
    private Long likeId;
    @Positive
    private Long questionId;
    @Positive
    private Long userName;
}
