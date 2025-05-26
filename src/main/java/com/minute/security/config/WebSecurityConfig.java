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
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
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

    @Bean
    protected SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource())
                )
                .csrf(CsrfConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
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
                        .requestMatchers("/","/api/v1/auth/**", "/api/v1/search/**","/file/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/v1/board/**","/api/v1/user/*").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free").permitAll() // <<< 이 줄을 추가 (임시)자유게시판 수정 필요
                        .requestMatchers(HttpMethod.PUT, "/api/v1/board/free/**").permitAll() // 게시글 수정
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/board/free/**").permitAll() // 게시글 삭제
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/**/comments").permitAll() // 댓글 작성
                        .requestMatchers(HttpMethod.PUT, "/api/v1/board/free/comments/**").permitAll() // <<< 댓글 수정
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/board/free/comments/**").permitAll() // <<< 댓글 삭제
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/**/like").permitAll() // <<< 게시글 좋아요
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/comments/**/like").permitAll() // <<< 댓글 좋아요
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/**/report").permitAll() // <<< 게시글 신고
                        .requestMatchers(HttpMethod.GET, "/api/notices/**").permitAll() //공지사항 목록/상세조회
                        .requestMatchers(HttpMethod.POST, "/api/notices").hasRole("ADMIN") //공지사항 작성
                        .requestMatchers(HttpMethod.PUT, "/api/notices/**").hasRole("ADMIN") //공지사항 수정
                        .requestMatchers(HttpMethod.DELETE, "/api/notices/**").hasRole("ADMIN") //공지사항 삭제
                        .requestMatchers(HttpMethod.PATCH, "/api/notices/{noticeId}/importance").hasRole("ADMIN") //공지사항 중요도 변경
                        .requestMatchers(HttpMethod.GET,"/api/v1/mypage/**","/api/v1/user/*").permitAll()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandle -> exceptionHandle
                        .authenticationEntryPoint(new FailedAuthenticationEntryPoint())
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
    protected CorsConfigurationSource corsConfigurationSource(){

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",configuration);
        return source;

    }

}
