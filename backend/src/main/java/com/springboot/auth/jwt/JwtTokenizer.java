package com.springboot.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Component
// 토큰을 생성, 검증하는 Class
public class JwtTokenizer {
    @Getter
    // yml 파일에 정의된 Key값을 주입받아 secretKey로 사용함
    @Value("${jwt.key}")
    private String secretKey;

    @Getter
    // yml 파일에서 jwt.access-token-expiration-minutes 값을 주입받아 Access Token의 만료 시간 설정
    @Value("${jwt.access-token-expiration-minutes}")
    private int accessTokenExpirationMinutes;

    @Getter
    // yml 파일에서 jwt.refresh-token-expiration-minutes 값을 주입받아 Refresh Token의 만료 시간 설정
    @Value("${jwt.refresh-token-expiration-minutes}")
    private int refreshTokenExpirationMinutes;

    // encodedBase64SecretKey -> secretKey를 Base64로 인코딩하여 반환하는 메서드
    public String encodedBase64SecretKey(String secretKey) {
        return Encoders.BASE64.encode(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token을 생성하는 메서드 (payload, subject, Access Token 유효 시간, base64로 인코딩 된 비밀번호)
    public String generateAccessToken(Map<String, Object> claims,
                                      String subject,
                                      Date expiration,
                                      String base64EncodedSecretKey) {
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        return Jwts.builder()
                .setClaims(claims) // 토큰에 포함 할 데이터 설정
                .setSubject(subject) // 토큰의 주제 설정
                .setIssuedAt(Calendar.getInstance().getTime()) // 토큰 발급시간을 현재시간 기준으로 생성
                .setExpiration(expiration) // Access 토큰의 만료 시간 설정
                .signWith(key) // secretKey를 사용해 서명 추가
                .compact(); // JWT 문자열을 생성하여 반환
    }

    // Refresh Token을 생성하는 메서드
    public String generateRefreshToken(String subject,
                                       Date expiration,
                                       String base64EncodedSecretKey) {
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        return Jwts.builder()
                .setSubject(subject) // 토큰의 주제 설정
                .setIssuedAt(Calendar.getInstance().getTime()) // 현재 시간을 기준으로 토큰 생성시간 설정
                .setExpiration(expiration) // Refresh 토큰의 만료 시간 설정
                .signWith(key) // secretKey를 사용해 서명 추가
                .compact(); // JWT 문자열을 생성하여 반환
    }

    // Claims를 추출하는 메서드
    public Jws<Claims> getClaims(String jws, String base64EncodedSecretKey) {
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);
        // ★★ parserBuilder를 통해 서명을 검증하여, 토큰이 변조되지 않았음을 확인 ★★

        // parserBuilder 사용(검증 時) : 클라이언트가 서버에 보낸 JWT가 유효한지 확인
        // -> 서명이 올바른지, 토큰이 만료되었는지, 토큰의 구조가 올바른지 등 확인한다

        // parserBuilder 사용 (추출 時) : 토큰에 포함 된 사용자 정보나 기타 데이터를 읽어야할 때 사용
        Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(key) // 서명을 검증하기 위한 KEY 생성
                .build()
                .parseClaimsJws(jws); // JWT 파싱 후 서명을 검증하고 Claim 정보 반환

        return claims;
    }

    // JWT 서명을 검증하는 메서드
    public void verifySignature(String jws, String base64EncodedSecretKey) {
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        Jwts.parserBuilder()
                .setSigningKey(key) // 서명을 검증 할 Key 생성
                .build()
                .parseClaimsJws(jws); // JWT 파싱 후 서명을 검증 return (X)

    }

    // 토큰의 만료 시간을 계산하는 메서드
    public Date getTokenExpiration(int expirationMinutes) {
        Calendar calendar = Calendar.getInstance(); // 현재 시간을 기준으로
        calendar.add(Calendar.MINUTE, expirationMinutes); // 입력받은 분 단위로 만료 시간을 더한 후 만료날짜 객체를 반환
        Date expiration = calendar.getTime();

        return expiration;
    }
    
    // Base64로 인코딩 된 Secret Key를 디코딩하여 byte 배열로 반환 後 HMAC-SHA 알고리즘에 사용 할 키 객체를 생성 후 반환
    private Key getKeyFromBase64EncodedKey(String base64EncodedSecretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return key;
    }
}
