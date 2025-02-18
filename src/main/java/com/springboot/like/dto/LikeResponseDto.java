package com.springboot.like.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponseDto {
    @Positive
    private Long likeId;
    @Positive
    private Long questionId;
    @Positive
    private Long userId;
}
