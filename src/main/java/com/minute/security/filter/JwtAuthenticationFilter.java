package com.minute.security.filter;

import com.minute.security.handler.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    /** 이 경로들은 JWT 검증을 건너뜁니다. */
    private static final List<String> EXCLUDE_PATHS = List.of(
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/swagger-resources",
            "/webjars",
            "/api/v1/auth",
            "/api/v1/youtube/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // getServletPath() 은 "/api/v1/youtube/region" 을 정확히 반환합니다.
        String path = request.getServletPath();
        return EXCLUDE_PATHS.stream()
                .anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {
        String token = parseBearerToken(req);
        if (token != null) {
            // 토큰 검증 후, subject(userId)를 꺼냅니다.
            String userId = jwtProvider.validate(token);
            if (userId != null) {
                // principal을 userId로 설정
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userId,        // ← principal: 이제 userId가 됩니다
                                null,          // credentials
                                AuthorityUtils.NO_AUTHORITIES
                        );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(req, res);
    }

    private String parseBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer "))
            return header.substring(7);
        return null;
    }
}
