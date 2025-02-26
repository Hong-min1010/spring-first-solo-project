package com.springboot.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.auth.dto.LoginDto;
import com.springboot.auth.jwt.JwtTokenizer;
import com.springboot.user.entity.User;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// 사용자의 로그인 요청을 처리하고 JWT 토큰을 생성하는 Class
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenizer jwtTokenizer;

    // DI 생성자 주입
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenizer jwtTokenizer) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenizer = jwtTokenizer;
        setFilterProcessesUrl("/v1/login");
    }

    @SneakyThrows // Throws를 쓰지 않아도 자동으로 예외를 thorw하는 에너테이션
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) {
        // 요청 본문(JSON)에서 로그인 정보를 읽어와 LoginDto 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);

        // 사용자의 ID, PW를 포함한 Authentication 객체생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());
        // AuthenticationManager를 사용하여 인증을 시도하고 결과 (Authentication)를 반환
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws ServletException, IOException {
        // 인증된 사용자 정보 가져오기(SpringSecurity에서 User 객체로 캐스팅)
        User user = (User) authResult.getPrincipal();

        // JWT AccessToken 생성
        String accessToken = delegateAccessToken(user);
        // JWT RefreshToken 생성
        String refreshToken = delegateRefreshToken(user);
        // 응답 Header에 AccessToken과 Refresh Token 추가
        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Refresh", refreshToken);

        // SpringSecurity의 성공 Handler실행
        this.getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
    }

    // 사용자의 정보를 기반으로 Access Token을 생성하는 메서드
    private String delegateAccessToken(User user) {
        // JWT의 Payload(데이터) 설정 claims -> Payload
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getEmail()); // 토큰에 사용자 이메일 포함
        claims.put("roles", user.getRoles()); // ★토큰에 사용자 권한 포함★

        String subject = user.getEmail();
        // 토큰 만료시간 설정
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());
        // JWT 서명을 위한 비밀키를 base64로 인코딩
        String base64EncodedSecretKey = jwtTokenizer.encodedBase64SecretKey(jwtTokenizer.getSecretKey());
        // Access Token 생성하여 반환
        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);

        return accessToken;
    }
    // 사용자의 정보를 기반으로 Refresh 토큰을 생성하는 메서드
    private String delegateRefreshToken(User user) {
        // Email
        String subject = user.getEmail();
        // 만료시간
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());
        // JWT 서명을 위한 비밀키를 Base64로 인코딩
        String base64EncodedSecretKey = jwtTokenizer.encodedBase64SecretKey(jwtTokenizer.getSecretKey());
        // Refresh 토큰을 생성하여 반환
        String refreshToken = jwtTokenizer.generateRefreshToken(subject, expiration, base64EncodedSecretKey);

        return refreshToken;
    }

}
