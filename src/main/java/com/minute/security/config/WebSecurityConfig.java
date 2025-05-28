package com.minute.security.config;

import com.minute.security.filter.JwtAuthenticationFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // CsrfConfigurer 대신 사용 권장
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
// 👇 PasswordEncoder 관련 import 추가
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 👇 [매우 중요!!!] PasswordEncoder Bean 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource())
                )
                // .csrf(CsrfConfigurer::disable) // 최신 Spring Security 방식에서는 AbstractHttpConfigurer 사용 권장
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (Stateless API 서버의 경우)
                .httpBasic(HttpBasicConfigurer::disable) // HTTP Basic 인증 비활성화
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안 함 (JWT 기반)
                )
                .authorizeHttpRequests(request -> request
                        .requestMatchers( // Swagger UI 등 문서 관련 경로는 모두 허용
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        // 인증(회원가입/로그인) 관련 API 경로는 모두 허용
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // 검색, 파일 등 공개 API 허용
                        .requestMatchers(
                                "/",
                                "/api/v1/search/**",
                                "/file/**"
                        ).permitAll()
                        // GET 요청에 대한 게시판 및 특정 사용자 정보 조회는 허용 (필요에 따라 더 세분화 가능)
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/**", "/api/v1/user/*").permitAll()

                        // 👇 [수정 권장] 폴더 및 마이페이지 관련 경로는 인증된 사용자만 접근하도록 변경
                        .requestMatchers("/api/folder/**").authenticated() // 또는 .hasRole("USER") 등
                        .requestMatchers("/mypage/**").authenticated()     // 또는 .hasRole("USER") 등

                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )
                .exceptionHandling(exceptionHandle -> exceptionHandle
                        .authenticationEntryPoint(new FailedAuthenticationEntryPoint()) // 인증 실패 시 처리
                )
                // 모든 요청 전에 jwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    // FailedAuthenticationEntryPoint 클래스는 인증 실패 시 일관된 응답을 보내기 위해 유지
    static class FailedAuthenticationEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
                throws IOException, ServletException {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":\"AP\",\"message\":\"Authorization Failed\"}");
        }
    }

    // CORS 설정은 이전과 동일하게 유지
    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:5173")); // 프론트엔드 개발 서버 주소
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 자격 증명 허용
        configuration.setMaxAge(3600L); // pre-flight 요청의 캐시 시간 (초)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 이 CORS 설정 적용
        return source;
    }
}