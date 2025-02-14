package com.springboot.utils;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.user.entity.User;
import com.springboot.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CheckUserRoles {
    // 로그인 한 사용자와 수정하고싶은 user의 Id 를 비교하는 메서드
    private void matchUserId(Long userId, Long currentUserId) {

        if (!userId.equals(currentUserId)) {
            throw new BusinessLogicException(ExceptionCode.USER_FORBIDDEN);
        }

    }

    public boolean isAdmin () {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }

    // User인지 검증하는 메서드 (ADMIN 제외)
    public boolean isUser () {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (roles.contains("ROLE_ADMIN")) {
            return false;
        }

        return roles.contains("ROLE_USER");
    }

}
