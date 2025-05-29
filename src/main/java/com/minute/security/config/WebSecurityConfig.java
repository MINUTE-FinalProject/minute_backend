package com.minute.security.config;

import com.minute.security.filter.JwtAuthenticationFilter;
import com.minute.security.filter.JwtLoginFilter;
import com.minute.security.handler.CustomAuthFailureHandler;
import com.minute.security.handler.CustomAuthSuccessHandler;
import com.minute.security.handler.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
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

    private final JwtProvider jwtProvider;
    private final CustomAuthSuccessHandler customAuthSuccessHandler;
    private final CustomAuthFailureHandler customAuthFailureHandler;

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            AuthenticationManager authenticationManager
    ) {
        return new JwtAuthenticationFilter(authenticationManager, jwtProvider);
    }

    @Bean
    public JwtLoginFilter jwtLoginFilter(
            AuthenticationManager authenticationManager
    ) {
        JwtLoginFilter filter = new JwtLoginFilter(authenticationManager, jwtProvider);
        filter.setAuthenticationSuccessHandler(customAuthSuccessHandler);
        filter.setAuthenticationFailureHandler(customAuthFailureHandler);
        return filter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected SecurityFilterChain configure(
            HttpSecurity httpSecurity,
            AuthenticationManager authenticationManager
    ) throws Exception {
        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // --- Swagger & Public API ---
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/webjars/**"
                        ).permitAll()

                        // --- 회원가입 & 인증 관련 ---
                        .requestMatchers("/api/v1/auth/sign-up/validate").permitAll()
                        .requestMatchers("/api/v1/auth/sign-up").permitAll()

                        // --- 파일 업로드 & 검색 & 루트 페이지 ---
                        .requestMatchers("/upload/**").permitAll()
                        .requestMatchers("/", "/api/v1/auth/**", "/api/v1/search/**", "/file/**").permitAll()

                        // --- 유저 조회/수정/등록 (permitAll) ---
                        .requestMatchers(HttpMethod.GET,   "/api/v1/board/**", "/api/v1/user/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/user/*").permitAll()
                        .requestMatchers(HttpMethod.POST,  "/api/v1/user/*").permitAll()

                        // --- 게시판 (임시 공개) ---
                        .requestMatchers(HttpMethod.POST,    "/api/v1/board/free").permitAll()
                        .requestMatchers(HttpMethod.PUT,     "/api/v1/board/free/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE,  "/api/v1/board/free/**").permitAll()
//                        .requestMatchers(HttpMethod.POST,    "/api/v1/board/free/**/comments").permitAll()
                        .requestMatchers(HttpMethod.PUT,     "/api/v1/board/free/comments/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE,  "/api/v1/board/free/comments/**").permitAll()
//                        .requestMatchers(HttpMethod.POST,    "/api/v1/board/free/**/like").permitAll()
//                        .requestMatchers(HttpMethod.POST,    "/api/v1/board/free/comments/**/like").permitAll()
//                        .requestMatchers(HttpMethod.POST,    "/api/v1/board/free/**/report").permitAll()
//                        .requestMatchers(HttpMethod.POST,    "/api/v1/board/free/comments/**/report").permitAll()
                        .requestMatchers(HttpMethod.GET,     "/api/v1/board/free/reports/posts").permitAll()
                        .requestMatchers(HttpMethod.GET,     "/api/v1/board/free/reports/comments").permitAll()
                        .requestMatchers(HttpMethod.PATCH,   "/api/v1/board/free/posts/**/visibility").permitAll()
                        .requestMatchers(HttpMethod.PATCH,   "/api/v1/board/free/comments/**/visibility").permitAll()
                        .requestMatchers(HttpMethod.GET,     "/api/v1/board/free/activity/my").permitAll()
                        .requestMatchers(HttpMethod.GET,     "/api/v1/board/free/comments/by-user").permitAll()
                        .requestMatchers(HttpMethod.GET,     "/api/v1/board/free/admin/reports/all").permitAll()

                        // --- 플랜 캘린더 (인증 필요) ---
                        .requestMatchers(HttpMethod.GET,    "/api/v1/plans",    "/api/v1/plans/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/v1/plans",    "/api/v1/plans/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/plans/{planId}", "/api/v1/plans/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/plans/{planId}", "/api/v1/plans/**").authenticated()

                        // --- 체크리스트 (인증 필요) ---
                        .requestMatchers(HttpMethod.GET,    "/api/v1/checklists",    "/api/v1/checklists/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/v1/checklists",    "/api/v1/checklists/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/checklists/{id}", "/api/v1/checklists/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/checklists/{id}", "/api/v1/checklists/**").authenticated()

                        // --- 비디오 & 쇼츠 & 히스토리 (permitAll) ---
                        .requestMatchers(HttpMethod.GET, "/api/v1/videos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/search/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/youtube/shorts").permitAll()
                        .requestMatchers("/api/v1/watch-history/**").permitAll()
                        .requestMatchers("/api/v1/youtube/**").permitAll()
                        .requestMatchers("/api/v1/youtube/shorts/save").permitAll()

                        // --- 공지사항(Notice) ---
                        .requestMatchers(HttpMethod.GET,  "/api/notices/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/notices").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,  "/api/notices/**").hasRole("ADMIN")

                        // --- 나머지 공개 API ---
                        .requestMatchers(HttpMethod.GET, "/api/v1/mypage/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/weather/**").permitAll()

                        // --- 그 외 모든 요청은 인증 필요 ---
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(new FailedAuthenticationEntryPoint())
                )
                .addFilterAt(jwtLoginFilter(authenticationManager),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter(authenticationManager),
                        JwtLoginFilter.class);

        return httpSecurity.build();
    }


    // 401 응답 포맷
    static class FailedAuthenticationEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(
                HttpServletRequest request,
                HttpServletResponse response,
                AuthenticationException authException
        ) throws IOException, ServletException {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter()
                    .write("{\"code\":\"AP\",\"message\":\"Authorization Failed\"}");
        }
    }

    // CORS 설정: Authorization 헤더 허용
    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*"));
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept"
        ));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
