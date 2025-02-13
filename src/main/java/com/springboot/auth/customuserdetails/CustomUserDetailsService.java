package com.springboot.auth.customuserdetails;

import com.springboot.auth.utils.AuthorityUtils;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.user.entity.User;
import com.springboot.user.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
// 사용자의 인증 정보를 DB에서 조회하는 Class
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final AuthorityUtils authorityUtils; // 사용자 권한을 생성하는 유틸 Class

    // DI 생성자 주입
    public CustomUserDetailsService(UserRepository userRepository, AuthorityUtils authorityUtils) {
        this.userRepository = userRepository;
        this.authorityUtils = authorityUtils;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // DB에서 이메일로 회원 정보 조회
        Optional<User> optionalMember = userRepository.findByEmail(username);
        // 사용자가 존재하지 않다면 예외를 발생시킴
        User findUser = optionalMember.orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

        // 조회된 회원 정보를 기반으로 UserDetails 객체 생성
        return new UserDetail(findUser);
    }

    // 인증된 사용자 정보를 저장하기 위한 Inner Class
    // UserDetails 인터페이스를 구현하여 사용자의 정보를 제공한다.
    private final class UserDetail extends User implements UserDetails {
        UserDetail(User user) {
            // 부모 Class 필드값 설정
            setUserId(user.getUserId());
            setEmail(user.getEmail());
            setPassword(user.getPassword());
            setRoles(user.getRoles());
        }

        @Override
        // 사용자의 권한 정보를 반환하는 메서드
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorityUtils.createAuthorities(this.getRoles());
        }

        @Override
        // 사용자의 고유 식별자를 반환하는 메서드
        public String getUsername() {
            return getEmail();
        }

        @Override
        // 계정이 만료되지 않았는지 확인하는 메서드
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        // 계정이 잠겨있지 않은지 확인하는 메서드
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        // 사용자 인증정보가 만료되지 않았는지 확인하는 메서드
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        // 계정이 활성화 되어있는지 확인하는 메서드
        public boolean isEnabled() {
            return true;
        }
    }
}
