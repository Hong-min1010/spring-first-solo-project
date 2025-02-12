package com.springboot.like.service;

import com.springboot.like.entity.Like;
import com.springboot.like.repository.LikeRepository;
import com.springboot.question.entity.Question;
import com.springboot.question.repository.QuestionRepository;
import com.springboot.user.entity.User;
import com.springboot.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public LikeService(LikeRepository likeRepository, QuestionRepository questionRepository, UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    public Like addLike(Long userId, Long questionId) {
        // 해당 user가 있는지 확인하는 로직
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 해당 question이 있는지 확인하는 로직
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다."));


        return null;
    }
}
