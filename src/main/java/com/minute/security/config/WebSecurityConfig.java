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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new JwtAuthenticationFilter(authenticationManager, jwtProvider);
    }

    @Bean
    public JwtLoginFilter jwtLoginFilter(AuthenticationManager authenticationManager) {
        JwtLoginFilter jwtLoginFilter = new JwtLoginFilter(authenticationManager, jwtProvider);
        jwtLoginFilter.setAuthenticationSuccessHandler(customAuthSuccessHandler);
        jwtLoginFilter.setAuthenticationFailureHandler(customAuthFailureHandler);
        System.out.println("JwtLoginFilter 등록됨");
        return jwtLoginFilter;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 공개 접근 허용 경로
                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-ui.html", "/api-docs/**", "/webjars/**",
                                "/api/v1/auth/**",
                                "/api/v1/user/*",
                                "/upload/**",
                                "/file/**",
                                "/api/v1/search/**", "/api/v1/videos/**",
                                "/api/v1/watch-history/**", "/api/v1/youtube/**",
                                "/api/v1/youtube/shorts/save",
                                "/api/v1/board/free", "/api/v1/board/free/{postId}", "/api/v1/board/free/{postId}/comments",
                                "/api/v1/mypage/**", "/api/v1/plans/**", "/api/v1/calendars/**", "/api/v1/weather/**",
                                "/api/notices/**"
                        ).permitAll()

                        // 회원 가입/비밀번호 재설정 관련
                        .requestMatchers(HttpMethod.POST, "/api/v1/user/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/user/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/auth/reset-password").permitAll()
                        .requestMatchers("/api/v1/auth/sign-up/validate", "/api/v1/auth/sign-up").permitAll()

                        // 자유 게시판 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/board/free/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/board/free/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/**/comments").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/board/free/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/board/free/comments/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/**/like").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/comments/**/like").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/**/report").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/comments/**/report").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/activity/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/comments/by-user").authenticated()

                        // 관리자 전용 (hasRole 사용 → ROLE_ADMIN 으로 비교됨)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/notices").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/notices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/notices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/notices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/reports/posts").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/reports/comments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/posts/{postId}/visibility").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/comments/{commentId}/visibility").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/admin/reports/all").hasRole("ADMIN")


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


                                // 비디오
//
                                .requestMatchers(HttpMethod.GET, "/api/v1/youtube/shorts").permitAll()
                                .requestMatchers("/api/v1/watch-history/**").permitAll()
                                .requestMatchers("/api/v1/youtube/**").permitAll()
                                .requestMatchers("/api/v1/videos/**").permitAll()
                                .requestMatchers("/api/v1/youtube/shorts/save").permitAll()
                        //
                                .anyRequest().authenticated()

                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new FailedAuthenticationEntryPoint()))
                .addFilterBefore(jwtAuthenticationFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(jwtLoginFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    class FailedAuthenticationEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
                throws IOException, ServletException {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":\"AP\",\"message\":\"Authorization Failed\"}");
        }
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:[*]"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}