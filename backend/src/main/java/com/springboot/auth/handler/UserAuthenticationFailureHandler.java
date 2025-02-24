package com.springboot.auth.handler;

import com.google.gson.Gson;
import com.springboot.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Slf4j
// 인증이 실패했을 時 실행되는 Handler
public class UserAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // 인증이 싫패했을 時 log에 Message 출력
        log.error("# Authentication failed: {}", exception.getMessage());
        // 응답으로 ErrorResponse 보내기
        sendErrorResponse(response);
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException{
        Gson gson = new Gson();
        // ErrorResponse HttpStatus.UNAUTHORIZED 상태를 가진 객체 생성
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.UNAUTHORIZED);
        // 응답 콘텐츠 타입 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 응답 상태코드 설정
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        // ErrorResponse 객체를 Json 형식으로 변환 後 Http 응답 본문에 작성하여 클라이언트에게 전송
        response.getWriter().write(gson.toJson(errorResponse, ErrorResponse.class));
    }
}
