package com.springboot.user.controller;

import com.springboot.auth.userdetailservice.UsersDetailService;
import com.springboot.auth.utils.CustomUserDetails;
import com.springboot.dto.MultiResponseDto;
import com.springboot.dto.SingleResponseDto;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.user.dto.UserPatchDto;
import com.springboot.user.dto.UserPostDto;
import com.springboot.user.dto.UserResponseDto;
import com.springboot.user.entity.User;
import com.springboot.user.mapper.UserMapper;
import com.springboot.user.service.UserService;
import com.springboot.utils.CheckUserRoles;
import com.springboot.utils.UriCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/users")
@Validated
@Slf4j
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final CheckUserRoles checkUserRoles;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserController(UserService userService, UserMapper userMapper,
                          CheckUserRoles checkUserRoles, RedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.checkUserRoles = checkUserRoles;
        this.redisTemplate = redisTemplate;
    }

    // 로그아웃 구현
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                       @RequestHeader("Authorization") String authorization) {
        // JWT 토큰을 Redis에서 제거하기 위해 사용자 ID 또는 토큰을 가져옴
        String token = authorization.replace("Bearer ", "");

        // Redis에서 해당 토큰 삭제
        redisTemplate.delete(token);

        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity postUser(@Valid @RequestBody UserPostDto requestBody) {

        User user = userMapper.userPostDtoToUser(requestBody);

        User createdUser = userService.createUser(user);

        URI location = UriCreator.createUri("/v1/users",
                Optional.ofNullable(createdUser.getUserId()).
                orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_CREATED)));

        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{user-id}")
    public ResponseEntity patchUser(@PathVariable("user-id") Long userId,
                                    @Valid @RequestBody UserPatchDto requestBody,
                                    @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        userService.matchUserId(userId, customUserDetails);

        User updatedUser =
                userService.updateUser(userId, userMapper.userPatchDtoToUser(requestBody));

        return new ResponseEntity(
                // singleResponseDto -> JSON 응답을 일관된 형식으로 유지할 수 있음
                new SingleResponseDto<>(userMapper.userToUserResponseDto(updatedUser)), HttpStatus.OK
        );
    }

    @GetMapping("/{user-id}")
    public ResponseEntity getUser(@PathVariable("user-id") Long userId,
                                  @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        // 현재 로그인 한(인증 된) 사용자의 ID 가져오기
        Long currentUserId = customUserDetails.getUserId();
        //        // 현재 로그인 한 사용자의 ID와 조회 하려는 User의 ID가 같은지 확인
        // 수정 -> 전체 주석 처리 : 이 로직을 작성하게 되면 ADMIN의 ID와 USER의 ID가 맞지 않으면
        // Exception이 발생하기 때문에 주석처리 함
//        userService.matchUserId(userId, customUserDetails);

        User user = userService.findUser(userId);

        if (!checkUserRoles.isAdmin() && !currentUserId.equals(userId)) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }

        UserResponseDto responseDto = new UserResponseDto(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getUserStatus(),
                user.getRoles()
        );

        return new ResponseEntity(new SingleResponseDto<>(responseDto), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getUsers(@Positive @RequestParam int page,
                                   @Positive @RequestParam int size) {

        if (!checkUserRoles.isAdmin()) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }
        Page<User> pageUsers = userService.findUsers(page, size);
        List<User> users = pageUsers.getContent();
        return new ResponseEntity(
                new MultiResponseDto<>(userMapper.usersToUsersResponses(users), pageUsers),
                HttpStatus.OK);
    }

    @DeleteMapping("/{user-id}")
    public ResponseEntity deleteUser(@PathVariable ("user-id") @Positive Long userId,
                                     @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long currentUserId = customUserDetails.getUserId();

        if (!checkUserRoles.isAdmin() && !currentUserId.equals(userId)) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }

        userService.deleteUser(userId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
