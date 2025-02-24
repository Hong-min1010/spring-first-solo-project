package com.springboot.like.mapper;

import com.springboot.like.dto.LikeResponseDto;
import com.springboot.like.entity.Like;
import com.springboot.question.entity.Question;
import com.springboot.user.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-02-19T18:41:55+0900",
    comments = "version: 1.5.2.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-7.6.1.jar, environment: Java 11.0.25 (Azul Systems, Inc.)"
)
@Component
public class LikeMapperImpl implements LikeMapper {

    @Override
    public LikeResponseDto likeToLikeResponseDto(Like like) {
        if ( like == null ) {
            return null;
        }

        LikeResponseDto likeResponseDto = new LikeResponseDto();

        likeResponseDto.setLikeId( like.getLikeId() );
        likeResponseDto.setQuestionId( likeQuestionQuestionId( like ) );
        likeResponseDto.setUserId( likeUserUserId( like ) );

        return likeResponseDto;
    }

    private Long likeQuestionQuestionId(Like like) {
        if ( like == null ) {
            return null;
        }
        Question question = like.getQuestion();
        if ( question == null ) {
            return null;
        }
        Long questionId = question.getQuestionId();
        if ( questionId == null ) {
            return null;
        }
        return questionId;
    }

    private Long likeUserUserId(Like like) {
        if ( like == null ) {
            return null;
        }
        User user = like.getUser();
        if ( user == null ) {
            return null;
        }
        Long userId = user.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }
}
