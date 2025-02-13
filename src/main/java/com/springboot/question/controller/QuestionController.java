package com.springboot.question.controller;

import com.springboot.dto.SingleResponseDto;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.question.dto.QuestionPatchDto;
import com.springboot.question.dto.QuestionPostDto;
import com.springboot.question.entity.Question;
import com.springboot.question.mapper.QuestionMapper;
import com.springboot.question.repository.QuestionRepository;
import com.springboot.question.service.QuestionService;
import com.springboot.user.entity.User;
import com.springboot.user.service.UserService;
import com.springboot.utils.UriCreator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/v1/questions")
@Validated
public class QuestionController {
    private final QuestionService questionService;
    private final UserService userService;
    private final QuestionMapper questionMapper;
    private final QuestionRepository questionRepository;

    public QuestionController(QuestionService questionService, QuestionMapper questionMapper,
                              UserService userService, QuestionRepository questionRepository) {
        this.questionService = questionService;
        this.questionMapper = questionMapper;
        this.userService = userService;
        this.questionRepository = questionRepository;
    }

    @PostMapping
    public ResponseEntity postQuestion(@Valid @RequestBody QuestionPostDto requestBody) {

        Question question = questionMapper.questionPostDtoToQuestion(requestBody);

        Question createdQuestion = questionService.createQuestion(question);

        URI location = UriCreator.createUri("/v1/questions", createdQuestion.getQuestionId());

        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{question-id}")
    public ResponseEntity patchQuestion(@PathVariable("question-id") Long questionId,
                                        @Valid @RequestBody QuestionPatchDto questionPatchDto,
                                        @AuthenticationPrincipal UserDetails userDetails) {

        String currentEmail = userDetails.getUsername();

        Question findQuestion = questionService.findVerifiedQuestion(questionId);

        if (!findQuestion.getUser().getEmail().equals(currentEmail)) {
            throw new BusinessLogicException(ExceptionCode.QUESTION_FORBIDDEN);
        }

        Question updateQuestion =
                questionService.updateQuestion(questionId, questionMapper.questionPatchDtoToQuestion(questionPatchDto));


        return new ResponseEntity(
                new SingleResponseDto<>(questionMapper.questionToQuestionResponseDto(question)), HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity getQuestion() {

    }
}
