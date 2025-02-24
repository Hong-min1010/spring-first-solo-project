package com.springboot.auth.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CustomUserDetails {
    private Long userId;
    private String email;
}
