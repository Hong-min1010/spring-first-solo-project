package com.springboot.like.dto;

import com.springboot.like.entity.Like;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Positive;
import java.util.List;

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
