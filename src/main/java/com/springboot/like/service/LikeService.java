package com.springboot.like.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
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
        User user = findVerifiedUser(userId);
        Question question = findVerifiedQuestion(questionId);

        verifyLikeNotExists(user, question);

        Like like = new Like();

        like.setUser(user);
        like.setQuestion(question);

        likeRepository.save(like);

        question.addLikeCount();

        questionRepository.save(question);

        return like;
    }

    public void removeLike(Long userId, Long questionId) {
        User user = findVerifiedUser(userId);
        Question question = findVerifiedQuestion(questionId);

        Like like = likeRepository.findByUserAndQuestion(user, question)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.LIKE_NOT_FOUND));

        likeRepository.delete(like);

        question.decreaseLikeCount();
        questionRepository.save(question);
    }

    // 해당 사용자가 존재하는지 확인하는 메서드
    public User findVerifiedUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
    }

    // 질문이 존재하는지 확인하는 메서드
    public Question findVerifiedQuestion(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND));
    }

    // 좋아요를 이미 눌렀는지 검증하는 메서드
    public void verifyLikeNotExists(User user, Question question) {
        Optional<Like> like = likeRepository.findByUserAndQuestion(user, question);

        if (like.isPresent()) {
            throw new BusinessLogicException(ExceptionCode.LIKE_ALREADY_EXISTS);
        }
    }
}
