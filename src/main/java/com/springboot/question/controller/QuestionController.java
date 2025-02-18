package com.springboot.question.controller;

import com.springboot.answer.dto.AnswerPatchDto;
import com.springboot.answer.dto.AnswerPostDto;
import com.springboot.answer.dto.AnswerResponseDto;
import com.springboot.answer.entity.Answer;
import com.springboot.answer.mapper.AnswerMapper;
import com.springboot.answer.service.AnswerService;
import com.springboot.auth.utils.CustomUserDetails;
import com.springboot.dto.MultiResponseDto;
import com.springboot.dto.SingleResponseDto;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.question.dto.QuestionPatchDto;
import com.springboot.question.dto.QuestionPostDto;
import com.springboot.question.dto.QuestionResponseDto;
import com.springboot.question.entity.Question;
import com.springboot.question.mapper.QuestionMapper;
import com.springboot.question.service.QuestionService;
import com.springboot.user.entity.User;
import com.springboot.user.service.UserService;
import com.springboot.utils.CheckUserRoles;
import com.springboot.utils.UriCreator;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/questions")
@Validated
public class QuestionController {
    private final QuestionService questionService;
    private final UserService userService;
    private final QuestionMapper questionMapper;
    private final CheckUserRoles checkUserRoles;
    private final AnswerService answerService;
    private final AnswerMapper answerMapper;

    public QuestionController(QuestionService questionService, QuestionMapper questionMapper,
                              UserService userService, CheckUserRoles checkUserRoles,
                              AnswerService answerService, AnswerMapper answerMapper) {
        this.questionService = questionService;
        this.questionMapper = questionMapper;
        this.userService = userService;
        this.checkUserRoles = checkUserRoles;
        this.answerService = answerService;
        this.answerMapper = answerMapper;
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
                                        @RequestParam("user-id") Long userId,
                                        @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long currentId = customUserDetails.getUserId();

        Question updateQuestion =
                questionService.updateQuestion(questionId, currentId, questionMapper.questionPatchDtoToQuestion(requestBody));

        QuestionResponseDto responseDto = questionMapper.questionToQuestionResponseDto(updateQuestion);

        if (!currentId.equals(userId)) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }

        return new ResponseEntity<>(
                new SingleResponseDto<>(responseDto), HttpStatus.OK
        );
    }

    @PreAuthorize("hasRole('ADMIN') or @questionService.isAuthorOrAdmin(#questionId, authentication.name)")
    @GetMapping("/{question-id}")
    public ResponseEntity getQuestion(@PathVariable("question-id") Long questionId,
                                      @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Question question = questionService.findQuestion(questionId, customUserDetails.getUserId());

//        Long currentUseId = customUserDetails.getUserId();
//        System.out.println("Authenticated User ID: " + currentUseId);
//        if (!question.getUser().getUserId().equals(currentUseId)) {
//            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
//        }

        String questionContext = question.getQuestionVisibility() == Question.QuestionVisibility.QUESTION_SECRET
                ? "비밀글입니다."
                : question.getQuestionContext();

        String title = question.getQuestionVisibility() == Question.QuestionVisibility.QUESTION_SECRET
                ? "SECRET"
                : question.getTitle();


        QuestionResponseDto questionResponseDto = questionMapper.questionToQuestionResponseDto(question);

        questionResponseDto = questionResponseDto.toBuilder()
                .title(title)
                .questionContext(questionContext)
                .build();

        return new ResponseEntity<>(new SingleResponseDto<>(questionResponseDto), HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Question>> getQuestions(@RequestParam("sortBy") String sortBy) {
        List<Question> questions = questionService.getQuestionSorted(sortBy);
        return ResponseEntity.ok(questions);
    }

    @GetMapping
    public ResponseEntity getQuestions(@Positive @RequestParam int page,
                                       @Positive @RequestParam int size) {
        Page<Question> pageQuestions = questionService.findQuestions(page, size);
        List<Question> questions = pageQuestions.getContent();

        List<QuestionResponseDto> list = questionMapper.questionsToQuestionResponses(questions);

        for (int i = 0; i < list.size(); i++) {
            QuestionResponseDto questionResponseDto = list.get(i);
            if (questions.get(i).getQuestionVisibility() == Question.QuestionVisibility.QUESTION_SECRET) {
                questionResponseDto = questionResponseDto.toBuilder()
                        .title("SECRET")
                        .questionContext("비공개글입니다.")
                        .build();
                list.set(i, questionResponseDto);  // 변경된 dto 다시 리스트에 반영
            }
        }

        return new ResponseEntity<>(
                new MultiResponseDto<>(list, pageQuestions), HttpStatus.OK
        );
    }

    @DeleteMapping("/{question-id}")
    public ResponseEntity deleteQuestion(@PathVariable("question-id") @Positive Long questionId,
                                         @RequestParam("user-id") Long userId,
                                         @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long currentUserId = customUserDetails.getUserId();

        Question question = questionService.findVerifiedQuestion(questionId);

        Long questionOwnerId = question.getUser().getUserId();

        userService.matchUserId(userId, customUserDetails);

        if (!checkUserRoles.isAdmin() && !currentUserId.equals(questionOwnerId)) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }

        questionService.deleteQuestion(questionId, customUserDetails);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{question-id}/answers")
    public ResponseEntity<AnswerResponseDto> postAnswer(@PathVariable("question-id") Long questionId,
                                                          @RequestBody AnswerPostDto answerPostDto,
                                                          @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Answer answer = answerMapper.answerPostDtoToAnswer(answerPostDto);

        Answer newAnswer = answerService.createAnswer(answer, questionId, customUserDetails);

        AnswerResponseDto responseDto = answerMapper.answerToAnswerResponseDto(newAnswer);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PatchMapping("/{question-id}/answers")
    public ResponseEntity<AnswerResponseDto> patchAnswer(@PathVariable("question-id") Long questionId,
                                                          @RequestBody AnswerPatchDto answerPatchDto,
                                                          @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Answer patchAnswer = answerMapper.answerPatchDtoToAnswer(answerPatchDto);

        Answer updateAnswer = answerService.updateAnswer(questionId, patchAnswer, customUserDetails);

        AnswerResponseDto responseDto = answerMapper.answerToAnswerResponseDto(updateAnswer);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{question-id}/answers")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable("question-id") Long questionId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        answerService.deleteAnswer(questionId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
