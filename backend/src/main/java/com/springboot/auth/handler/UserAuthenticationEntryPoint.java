package com.springboot.auth.handler;

import com.springboot.auth.utils.ErrorResponder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
// 인증되지 않은 사용자가 보호된 리소스에 접근할 때 실행되는 Handler
// AuthenticationEntryPoint는 인증 예외를 처리하고 클라이언트에게 응답을 반환한다

public class UserAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override

    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // request객체에서 "exception" 속성을 가져옴(JWT 검증 과정에서 발생한 예외일 수 있다)
        Exception exception = (Exception) request.getAttribute("exception");
        // 클라이언트에게 401 Unauthorized 응답을 보냄
        ErrorResponder.sendErrorResponse(response, HttpStatus.UNAUTHORIZED);
        // 예외 로그 출력
        logExceptionMessage(authException, exception);
    }

    // 예외 메세지를 logging 하는 메서드
    // JWT 관련 예외가 존재하면 해당 메세지 logging
    // 그렇지 않다면 기본 AuthenticationException 메세지 logging
    private void logExceptionMessage(AuthenticationException authException, Exception exception) {
        // JWT 검증 과정에서 발생한 예외가 있으면 해당 메시지를 사용, 없으면 기본 인증 메세지 출력
        String message = exception != null ? exception.getMessage() : authException.getMessage();
        // Warning log Message 출력
        log.warn("Unauthorized error happened: {}", message);
    }
}
