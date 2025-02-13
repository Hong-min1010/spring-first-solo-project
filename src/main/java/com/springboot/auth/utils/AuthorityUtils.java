package com.springboot.auth.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthorityUtils {
    // Value는 application.yml 설정 파일에 정의된 경로의 값을 읽어와서 필드에 주입함
    // 여기선 application.yml 안에 mail.address.admin 값을 필드에 주입
    @Value("${mail.address.admin}")
    private String adminMailAddress;

    // GrantedAuthority란 Spring Security에서 인증(Authentication)된 사용자에게 부여된 권한이나 역할을 표현하는 인터페이스
    // 이는 사용자가 어떤 작업이나 자원에 접근할 수 있는지를 결정하는 데 사용
    // Spring Security 에서는 역할을 표현할 때 일반적으로 앞에 ROLE_을 붙여서 사용함

    // 관리자의 권한 목록을 저장하는 상수, final로 선언해서 한번 초기화 하면 변경 불가능함
    // Spring Security의 AuthorityUtils를 사용해서 문자열 "ROLE_ADMIN", "ROLE_USER"
    // 저 두 문자열을 기반으로 GrantedAuthority 객체 리스트를 생성
    // 즉, 관리자는 관리자 권한, 일반 사용자 권한 두개를 가지게 됨
    private final List<GrantedAuthority> ADMIN_ROLES = org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER");

    // 일반 유저 권한 부여
    private final List<GrantedAuthority> USER_ROLES = org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_USER");

    // 관리자의 역할을 문자열 형태로 저장한 리스트
    private final List<String> ADMIN_ROLES_STRING = List.of("ADMIN", "USER");

    // 사용자의 역할을 문자열 형태로 저장한 리스트
    private final List<String> USER_ROLES_STRING = List.of("USER");

    // 이메일 주소를 입력받아서 그에 해당하는 List<GrantedAuthority> (즉, 권한 목록) 반환하는 메서드
    public List<GrantedAuthority> createAuthorities(String email) {
        // 입력받은 이메일과 @Value로 주입된 adminMailAddress를 비교
        // 만약 두 이메일이 같다면 관리자 권한 return
        if (email.equals(adminMailAddress)) {
            return ADMIN_ROLES;
        } // 아닐 경우 전부 일반 사용자 권한 return
        return USER_ROLES;
    }

    // 문자열 형태의 역할 리스트(ex. ["ADMIN, USER"], "USER")를 GrantedAuthority 객체 리스트로 변환하는 메서드
    public List<GrantedAuthority> createAuthorities(List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                // stream을 돌면서 각 역할 문자열에 대해서 ROLE_ 접두사를 붙여서
                // 새로운 SimpleGrantedAuthority 객체를 생성 (ex. "ADMIN" -> "ROLE_ADMIN")
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        // 변환된 권한 목록 반환
        return authorities;
    }

    // 이메일을 기반으로 역할(문자열 형태)을 결정하여 반환하는 메서드
    public List<String> createRoles(String email) {
        // 만약 관리자라면 ["ADMIN", "USER"]를 가지는 리스트 ADMIN_ROLES_STRING 반환
        if (email.equals(adminMailAddress)) {
            return ADMIN_ROLES_STRING;
        } // 그 외에는 전부 USER_ROLES_STRING
        return USER_ROLES_STRING;
    }
}
