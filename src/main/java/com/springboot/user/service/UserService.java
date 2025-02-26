package com.springboot.user.service;

import com.springboot.auth.utils.AuthorityUtils;
import com.springboot.auth.utils.CustomUserDetails;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.helper.event.UserRegistrationApplicationEvent;
import com.springboot.user.entity.User;
import com.springboot.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Transactional
@Service
public class UserService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityUtils authorityUtils;

    public UserService(UserRepository userRepository, ApplicationEventPublisher publisher, PasswordEncoder passwordEncoder, AuthorityUtils authorityUtils) {
        this.userRepository = userRepository;
        this.publisher = publisher;
        this.passwordEncoder = passwordEncoder;
        this.authorityUtils = authorityUtils;
    }

    public User createUser(User user) {
        verifyExistsEmail(user.getEmail());

        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);

        List<String> roles = authorityUtils.createRoles(user.getEmail());
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        publisher.publishEvent(new UserRegistrationApplicationEvent(this, savedUser));

        return savedUser;
    }

    public User updateUser(Long userId, User user) {

         User existingUser = findVerifiedUser(userId);


//        String currentUserEmail = getCurrentUserEmail();

        // 현재 로그인 한 사용자의 Email과 수정하려는 사용자의 Email이 같은지 확인
//        if (!findUser.getEmail().equals(currentUserEmail)) {
//            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
//        }

        Optional.ofNullable(user.getEmail())
                .ifPresent(email -> existingUser.setEmail(email));
        Optional.ofNullable(user.getName())
                .ifPresent(name -> existingUser.setName(name));

        return userRepository.save(existingUser);
    }

    public User findUser(Long userId) {

        return findVerifiedUser(userId);
    }

    public Page<User> findUsers (int page, int size) {

        log.info("page: {}", page);
        return userRepository.findAll(PageRequest.of(page - 1, size,
                Sort.by("userId").descending()));
    }

    public void deleteUser(Long userId) {

        User user = findVerifiedUser(userId);

        user.quitUser();

        userRepository.save(user);
    }

    // 이미 email로 가입이 되어있는 회원인지 확인(create)
    public void verifyExistsEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent())
            throw new BusinessLogicException(ExceptionCode.USER_EXISTS);
    }

    // 기존 사용자를 찾을 때 사용
    public User findVerifiedUser(Long userId) {
        Optional<User> optionalUser =
                userRepository.findById(userId);
        User findUser =
                optionalUser.orElseThrow(() ->
                        new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
        return findUser;
    }

    public void matchUserId(Long userId,CustomUserDetails customUserDetails) {

        User findUser = findVerifiedUser(userId);

        Long foundUserId = findUser.getUserId();

        Long currentUserId = customUserDetails.getUserId();

        if (!foundUserId.equals(currentUserId)) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }
    }

}
