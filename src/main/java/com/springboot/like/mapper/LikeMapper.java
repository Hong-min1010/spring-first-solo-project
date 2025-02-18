package com.springboot.like.mapper;

import com.springboot.like.dto.LikeResponseDto;
import com.springboot.like.entity.Like;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LikeMapper {
    @Mapping(target = "likeId", source = "like.likeId")
    @Mapping(target = "questionId", source = "like.question.questionId")
    @Mapping(target = "userId", source = "like.user.userId")
    LikeResponseDto likeToLikeResponseDto(Like like);
}
