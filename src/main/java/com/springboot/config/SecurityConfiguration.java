package com.springboot.config;

import com.springboot.auth.handler.UserAccessDeniedHandler;
import com.springboot.auth.handler.UserAuthenticationEntryPoint;
import com.springboot.auth.userdetailservice.UsersDetailService;
import com.springboot.auth.utils.AuthorityUtils;
import com.springboot.auth.filter.JwtAuthenticationFilter;
import com.springboot.auth.filter.JwtVerificationFilter;
import com.springboot.auth.handler.UserAuthenticationFailureHandler;
import com.springboot.auth.handler.UserAuthenticationSuccessHandler;
import com.springboot.auth.jwt.JwtTokenizer;
import com.springboot.user.service.UserService;
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
    private final UsersDetailService usersDetailService;

    public SecurityConfiguration(JwtTokenizer jwtTokenizer,
                                 AuthorityUtils authorityUtils,
                                 UsersDetailService usersDetailService) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
        this.usersDetailService = usersDetailService;
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
                        // USER

                        // 로그인(토큰 발급) 요청은 모두 허용
                        .antMatchers(HttpMethod.POST, "/v1/login").permitAll() // 로그인 요청 허용
                        //로그아웃 요청 모두 허용
                        .antMatchers(HttpMethod.POST, "/v1/logout").permitAll()
                        // 회원가입 요청 모두 허용
                        .antMatchers(HttpMethod.POST, "/*/users").permitAll()
                        // 회원 정보 수정 (USER만 가능)
                        .antMatchers(HttpMethod.PATCH, "/*/users/**").hasRole("USER")
                        // 회원 전체 조회 (ADMIN만 가능)
                        .antMatchers(HttpMethod.GET, "/*/users").hasRole("ADMIN")
                        // 특정 회원 조회 (ADMIN만 가능)
                        .antMatchers(HttpMethod.GET, "/*/users/**").hasAnyRole("ADMIN", "USER")
                        // 회원 삭제 (USER만 가능)
                        .antMatchers(HttpMethod.DELETE, "/**/users/**").hasAnyRole("ADMIN", "USER")

                        // QUESTION

                        // Question 생성 (회원만 가능)
                        .antMatchers(HttpMethod.POST, "/*/questions").hasRole("USER")
                        // Question 수정 (USER만 허용)
                        .antMatchers(HttpMethod.PATCH, "/*/questions/**").hasRole("USER")
                        // Question 전체 조회 (USER, ADMIN 가능)
                        .antMatchers(HttpMethod.GET, "/*/questions").hasAnyRole("USER", "ADMIN")
                        // 특정 Question 조회 (USER, ADMIN 둘 다 가능)
                        .antMatchers(HttpMethod.GET, "/*/questions/**").hasAnyRole("USER", "ADMIN")
                        // Question 삭제 (USER만 가능)
                        .antMatchers(HttpMethod.DELETE, "/*/questions/**").hasRole("USER")

                        // Answer 권한 설정

                        // Answer 생성 (ADMIN만 가능)
                        .antMatchers(HttpMethod.POST, "/*/questions/*/answers").hasRole("ADMIN")
                        // Answer 수정 (ADMIN만 가능)
                        .antMatchers(HttpMethod.PATCH, "/*/questions/*/answers/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/*/questions/*/answers/**").hasRole("ADMIN")

                        // Like

                        // Like 생성 (USER, ADMIN 가능)
                        .antMatchers(HttpMethod.POST, "/*/likes").hasAnyRole("USER", "ADMIN")

                        // 특정 like 삭제 (USER, ADMIN 가능)
                        .antMatchers(HttpMethod.DELETE, "/*/likes").hasAnyRole("USER", "ADMIN")

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
            jwtAuthenticationFilter.setFilterProcessesUrl("/v1/login");
            // 인증 성공 時 핸들러
            jwtAuthenticationFilter.setAuthenticationSuccessHandler(new UserAuthenticationSuccessHandler());
            // 인증 실패 時 핸들러
            jwtAuthenticationFilter.setAuthenticationFailureHandler(new UserAuthenticationFailureHandler());

            // JWT 검증 필터 설정
            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils, usersDetailService);

            // FilterChain에 추가 (인증필터 後 검증 필터 실행)
            builder.addFilter(jwtAuthenticationFilter)
                    .addFilterAfter(jwtVerificationFilter, JwtAuthenticationFilter.class);
        }
    }

}
