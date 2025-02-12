package com.springboot.user.dto;

import com.springboot.user.entity.User;
import com.springboot.validator.NotSpace;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Setter
@NoArgsConstructor
@Getter
public class UserResponseDto {
    private Long userId;
    private String email;
    private String name;
    private User.UserStatus userStatus;
}
