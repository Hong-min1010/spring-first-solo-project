package com.springboot.question.controller;

import com.springboot.auth.utils.CustomUserDetails;
import com.springboot.dto.SingleResponseDto;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.question.dto.QuestionPatchDto;
import com.springboot.question.dto.QuestionPostDto;
import com.springboot.question.dto.QuestionResponseDto;
import com.springboot.question.entity.Question;
import com.springboot.question.mapper.QuestionMapper;
import com.springboot.question.repository.QuestionRepository;
import com.springboot.question.service.QuestionService;
import com.springboot.user.entity.User;
import com.springboot.user.service.UserService;
import com.springboot.utils.CheckUserRoles;
import com.springboot.utils.UriCreator;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/questions")
@Validated
public class QuestionController {
    private final QuestionService questionService;
    private final UserService userService;
    private final QuestionMapper questionMapper;
    private final QuestionRepository questionRepository;
    private final CheckUserRoles checkUserRoles;

    public QuestionController(QuestionService questionService, QuestionMapper questionMapper,
                              UserService userService, QuestionRepository questionRepository,
                              CheckUserRoles checkUserRoles) {
        this.questionService = questionService;
        this.questionMapper = questionMapper;
        this.userService = userService;
        this.questionRepository = questionRepository;
        this.checkUserRoles = checkUserRoles;
    }

    @PostMapping
    public ResponseEntity postQuestion(@Valid @RequestBody QuestionPostDto requestBody,
                                       @AuthenticationPrincipal CustomUserDetails customUserDetails) {

       User user = userService.findVerifiedUser(requestBody.getUserId());

        Question question = questionMapper.questionPostDtoToQuestion(requestBody);

        question.setUser(user);

        Question createdQuestion = questionService.createQuestion(question, customUserDetails);

        URI location = UriCreator.createUri("/v1/questions", createdQuestion.getQuestionId());

        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{question-id}")
    public ResponseEntity patchQuestion(@PathVariable("question-id") Long questionId,
                                        @Valid @RequestBody QuestionPatchDto requestBody,
                                        @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long currentId = customUserDetails.getUserId();



        Question updateQuestion =
                questionService.updateQuestion(questionId, currentId, questionMapper.questionPatchDtoToQuestion(requestBody));

        QuestionResponseDto responseDto = questionMapper.questionToQuestionResponseDto(updateQuestion);

        return new ResponseEntity(
                new SingleResponseDto<>(new SingleResponseDto<>(responseDto)), HttpStatus.OK
        );
    }

    @GetMapping("/{question-id}")
    public ResponseEntity getQuestion(@PathVariable("question-id") Long questionId,
                                      Long userId,
                                      @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        QuestionResponseDto responseDto = questionService.findQuestion(questionId, userId, customUserDetails);

        Long currentUseId = customUserDetails.getUserId();

        if (!checkUserRoles.isAdmin() || !currentUseId.equals(userId)) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }
        return new ResponseEntity(
                new SingleResponseDto<>(new SingleResponseDto<>(responseDto)), HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity getQuestions(@Positive @RequestParam int page,
                                       @Positive @RequestParam int size) {
        Page<Question> pageQuestions = questionService.findQuestions(page - 1, size);
        List<Question> questions = pageQuestions.getContent();
        return new ResponseEntity(
                new SingleResponseDto<>(new SingleResponseDto<>(questions)), HttpStatus.OK
        );
    }

    @DeleteMapping("/{question-id}")
    public ResponseEntity deleteQuestion(@PathVariable("question-id") @Positive Long questionId,
                                         User user,
                                         @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        userService.matchUserId(user.getUserId(), customUserDetails);

        questionService.deleteQuestion(questionId);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
