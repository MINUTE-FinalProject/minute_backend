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
    protected SecurityFilterChain configure(HttpSecurity httpSecurity, AuthenticationManager authenticationManager) throws Exception {
        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(
                                // Swagger
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**",
                                "/swagger-ui.html", "/api-docs/**", "/webjars/**",

                                // 인증 관련
                                "/api/v1/auth/**",
                                "/upload/**",
                                "/file/**",

                                // 사용자 관련
                                "/api/v1/user/*",

                                // 검색, 비디오
                                "/api/v1/search/**", "/api/v1/videos/**",
                                "/api/v1/watch-history/**", "/api/v1/youtube/**",
                                "/api/v1/youtube/shorts/save",

                                // 게시판 조회
                                "/api/v1/board/**",

                                // 마이페이지, 플랜 캘린더
                                "/api/v1/mypage/**", "/api/v1/plans/**", "/api/v1/caldendars/**",
                                "/api/v1/weather/**",

                                // 공지사항 (조회는 모두 허용)
                                "/api/notices/**"
                        ).permitAll()

                        // 게시판 쓰기 및 기능 관련 (임시 공개)
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/board/free/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/board/free/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/**/comments").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/board/free/comments/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/board/free/comments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/**/like").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/comments/**/like").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/**/report").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/comments/**/report").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/reports/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/reports/comments").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/posts/**/visibility").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/comments/**/visibility").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/activity/my").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/comments/by-user").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/admin/reports/all").permitAll()

                        // 회원가입 및 비밀번호 재설정
                        .requestMatchers(HttpMethod.POST, "/api/v1/user/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/user/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/auth/reset-password").permitAll()
                        .requestMatchers("/api/v1/auth/sign-up/validate").permitAll()
                        .requestMatchers("/api/v1/auth/sign-up").permitAll()

                        // 공지사항 관리자 권한
                        .requestMatchers(HttpMethod.POST, "/api/notices").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/notices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/notices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/notices/{noticeId}/importance").hasRole("ADMIN")

                        // 관리자 API
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/admin/*").hasRole("ADMIN")

                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new FailedAuthenticationEntryPoint()))
                .addFilterAt(jwtLoginFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter(authenticationManager), JwtLoginFilter.class);

        return httpSecurity.build();
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
