package com.springboot.auth.handler;

import com.springboot.auth.utils.ErrorResponder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class UserAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 에러 응답 전송(POSTMAN) -> message : "Forbidden"
        ErrorResponder.sendErrorResponse(response, HttpStatus.FORBIDDEN);
        // warning 로그 출력 時 출력 할 문자열 지정
        log.warn("Forbidden error happend: {}", accessDeniedException.getMessage());
    }
}
