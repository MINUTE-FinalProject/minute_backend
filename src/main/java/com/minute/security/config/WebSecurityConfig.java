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
                        // 1. Swagger/API Docs 관련 경로들을 최상단에 배치하여 항상 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/webjars/**"
                        ).permitAll()

                        // 2. 회원가입 및 인증 관련 API (로그인, 회원가입 검증 등)
                        .requestMatchers("/api/v1/auth/sign-up/validate").permitAll()
                        .requestMatchers("/api/v1/auth/sign-up").permitAll() // 중복 제거: 아래에 통합
                        .requestMatchers("/api/v1/auth/**").permitAll() // /api/v1/auth/ 하위의 모든 요청 허용

                        // 3. 파일 업로드/다운로드 관련 (필요한 경우)
                        .requestMatchers("/upload/**").permitAll() // 파일 업로드 경로
                        .requestMatchers("/file/**").permitAll() // 파일 다운로드/조회 경로

                        // 4. 비디오, 검색, 유튜브, 시청 기록 관련 (모두 permitAll)
                        .requestMatchers(HttpMethod.GET, "/api/v1/videos/**").permitAll()
                        .requestMatchers("/api/v1/search/**").permitAll() // POST/GET 등 모든 메서드 포함
                        .requestMatchers(HttpMethod.GET, "/api/v1/youtube/shorts").permitAll()
                        .requestMatchers("/api/v1/watch-history/**").permitAll()
                        .requestMatchers("/api/v1/youtube/**").permitAll()
                        .requestMatchers("/api/v1/youtube/shorts/save").permitAll() // 이전에 중복 선언된 부분 통합

                        // 5. 공지사항 (GET은 permitAll, POST/PUT/DELETE/PATCH는 ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/notices/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/notices").hasAuthority("ADMIN") // hasRole 대신 hasAuthority 권장
                        .requestMatchers(HttpMethod.PUT, "/api/notices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/notices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/notices/**").hasAuthority("ADMIN")

                        // 6. 마이페이지, 날씨 등 공개 API
                        .requestMatchers(HttpMethod.GET, "/api/v1/mypage/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/weather/**").permitAll()

                        // 7. 자유게시판 (Freeboard) API 경로 권한 설정
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
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/comments/by-user").authenticated() // 내가 쓴 댓글 보기
                        // ADMIN 역할 사용자만 접근 가능한 API
                        .requestMatchers("/api/v1/board/free/admin/**").hasAuthority("ADMIN") // admin 경로 전체를 ADMIN 권한으로 묶음
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/reports/posts").hasAuthority("ADMIN") // 신고된 게시글 목록
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/reports/comments").hasAuthority("ADMIN") // 신고된 댓글 목록
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/posts/{postId}/visibility").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/comments/{commentId}/visibility").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/admin/reports/all").hasAuthority("ADMIN")

                        // 8. 플랜 캘린더 (인증 필요)
                        .requestMatchers("/api/v1/plans/**").authenticated() // GET, POST, PUT, DELETE 모두 포함

                        // 9. 체크리스트 (인증 필요)
                        .requestMatchers("/api/v1/checklists/**").authenticated() // GET, POST, PUT, DELETE 모두 포함


                        // 10. 폴더 및 북마크 관련 API (인증 필요) - 문제의 `/api/v1/bookmarks/folder/**` 포함
                        // 모든 메서드에 대해 authenticated() 적용
                        .requestMatchers("/api/v1/folder/**").authenticated() // GET, POST, PUT, DELETE 모두 포함
                        .requestMatchers("/api/v1/bookmarks/**").authenticated() // GET, POST, DELETE 모두 포함


                        // 11. 관리자 API
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN") // hasRole 대신 hasAuthority 권장

                        // 12. 기타 /api/v1/user/* 관련 (PUT, POST는 permitAll 되어있었으나, 일반적으로 인증 필요)
                        // 만약 사용자 정보 조회/수정/생성에 대한 권한이 필요하다면 authenticated()로 변경해야 함.
                        // 현재 코드는 GET은 permitAll, PATCH/POST도 permitAll로 되어있어 모호함.
                        // 일반적으로 /api/v1/user/* 는 자신의 정보에만 접근 가능해야 하므로 authenticated()가 맞음.
                        // 아니면 특정 user/{userId}에 대한 조회만 permitAll일 수 있음.
                        // 여기서는 일단 permitAll로 유지 (기존 코드 기준)
                        .requestMatchers(HttpMethod.GET, "/api/v1/user/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/user/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/user/*").permitAll()

                        // 13. 모든 요청은 위에 해당하지 않으면 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new FailedAuthenticationEntryPoint()))
                // JwtLoginFilter는 UsernamePasswordAuthenticationFilter 전에 실행 (로그인 요청 처리)
                .addFilterAt(jwtLoginFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                // JwtAuthenticationFilter는 모든 요청에 대해 토큰 유효성 검사 (JwtLoginFilter 이후)
                .addFilterAfter(jwtAuthenticationFilter(authenticationManager), JwtLoginFilter.class);


        return httpSecurity.build();
    }

    // CORS 설정
    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 실제 운영 환경에서는 http://localhost:[*] 대신 허용할 정확한 도메인을 명시해야 합니다.
        configuration.setAllowedOriginPatterns(List.of("http://localhost:[*]", "http://127.0.0.1:[*]")); // 개발 환경용
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 인증 실패 시 처리 (커스텀 엔트리포인트)
    class FailedAuthenticationEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
                throws IOException, ServletException {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":\"AP\",\"message\":\"Authorization Failed\"}");
        }
    }
}