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

    // Answer = Admin만 작성할 수 있음
    public AnswerService(AnswerRepository answerRepository,
                         CheckUserRoles checkUserRoles,
                         QuestionService questionService,
                         UserService userService) {

        this.answerRepository = answerRepository;
        this.checkUserRoles = checkUserRoles;
        this.questionService = questionService;
        this.userService = userService;
    }

    public Answer createAnswer(Answer answer, Long questionId, CustomUserDetails customUserDetails) {

        // 현재 로그인 한 회원이 관리자 계정인지 확인하는 로직
        // 관리자 계정이 아니라면 Exception 발생
        if (!checkUserRoles.isAdmin()) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }
        
        // 답변을 하려는 질문이 존재하는지 확인하고 새로운 Question 객체에 할당
        Question question = questionService.findVerifiedQuestion(questionId);

        // Question이 이미 답변이 작성되어 있다면 Exception 발생
        // 현재 문서에는 1개의 Question에 1개의 Answer만 존재할 수 있기때문
        if (question.getQuestionStatus() == Question.QuestionStatus.QUESTION_ANSWERED) {
            throw new BusinessLogicException(ExceptionCode.ANSWER_ALREADY_EXISTS);
        }

        // Question에 Answer를 작성 했다면 Question의 상태를 QUESTION_ANSWERED로 변경
        question.setQuestionStatus(Question.QuestionStatus.QUESTION_ANSWERED);

        // Question이 비밀글로 작성 되었다면 Answer도 보이지 않게 설정
        if (question.getQuestionVisibility() == Question.QuestionVisibility.QUESTION_SECRET) {
            answer.setAnswerContext("비공개 답변입니다.");
        }


        answer.setQuestion(question);
        answer.setUser(userService.findVerifiedUser(customUserDetails.getUserId()));


        return answerRepository.save(answer);
    }

    public void deleteAnswer(Long questionId) {
        // 현재 로그인 한 사용자가 관리자 계정인지 확인하는 메서드
        checkUserRoles.isAdmin();

        // Question이 존재하는지 확인 후 새로운 Question 객체에 할당
        Question question = questionService.findVerifiedQuestion(questionId);

        // 해당 Question의 Answer를 가져와서 새로운 Answer 객체에 저장
        Answer answer = question.getAnswer();

        // Answer가 작성이 안되어 있다면 Exception 발생시킴
        if (answer == null) {
            throw new BusinessLogicException(ExceptionCode.ANSWER_NOT_FOUND);
        }

        // Answer를 Repository에서 삭제
        answerRepository.delete(answer);
        // 현재까지 변경 된 Entity를 즉시 DataBase에 반영하는 SQL 실행
        answerRepository.flush();
    }

    public Answer updateAnswer(Long questionId, Answer patchAnswer, CustomUserDetails customUserDetails) {

        // 현재 로그인 한 사용자가 관리자 계정인지 확인하는 메서드
        checkUserRoles.isAdmin();
        // Question이 존재하는지 확인 후 새로운 Question 객체에 할당
        Question question = questionService.findVerifiedQuestion(questionId);

        // 해당 Question의 Answer를 가져와 새로운 Answer 객체에 저장
        Answer answer = question.getAnswer();
        // Answer가 없다면 Exception 발생 시킴
        if (answer == null) {
            throw new BusinessLogicException(ExceptionCode.ANSWER_NOT_FOUND);
        }
        // patch요청에서 answerContext가 입력 된 경우 업데이트
        // 아니라면 기존 Answer 가져오기
        if (patchAnswer.getAnswerContext() != null) {
            answer.setAnswerContext(patchAnswer.getAnswerContext());
        }

        return answerRepository.save(answer);
    }

}
