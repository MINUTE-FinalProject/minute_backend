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
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/webjars/**"

                        ).permitAll()
                                .requestMatchers("/api/v1/auth/sign-up/validate").permitAll()
                                .requestMatchers("/api/v1/auth/sign-up").permitAll()
                                .requestMatchers("/upload/**").permitAll()
                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                .requestMatchers("/", "/api/v1/auth/**", "/api/v1/search/**", "/file/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/board/**", "/api/v1/user/*").permitAll()
                                .requestMatchers(HttpMethod.PATCH, "/api/v1/user/*").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/user/*").permitAll()
                                .requestMatchers("/api/v1/auth/sign-up").permitAll()
                                .requestMatchers("/","/api/v1/auth/**", "/api/v1/search/**","/file/**").permitAll()
                                .requestMatchers(HttpMethod.GET,"/api/v1/board/**","/api/v1/user/*").permitAll()


// 공개적으로 접근 가능한 API (주로 GET 요청)
                                .requestMatchers(HttpMethod.GET, "/api/v1/board/free", "/api/v1/board/free/{postId}", "/api/v1/board/free/{postId}/comments").permitAll()

// 인증된 사용자만 접근 가능한 API
                                .requestMatchers(HttpMethod.POST, "/api/v1/board/free").authenticated() // 게시글 작성
                                .requestMatchers(HttpMethod.PUT, "/api/v1/board/free/{postId}").authenticated() // 게시글 수정
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/board/free/{postId}").authenticated() // 게시글 삭제
                                .requestMatchers(HttpMethod.POST, "/api/v1/board/free/{postId}/comments").authenticated() // 댓글 작성
                                .requestMatchers(HttpMethod.PUT, "/api/v1/board/free/comments/{commentId}").authenticated() // 댓글 수정
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/board/free/comments/{commentId}").authenticated() // 댓글 삭제
                                .requestMatchers(HttpMethod.POST, "/api/v1/board/free/{postId}/like").authenticated() // 게시글 좋아요
                                .requestMatchers(HttpMethod.POST, "/api/v1/board/free/comments/{commentId}/like").authenticated() // 댓글 좋아요
                                .requestMatchers(HttpMethod.POST, "/api/v1/board/free/{postId}/report").authenticated() // 게시글 신고
                                .requestMatchers(HttpMethod.POST, "/api/v1/board/free/comments/{commentId}/report").authenticated() // 댓글 신고
                                .requestMatchers(HttpMethod.GET, "/api/v1/board/free/activity/my").authenticated() // 내 활동 보기
                                .requestMatchers(HttpMethod.GET, "/api/v1/board/free/comments/by-user").authenticated() // 내가 쓴 댓글 보기 (만약 "내"가 기준이라면)

// ADMIN 역할 사용자만 접근 가능한 API
                                .requestMatchers(HttpMethod.GET, "/api/v1/board/free/reports/posts").hasRole("ADMIN") // 신고된 게시글 목록
                                .requestMatchers(HttpMethod.GET, "/api/v1/board/free/reports/comments").hasRole("ADMIN") // 신고된 댓글 목록
//                                .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/posts/{postId}/visibility").hasRole("ADMIN") // 게시글 공개/숨김
                                .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/posts/{postId}/visibility").hasAuthority("ADMIN") // hasRole 대신 hasAuthority 사용
//                                .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/comments/{commentId}/visibility").hasRole("ADMIN") // 댓글 공개/숨김
                                .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/comments/{commentId}/visibility").hasAuthority("ADMIN") // 댓글 공개/숨김

                                .requestMatchers(HttpMethod.GET, "/api/v1/board/free/admin/reports/all").hasRole("ADMIN") // 모든 신고된 활동

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