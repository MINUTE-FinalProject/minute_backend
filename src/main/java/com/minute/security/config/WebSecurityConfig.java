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

                                // 게시판
//                                .requestMatchers(HttpMethod.POST, "/api/v1/board/free").permitAll() // <<< 이 줄을 추가 (임시)자유게시판 수정 필요
//                                .requestMatchers(HttpMethod.PUT, "/api/v1/board/free/**").permitAll() // 게시글 수정
//                                .requestMatchers(HttpMethod.DELETE, "/api/v1/board/free/**").permitAll() // 게시글 삭제
//                                .requestMatchers(HttpMethod.POST, "/api/v1/board/free/**/comments").permitAll() // 댓글 작성
//                                .requestMatchers(HttpMethod.PUT, "/api/v1/board/free/comments/**").permitAll() // <<< 댓글 수정
//                                .requestMatchers(HttpMethod.DELETE, "/api/v1/board/free/comments/**").permitAll() // <<< 댓글 삭제
//                                .requestMatchers(HttpMethod.POST, "/api/v1/board/free/**/like").permitAll() // <<< 게시글 좋아요
//                                .requestMatchers(HttpMethod.POST, "/api/v1/board/free/comments/**/like").permitAll() // <<< 댓글 좋아요
//                                .requestMatchers(HttpMethod.POST, "/api/v1/board/free/**/report").permitAll() // <<< 게시글 신고
//                                .requestMatchers(HttpMethod.POST, "/api/v1/board/free/comments/**/report").permitAll() // <<< 댓글 신고
//                                .requestMatchers(HttpMethod.GET, "/api/v1/board/free/reports/posts").permitAll() // <<< 신고된 게시글 목록 조회 (관리자용 임시)
//                                .requestMatchers(HttpMethod.GET, "/api/v1/board/free/reports/comments").permitAll() // <<< 신고된 댓글 목록 조회 (관리자용 임시)
//                                .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/posts/**/visibility").permitAll() // <<< 게시글 숨김/공개 처리 (관리자용 임시)
//                                .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/comments/**/visibility").permitAll() // <<< 댓글 숨김/공개 처리 (관리자용 임시)
//                                .requestMatchers(HttpMethod.GET, "/api/v1/board/free/activity/my").permitAll() // <<< 내 활동 목록 조회 (임시)
//                                .requestMatchers(HttpMethod.GET, "/api/v1/board/free/comments/by-user").permitAll() // <<< 내가 쓴 댓글 목록 조회 (임시)
//                                .requestMatchers(HttpMethod.GET, "/api/v1/board/free/admin/reports/all").permitAll() // <<< 전체 신고 활동 목록 (관리자용 임시)

                                .requestMatchers(HttpMethod.GET,"/api/v1/videos/**","/api/v1/user/*").permitAll()
                                .requestMatchers(HttpMethod.GET,"/api/v1/search/**","/api/v1/user/*").permitAll()
                                .requestMatchers("/api/v1/watch-history/**").permitAll()
                                .requestMatchers("/api/v1/youtube/**").permitAll()
                                .requestMatchers("/api/v1/videos/**").permitAll()
                                .requestMatchers("/api/v1/youtube/shorts/save").permitAll()

                                .requestMatchers(HttpMethod.GET, "/api/notices/**").permitAll() //공지사항 목록/상세조회
                                .requestMatchers(HttpMethod.POST, "/api/notices").hasRole("ADMIN") //공지사항 작성
                                .requestMatchers(HttpMethod.PUT, "/api/notices/**").hasRole("ADMIN") //공지사항 수정

                                // 플랜 캘린더
                                .requestMatchers(HttpMethod.GET,"/api/v1/mypage/**","/api/v1/user/*").permitAll()
                                .requestMatchers(HttpMethod.GET,"/api/v1/plans/**","/api/v1/user/*").permitAll()
                                .requestMatchers(HttpMethod.GET,"/api/v1/caldendars/**","/api/v1/user/*").permitAll()
                                .requestMatchers(HttpMethod.GET,"/api/v1/weather/**").permitAll()

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