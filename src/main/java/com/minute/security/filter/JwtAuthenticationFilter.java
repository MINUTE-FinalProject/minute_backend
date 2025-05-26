package com.minute.security.filter;

import com.minute.auth.service.DetailUser;
import com.minute.security.handler.JwtProvider;

import com.minute.user.entity.User;
import com.minute.user.enumpackage.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    private static final AntPathMatcher matcher = new AntPathMatcher();
    private final JwtProvider jwtProvider;

    // Swagger 및 인증 엔드포인트를 모두 제외할 패턴
    private static final List<String> whitelist = List.of(
            "/api/v1/auth/signup",
            "/api/v1/auth/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    );

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtProvider jwtProvider) {
        super(authenticationManager);
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws IOException, ServletException {

        String uri = request.getRequestURI();
        // 패턴 매칭으로 예외 처리
        for (String pattern : whitelist) {
            if (matcher.match(pattern, uri)) {
                chain.doFilter(request, response);
                return;
            }
        }

        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                chain.doFilter(request, response);
                return;
            }

            String token = header.substring(7);
            if (jwtProvider.isValidToken(token)) {
                Claims claims = jwtProvider.getClaims(token);

                String userId = claims.get("userId", String.class);
                String role   = claims.get("Role",   String.class);

                User user = new User();
                user.setUserId(userId);
                user.setRole(Role.valueOf(role));

                DetailUser detailUser = new DetailUser();
                detailUser.setUser(user);

                AbstractAuthenticationToken auth =
                        UsernamePasswordAuthenticationToken.authenticated(
                                detailUser, token, detailUser.getAuthorities()
                        );
                auth.setDetails(new WebAuthenticationDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            sendErrorResponse(response, e);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, Exception e) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        JSONObject json = createErrorJson(e);
        try (PrintWriter writer = response.getWriter()) {
            writer.print(json);
        }
    }

    private JSONObject createErrorJson(Exception e) {
        String message;
        if (e instanceof ExpiredJwtException) {
            message = "Token Expired";
        } else if (e instanceof SignatureException) {
            message = "Invalid Token Signature";
        } else if (e instanceof JwtException) {
            message = "Token Parsing Error";
        } else {
            message = "Authentication Failed";
        }

        HashMap<String, Object> result = new HashMap<>();
        result.put("status", 401);
        result.put("message", message);
        result.put("reason", e.getMessage());

        return new JSONObject(result);
    }
}
