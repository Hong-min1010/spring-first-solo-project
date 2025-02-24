package com.springboot.user.dto;

import com.springboot.user.entity.User;
import com.springboot.validator.NotSpace;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
public class UserPatchDto {

    @Positive
    private Long userId;

    @Email
    private String email;

    @NotSpace(message = "회원 이름은 공백이 아니어야 합니다.")
    private String name;

    private User.UserStatus userStatus;
}
