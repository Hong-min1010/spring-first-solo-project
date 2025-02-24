package com.springboot.like.repository;

import com.springboot.like.entity.Like;
import com.springboot.question.entity.Question;
import com.springboot.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    // Like는 user와 question이 매핑되기 때문에 메서드 추가
    Optional<Like> findByUserAndQuestion(User user, Question question);
    List<Like> findByQuestion(Question question);
}
