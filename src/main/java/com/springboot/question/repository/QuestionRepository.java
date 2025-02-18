package com.springboot.question.repository;

import com.springboot.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<Question> findWithUserByQuestionId(Long questionId);

    Page<Question> findByQuestionStatusNotIn(List<Question.QuestionStatus> statuses,
                                           Pageable pageable);
}
