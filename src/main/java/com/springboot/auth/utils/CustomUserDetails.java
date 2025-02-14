package com.springboot.auth.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomUserDetails {
    private Long userId;
    private String email;
}
