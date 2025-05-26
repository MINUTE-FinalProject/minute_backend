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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtProvider jwtProvider) {
        super(authenticationManager);
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 인증 제외 URI 목록
        List<String> whitelist = Arrays.asList("/api/v1/auth/signup", "/api/v1/auth");

        if (whitelist.contains(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        try {
            if (header == null || !header.startsWith("Bearer ")) {
                chain.doFilter(request, response);
                return;
            }

            String token = header.substring(7);

            if (jwtProvider.isValidToken(token)) {
                Claims claims = jwtProvider.getClaims(token);

                // 사용자 정보 파싱
                String userId = claims.get("userId", String.class);
                System.out.println("JWT에서 추출한 userId = " + userId);

                String role = claims.get("Role", String.class);

                // User, DetailsUser 생성
                User user = new User();
                user.setUserId(userId);
                user.setRole(Role.valueOf(role));

                DetailUser detailUser = new DetailUser();
                detailUser.setUser(user);

                // 인증 객체 생성 및 설정
                AbstractAuthenticationToken authenticationToken =
                        UsernamePasswordAuthenticationToken.authenticated(
                                detailUser, token, detailUser.getAuthorities()
                        );
                authenticationToken.setDetails(new WebAuthenticationDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
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

        PrintWriter writer = response.getWriter();
        writer.print(json);
        writer.flush();
        writer.close();
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
