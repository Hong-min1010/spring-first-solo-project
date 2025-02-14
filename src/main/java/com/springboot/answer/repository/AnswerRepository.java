package com.springboot.answer.repository;

import com.springboot.answer.entity.Answer;
import com.springboot.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Optional<Answer> findAnswerByQuestion(Long questionId);
}
