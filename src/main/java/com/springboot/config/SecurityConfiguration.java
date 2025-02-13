package com.springboot.config;

import com.springboot.auth.handler.UserAccessDeniedHandler;
import com.springboot.auth.handler.UserAuthenticationEntryPoint;
import com.springboot.auth.utils.AuthorityUtils;
import com.springboot.auth.filter.JwtAuthenticationFilter;
import com.springboot.auth.filter.JwtVerificationFilter;
import com.springboot.auth.handler.UserAuthenticationFailureHandler;
import com.springboot.auth.handler.UserAuthenticationSuccessHandler;
import com.springboot.auth.jwt.JwtTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfiguration {
    private final JwtTokenizer jwtTokenizer;
    private final AuthorityUtils authorityUtils;

    public SecurityConfiguration(JwtTokenizer jwtTokenizer,
                                 AuthorityUtils authorityUtils) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
    }

    @Bean
    // filterChain으로 메서드를 연결해서 사용
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // h2 콘솔 사용을 위해 동일 출처에서만 허용
                .headers().frameOptions().sameOrigin()
                .and()
                // csrf 보호 비활성화
                .csrf().disable()
                // CORS 설정 적용(withDefaults() 사용하여 기본 CORS 설정 적용)
                .cors(withDefaults())
                // 세션을 사용하지 않고 STATELESS 정책 적용
                // SpringSecurity에서 기본적으로 세션을 만들기 때문에 사용하지 않겠다는 의미임.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // formLogin(기본 로그인) 방식 비활성화
                .formLogin().disable()
                // HTTP 기본 인증 방식 비활성화
                .httpBasic().disable()
                // 예외 처리 설정
                .exceptionHandling()
                // 인증 실패 時 처리 할 EntryPoint 설정 (401 Unauthorized)
                .authenticationEntryPoint(new UserAuthenticationEntryPoint())
                // 접근 거부(403 FORBIDDEN) 時 처리 할 핸들러 설정
                .accessDeniedHandler(new UserAccessDeniedHandler())
                .and()
                // CustomFilter 추가
                .apply(new CustomFilterConfigurer())
                .and()
                // 요청별 권한 설정
                .authorizeHttpRequests(authorize ->authorize
                        // 로그인(토큰 발급) 요청은 모두 허용
                        .antMatchers(HttpMethod.POST, "/v11/auth/login").permitAll() // 로그인 요청 허용
                        // 회원가입 요청 모두 허용
                        .antMatchers(HttpMethod.POST, "/*/members").permitAll()
                        // 회원 정보 수정 (USER만 가능)
                        .antMatchers(HttpMethod.PATCH, "/*/members/**").hasRole("USER")
                        // 회원 목록 조회 (ADMIN만 가능)
                        .antMatchers(HttpMethod.GET, "/*/members").hasRole("ADMIN")
                        // 특정 회원 조회 (USER, ADMIN 가능)
                        // 이 설정은 모든 회원의 정보를 열람할 수 있도록 허용한 것이 아니라, 해당 엔드포인트를 호출할 권한만 부여한 것임.
                        .antMatchers(HttpMethod.GET, "/*/members/**").hasAnyRole("USER", "ADMIN")
                        // 회원 삭제 (USER만 가능)
                        .antMatchers(HttpMethod.DELETE, "/*/members/**").hasRole("USER")

                        // 주문 관련 권한 설정
                        // 주문 생성 (모든 사용자 접근 허용 (비회원도 가능 !))
                        .antMatchers(HttpMethod.POST, "/*/orders").hasRole("USER")
                        // 주문 수정 (USER, ADMIN 둘 다허용)
                        .antMatchers(HttpMethod.PATCH, "/*/orders/**").hasAnyRole("USER", "ADMIN")
                        // 주문 목록 조회 (ADMIN만 가능)
                        .antMatchers(HttpMethod.GET, "/*/orders").hasRole("ADMIN")
                        // 특정 주문 조회 (USER, ADMIN 둘 다 가능)
                        .antMatchers(HttpMethod.GET, "/*/orders/**").hasAnyRole("USER", "ADMIN")
                        // 주문 취소 (USER만 가능)
                        .antMatchers(HttpMethod.DELETE, "/*/orders/**").hasRole("USER")

                        // 커피 관련 권한 설정
                        // 커피 생성 (ADMIN만 가능)
                        .antMatchers(HttpMethod.POST, "/*/coffees").hasRole("ADMIN")
                        // 커피 정보 수정 (ADMIN만 가능)
                        .antMatchers(HttpMethod.PATCH, "/*/coffees/**").hasRole("ADMIN")
                        // Coffee 전체 조회 (모두 허용 (비회원도 조회 가능))
                        .antMatchers(HttpMethod.GET, "/*/coffees").permitAll()
                        // 특정 Coffee 조회 (모두 허용 (비회원도 특정 커피 조회 가능))
                        .antMatchers(HttpMethod.GET, "/*/coffees/**").permitAll()
                        // 특정 Coffee 삭제 (ADMIN만 가능)
                        .antMatchers(HttpMethod.DELETE, "/*/coffees/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                );
        return http.build();
    }


    @Bean
    // 비밀번호 암호화 기능
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    CorsConfigurationSource configurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList("*")); // 모든 출처 허용

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE")); // 허용 할 HTTP 메서드 설정

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해서 CORS 설정 적용

        return source;
    }

    // JWT 인증을 위한 커스텀 필터 설정 Class
    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {
        @Override
        public void configure(HttpSecurity builder) throws Exception {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);

            JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenizer);
            // 로그인 경로 설정
            jwtAuthenticationFilter.setFilterProcessesUrl("/v11/auth/login");
            // 인증 성공 時 핸들러
            jwtAuthenticationFilter.setAuthenticationSuccessHandler(new UserAuthenticationSuccessHandler());
            // 인증 실패 時 핸들러
            jwtAuthenticationFilter.setAuthenticationFailureHandler(new UserAuthenticationFailureHandler());

            // JWT 검증 필터 설정
            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils);

            // FilterChain에 추가 (인증필터 後 검증 필터 실행)
            builder.addFilter(jwtAuthenticationFilter)
                    .addFilterAfter(jwtVerificationFilter, JwtAuthenticationFilter.class);
        }
    }

}
