package com.springboot.question.mapper;

import com.springboot.answer.dto.AnswerResponseDto;
import com.springboot.answer.entity.Answer;
import com.springboot.question.dto.QuestionPatchDto;
import com.springboot.question.dto.QuestionPostDto;
import com.springboot.question.dto.QuestionResponseDto;
import com.springboot.question.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionMapper{
    Question questionPostDtoToQuestion(QuestionPostDto questionPostDto);
    Question questionPatchDtoToQuestion(QuestionPatchDto questionPatchDto);
//    // Question을 QuestionDto로 변환 하면서 Answer 같이 반환
//    @Mapping(source = "answers", target = "answers")
   default QuestionResponseDto questionToQuestionResponseDto(Question question) {
       QuestionResponseDto.QuestionResponseDtoBuilder builder = QuestionResponseDto.builder()
               .questionId(question.getQuestionId())
               .title(question.getTitle())
               .questionContext(question.getQuestionContext())
               .questionStatus(question.getQuestionStatus())
               .questionVisibility(question.getQuestionVisibility())
               .likeCount(question.getLikeCount())
               .viewCount(question.getViewCount())
               .userName(question.getUser().getName());

       if (question.getAnswer() != null) {
           builder.answer(
                   AnswerResponseDto.builder()
                           .answerId(question.getAnswer().getAnswerId())
                           .answerContext(question.getAnswer().getAnswerContext())
                           .build()
           );
       }
       return builder.build();
   }
    List<QuestionResponseDto> questionsToQuestionResponses(List<Question> questions);
}
