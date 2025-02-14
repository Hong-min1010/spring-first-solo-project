package com.springboot.user.controller;

import com.springboot.auth.userdetailservice.UsersDetailService;
import com.springboot.auth.utils.CustomUserDetails;
import com.springboot.dto.MultiResponseDto;
import com.springboot.dto.SingleResponseDto;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.user.dto.UserPatchDto;
import com.springboot.user.dto.UserPostDto;
import com.springboot.user.entity.User;
import com.springboot.user.mapper.UserMapper;
import com.springboot.user.service.UserService;
import com.springboot.utils.UriCreator;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/users")
@Validated
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
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

        User findUser = userService.findUser(userId);

        Long foundUserId = findUser.getUserId();

        Long currentUserId = customUserDetails.getUserId();

//        User findUser = userService.findVerifiedUser(userId);

        if (foundUserId != currentUserId) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }


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

        User findUser = userService.findUser(userId);

        Long findUserId = findUser.getUserId();

        Long currentUserId = customUserDetails.getUserId();

        if(findUserId != currentUserId) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }

        return new ResponseEntity(
                new SingleResponseDto<>(userService.findUser(userId)),
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity getUsers(@Positive @RequestParam int page,
                                   @Positive @RequestParam int size) {
        Page<User> pageUsers = userService.findUsers(page - 1, size);
        List<User> users = pageUsers.getContent();
        return new ResponseEntity(
                new MultiResponseDto<>(userMapper.usersToUsersResponses(users), pageUsers),
                HttpStatus.OK);
    }

    @DeleteMapping("/{user-id}")
    public ResponseEntity deleteUser(@PathVariable ("user-id") @Positive Long userId) {
        userService.deleteUser(userId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
