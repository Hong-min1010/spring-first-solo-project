package com.springboot.question.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.question.entity.Question;
import com.springboot.question.repository.QuestionRepository;
import com.springboot.user.entity.User;
import com.springboot.user.repository.UserRepository;
import com.springboot.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;

@Transactional
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public QuestionService(QuestionRepository questionRepository,
                           UserRepository userRepository,
                           UserService userService) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public Question createQuestion(Question question) {
        userService.verifyExistsEmail(question.getUser().getEmail());

        return questionRepository.save(question);
    }

    public Question findQuestion(Long questionId, Long userId) {
        Question question = findVerifiedQuestion(questionId);

        // 비밀글은 작성자 아니면 못봄(수정 해야됨)
        if(question.getQuestionVisibility() == Question.QuestionVisibility.QUESTION_SECRET &&
            !question.getUser().getUserId().equals(userId)) {
            throw new BusinessLogicException(ExceptionCode.QUESTION_FORBIDDEN);
        }
        return question;
    }

    public Page<Question> findQuestions(int page, int size) {
        // findBtQuestionStatusNot -> Repository에서 생성 한 메서드
        // 제외하고 싶은 상태 제외

        return questionRepository.findByQuestionStatusNotIn(
                Arrays.asList(
                        Question.QuestionStatus.QUESTION_DELETED,
                        Question.QuestionStatus.QUESTION_DEACTIVED
                ),
                PageRequest.of(page - 1, size, Sort.by("questionId").descending()));
    }

    public void deleteQuestion(long questionId) {
        Question question = findVerifiedQuestion(questionId);

        question.setQuestionStatus(Question.QuestionStatus.QUESTION_DELETED);

        questionRepository.save(question);
    }

    public Question updateQuestion(long questionId, Long userId) {
        Question question = findVerifiedQuestion(questionId);

        if (question.getQuestionStatus() == Question.QuestionStatus.QUESTION_ANSWERED ||
                !question.getUser().getUserId().equals(userId)) {
            throw new BusinessLogicException(ExceptionCode.QUESTION_FORBIDDEN);
        }

        return question;
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

}
