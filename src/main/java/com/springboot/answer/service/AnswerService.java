package com.springboot.answer.service;

import com.springboot.answer.entity.Answer;
import com.springboot.answer.repository.AnswerRepository;
import com.springboot.question.repository.QuestionRepository;
import com.springboot.utils.CheckUserRoles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final CheckUserRoles checkUserRoles;

    // Answer = Admin만 작성할 수 있음
    public AnswerService(AnswerRepository answerRepository,
                         QuestionRepository questionRepository,
                         CheckUserRoles checkUserRoles) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.checkUserRoles = checkUserRoles;
    }

    public Answer createAnswer(Answer answer) {

        return null;
    }

    public void deleteAnswer(Long answerId) {

    }

    public Answer updateAnswer(Long answerId) {
        return null;
    }

    public Answer getAnswer(Long answerId) {
        return null;
    }

    public List<String> getAnswers(int page,
                                   int size) {

        return null;
    }
}
