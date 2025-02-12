package com.springboot.user.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.user.entity.User;
import com.springboot.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(Long userId) {
        findVerifiedUser(userId);
        return ;
    }

    public User updateUser(Long userId) {

        return null;
    }

    public User findUser(Long userId) {

        return null;
    }

    public User findUsers(int page, int size) {

        return null;
    }

    public void deleteUser(Long userId) {

    }
    // 가입이 되어있는 회원인지 확인하는 메서드 생성
    public void verifyExistsEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent())
            throw new BusinessLogicException(ExceptionCode.USER_NOT_FOUND);
    }

    // 중복 회원인지 검증하는 메서드 생성
    public User findVerifiedUser(Long userId) {
        Optional<User> optionalUser =
                userRepository.findById(userId);
        User findUser =
                optionalUser.orElseThrow(() ->
                        new BusinessLogicException(ExceptionCode.USER_EXISTS));
        return findUser;
    }
}
