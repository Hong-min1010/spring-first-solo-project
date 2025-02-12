package com.springboot.question.repository;

import com.springboot.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    Page<Question> findByQuestionStatusNot(Question.QuestionStatus firstStatus, Question.QuestionStatus secondStatus, Pageable pageable);
}
