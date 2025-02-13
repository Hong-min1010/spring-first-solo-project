package com.springboot.auth.filter;

import com.springboot.auth.utils.AuthorityUtils;
import com.springboot.auth.jwt.JwtTokenizer;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

// JwtVerificationFilter는 클라이언트가 보낸 JWT를 검증하는 필터이다
// OncePerRequestFilter를 상속 받음으로써 [모든 요청에서 한번만 실행] 되도록 설정한다
public class JwtVerificationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;
    private final AuthorityUtils authorityUtils;

    public JwtVerificationFilter(JwtTokenizer jwtTokenizer, AuthorityUtils authorityUtils) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
    }

    // 예외 처리하는 메서드
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 요청에서 JWT를 검증하고, Payload(claims)를 가져옴
            Map<String, Object> claims = verifyJws(request);
            // 인증 정보를 SecurityContext에 저장한다
            setAuthenticationToContext(claims);
        } catch (SignatureException se) { // JWT 서명이 유효하지 않을 경우
            request.setAttribute("exception", se);
        } catch (ExpiredJwtException ee) { // JWT가 만료된경우
            request.setAttribute("exception", ee);
        } catch (Exception e) { // 기타 예외
            request.setAttribute("exception", e);
        }
        filterChain.doFilter(request, response);
    }

    @Override
    // 특정 요청에 대해 Filter를 실행할지 여부를 결정하는 메서드
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String authorization = request.getHeader("Authorization");

        return authorization == null || !authorization.startsWith("Bearer");
    }

    // 요청에서 JWT를 추출하고, 서명을 검증한 후 Payload 정보를 반환하는 메서드
    private Map<String, Object> verifyJws(HttpServletRequest request) {
        // Athuorization Header에서 "Bearer"를 제거하고 JWT 추출
        String jws = request.getHeader("Authorization").replace("Bearer", "");
        // 비밀 키 (Base64 인코딩 된 형태)
        String base64EncodedSecretKey = jwtTokenizer.encodedBase64SecretKey(jwtTokenizer.getSecretKey());

        // JWT 검증 및 디코딩하여 Payload(claims) 정보를 가져옴
        Map<String, Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();

        return claims;
    }
    // JWT의 Payload 정보를 이용해 SpringSecurity의 인증 객체를 생성하고
    // SecurityContextHolder에 저장하는 메서드
    private void setAuthenticationToContext(Map<String, Object> claims) {
        // Jwt에서 사용자 이름 가져오기
        String username = (String) claims.get("username");
        // JWT에서 권한 정보 가져와서 SpringSecurity의 GrantedAtuhority 리스트로 변환
        List<GrantedAuthority> authorities = authorityUtils.createAuthorities((List) claims.get("roles"));
        // 인증 객체 생성(비밀번호는 JWT기반 인증이므로 null 설정)
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
        //ContextHolder에 인증 객체 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
