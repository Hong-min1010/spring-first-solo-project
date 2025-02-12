package com.springboot.user.dto;

import com.springboot.user.entity.User;
import com.springboot.validator.NotSpace;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Setter
@NoArgsConstructor
@Getter
public class UserPostDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String name;
}
