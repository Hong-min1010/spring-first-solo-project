package com.springboot.auth.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
// 검증이 성공했을 時 작동하는 Handler
public class UserAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override

    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 검증에 성공했을 時 log에 "# Authenticated successfully!"를 logging.
        log.info("# Authenticated successfully!");
    }
}
