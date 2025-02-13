package com.springboot.auth.utils;

import com.springboot.auth.customuserdetails.CustomUserDetailsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    public static String getCurrentUserEmail() {
        // 1. 현재 로그인 한 사용자의 보안 Context를 가져옴
        // 2. 보안 컨텍스트에서 현재 인증 객체 가져옴
        // 3. 인증 된 사용자의 주체(principal) 가져오기
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // instanceof -> 객체 타입을 확인하는 역할을 한다
        if (principal instanceof UserDetails) {
            // principal을 UserDetails 형태로 변환(username -> email)
            return ((UserDetails) principal).getUsername();
        } else {
            // UserDetails가 아닐 時 문자열로 변환하여 반환
            return principal.toString();
        }
    }
}
