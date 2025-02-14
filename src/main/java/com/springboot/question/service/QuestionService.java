package com.springboot.question.service;

import com.springboot.answer.dto.AnswerResponseDto;
import com.springboot.answer.repository.AnswerRepository;
import com.springboot.auth.userdetailservice.UsersDetailService;
import com.springboot.auth.utils.CustomUserDetails;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.question.dto.QuestionResponseDto;
import com.springboot.question.entity.Question;
import com.springboot.question.mapper.QuestionMapper;
import com.springboot.question.repository.QuestionRepository;
import com.springboot.user.entity.User;
import com.springboot.user.repository.UserRepository;
import com.springboot.user.service.UserService;
import com.springboot.utils.CheckUserRoles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserService userService;
    private final CheckUserRoles checkUserRoles;
    private final QuestionMapper questionMapper;

    public QuestionService(QuestionRepository questionRepository,
                           AnswerRepository answerRepository,
                           UserService userService,
                           CheckUserRoles checkUserRoles,
                           QuestionMapper questionMapper) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.userService = userService;
        this.checkUserRoles = checkUserRoles;
        this.questionMapper = questionMapper;
    }

    public Question createQuestion(Question question) {

        checkUserRoles.isUser();

        userService.verifyExistsEmail(question.getUser().getEmail());

        return questionRepository.save(question);
    }

    public QuestionResponseDto findQuestion(Long questionId, Long userId, CustomUserDetails customUserDetails) {

        Question question = findVerifiedQuestion(questionId);

        checkSecretQuestion(question, userId, customUserDetails);

        if(question.getQuestionVisibility() == Question.QuestionVisibility.QUESTION_SECRET) {
            boolean isAuthor = userId.equals(customUserDetails.getUserId());
            boolean isAdmin = checkUserRoles.isAdmin();
            if (!(isAuthor || isAdmin)) {
                throw new BusinessLogicException(ExceptionCode.QUESTION_FORBIDDEN);
            }
        }

        question.setViewCount(question.getViewCount() + 1);

        questionRepository.save(question);

        // Dto를 어떻게 구성할지 결정하는 로직이 포함될 때 Service에서 처리해도 가능 !
        AnswerResponseDto answer = answerRepository.findAnswerByQuestion(question.getQuestionId())
                .map(questionMapper::answerToAnswerResponseDto)
                .orElse(null);

        QuestionResponseDto responseDto = questionMapper.questionToQuestionResponseDto(question);
        if(answer != null) {
            // Collections.singletonList -> 불변의 리스트를 생성하는 메서드
            // 항상 하나의 답변만 담긴 리스트를 반환
            responseDto.setAnswers(Collections.singletonList(answer));
        }

        return responseDto;
    }

    public Page<Question> findQuestions(int page, int size) {
        // Secret Qeustion 은 조회되면 안됨

        // 전체 조회 時 SECRET Question "비공개글 입니다" 로 변환 -> convertToResponseDto 사용 해야함

        Page<Question> questionPage = questionRepository.findByQuestionStatusNotIn(
                Arrays.asList(
                        Question.QuestionStatus.QUESTION_DELETED,
                        Question.QuestionStatus.QUESTION_DEACTIVED
                ),
                PageRequest.of(page - 1, size, Sort.by("questionId").descending()));

        return questionPage;
    }

    public void deleteQuestion(long questionId) {
        // 작성자가 User인지 확인하는 메서드 (사용 안해도 됨)
//        checkUserRoles.isUser();

        // 질문을 등록 한 사용자만 삭제 가능
        Question question = findVerifiedQuestion(questionId);

        question.setQuestionStatus(Question.QuestionStatus.QUESTION_DELETED);

        questionRepository.save(question);
    }

    public Question updateQuestion(Long questionId, Long userId, Question question) {

        checkUserRoles.isUser();

        Question findQuestion = findVerifiedQuestion(questionId);

        if (findQuestion.getUser() == null || !findQuestion.getUser().getUserId().equals(userId)) {
            throw new BusinessLogicException(ExceptionCode.QUESTION_FORBIDDEN);
        }

        if (findQuestion.getQuestionStatus() == Question.QuestionStatus.QUESTION_ANSWERED) {
            throw new BusinessLogicException(ExceptionCode.QUESTION_FORBIDDEN);
        }

        Optional.ofNullable(question.getTitle())
                .ifPresent(findQuestion::setTitle);
        Optional.ofNullable(question.getQuestionContext())
                .ifPresent(findQuestion::setQuestionContext);

        return questionRepository.save(question);
    }

    // LikeCount를 증가 시키는 메서드 생성
    public void addLikeCount(Question question) {
        question.setLikeCount(question.getLikeCount() + 1);
        questionRepository.save(question);
    }

    // LikeCount를 감소 시키는 메서드 생성
    public void decreaseCount(Question question) {
        question.setLikeCount(question.getLikeCount() - 1);
        questionRepository.save(question);
    }

    // 질문이 존재하는지 확인하는 메서드 생성
    public Question findVerifiedQuestion(Long questionId) {
        Optional<Question> optionalQuestion =
                questionRepository.findById(questionId);
        Question findQuestion =
                optionalQuestion.orElseThrow(() ->
                        new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND));

        return findQuestion;
    }

    // Secret Question일 경우 작성자와 ADMIN만 확인 가능
    public void checkSecretQuestion(Question question, Long userId, CustomUserDetails customUserDetails) {

        if(question.getQuestionVisibility() == Question.QuestionVisibility.QUESTION_SECRET) {
            boolean isAuthor = userId.equals(customUserDetails.getUserId());
            boolean isAdmin = checkUserRoles.isAdmin();
            if (!(isAuthor || isAdmin)) {
                throw new BusinessLogicException(ExceptionCode.QUESTION_FORBIDDEN);
            }
        }
    }

    private QuestionResponseDto convertToResponseDto(Question question) {

        QuestionResponseDto responseDto = questionMapper.questionToQuestionResponseDto(question);

        if (question.getQuestionVisibility() == Question.QuestionVisibility.QUESTION_SECRET) {
            responseDto.setTitle("비공개 질문입니다.");
        }
        return responseDto;
    }


}
