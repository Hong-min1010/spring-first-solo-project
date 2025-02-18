package com.springboot.answer.service;

import com.springboot.answer.dto.AnswerResponseDto;
import com.springboot.answer.entity.Answer;
import com.springboot.answer.repository.AnswerRepository;
import com.springboot.auth.utils.CustomUserDetails;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.question.entity.Question;
import com.springboot.question.repository.QuestionRepository;
import com.springboot.question.service.QuestionService;
import com.springboot.user.service.UserService;
import com.springboot.utils.CheckUserRoles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final CheckUserRoles checkUserRoles;
    private final QuestionService questionService;
    private final UserService userService;
    private final QuestionRepository questionRepository;

    // Answer = Admin만 작성할 수 있음
    public AnswerService(AnswerRepository answerRepository,
                         QuestionRepository questionRepository,
                         CheckUserRoles checkUserRoles,
                         QuestionService questionService,
                         UserService userService) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.checkUserRoles = checkUserRoles;
        this.questionService = questionService;
        this.userService = userService;
    }

    public Answer createAnswer(Answer answer, Long questionId, CustomUserDetails customUserDetails) {

        if (!checkUserRoles.isAdmin()) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }

        Question question = questionService.findVerifiedQuestion(questionId);

        if (question.getQuestionStatus() == Question.QuestionStatus.QUESTION_ANSWERED) {
            throw new BusinessLogicException(ExceptionCode.ANSWER_ALREADY_EXISTS);
        }

        question.setQuestionStatus(Question.QuestionStatus.QUESTION_ANSWERED);


        answer.setQuestion(question);
        answer.setUser(userService.findVerifiedUser(customUserDetails.getUserId()));


        return answerRepository.save(answer);
    }

    public void deleteAnswer(Long questionId) {
        checkUserRoles.isAdmin();

        Question question = questionService.findVerifiedQuestion(questionId);

        Answer answer = question.getAnswer();

        if (answer == null) {
            throw new BusinessLogicException(ExceptionCode.ANSWER_NOT_FOUND);
        }

        answerRepository.delete(answer);
        answerRepository.flush();
    }

    public Answer updateAnswer(Long questionId, Answer patchAnswer, CustomUserDetails customUserDetails) {

        checkUserRoles.isAdmin();

        Question question = questionService.findVerifiedQuestion(questionId);

        Answer answer = question.getAnswer();

        if (answer == null) {
            throw new BusinessLogicException(ExceptionCode.ANSWER_NOT_FOUND);
        }

        if (patchAnswer.getAnswerContext() != null) {
            answer.setAnswerContext(patchAnswer.getAnswerContext());
        }

        return answerRepository.save(answer);
    }

    public AnswerResponseDto convertToAnswerResponseDto(Answer answer) {
        return new AnswerResponseDto(answer.getAnswerId(),
                answer.getUser().getUserId(),
                answer.getUser().getName(),
                answer.getAnswerContext(),
                answer.getQuestion().getQuestionId() );
    }

}
