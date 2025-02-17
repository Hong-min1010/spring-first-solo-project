package com.springboot.answer.mapper;

import com.springboot.answer.dto.AnswerPatchDto;
import com.springboot.answer.dto.AnswerPostDto;
import com.springboot.answer.dto.AnswerResponseDto;
import com.springboot.answer.entity.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnswerMapper {
    @Mapping(source = "question.questionId", target = "questionId")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.email", target = "userName")
    AnswerResponseDto answerToAnswerResponseDto(Answer answer);
    Answer answerPostDtoToAnswer(AnswerPostDto answerPostDto);
    Answer answerPatchDtoToAnswer(AnswerPatchDto answerPatchDto);
}
