package com.minute.security.filter;

import com.minute.auth.service.DetailUser;
import com.minute.security.handler.JwtProvider;

import com.minute.user.entity.User;
import com.minute.user.enumpackage.Role;
import io.jsonwebtoken.*;
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
        List<String> whitelistPrefixes = Arrays.asList("/api/v1/auth/signup", "/api/v1/auth");

        boolean isWhitelisted = whitelistPrefixes.stream()
                .anyMatch(prefix -> request.getRequestURI().startsWith(prefix));

        if (isWhitelisted) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization"); // AuthConstants.AUTH_HEADER 사용 권장

        // 1. 토큰이 없는 경우: 다음 필터로 진행 (로그인 요청 등이 처리될 수 있도록)
        if (header == null || !header.startsWith("Bearer ")) { // AuthConstants.TOKEN_TYPE + " " 사용 권장
            chain.doFilter(request, response);
            return;
        }

        // 2. 토큰이 있는 경우: 토큰 처리 시도
        try {
            String token = header.substring(7);
            System.out.println("[JwtAuthFilter] 수신된 토큰: " + token);

            // isValidToken을 먼저 호출하는 기존 구조를 유지하되,
            // false 반환 시 명시적으로 예외를 발생시켜 아래 catch 블록에서 처리하도록 유도
            if (!jwtProvider.isValidToken(token)) {
                // isValidToken 내부에서 구체적인 예외를 throw하고 여기서 잡거나,
                // 여기서 일반적인 JwtException을 발생시켜 sendErrorResponse가 처리하도록 함.
                // getClaims를 호출하여 실제 예외를 유도하는 것이 더 정확한 오류 메시지를 줄 수 있음.
                // 여기서는 isValidToken이 false를 반환하는 상황을 JwtException으로 간주하여 처리.
                System.err.println("[JwtAuthFilter] jwtProvider.isValidToken(token)이 false 반환. 토큰 유효하지 않음.");
                jwtProvider.getClaims(token); // 이렇게 하면 getClaims 내부의 특정 예외(Signature, Expired 등)가 발생하여 아래 catch에서 잡힘
                // 만약 getClaims가 예외를 안던지고 isValidToken만 false를 반환했다면 아래처럼 직접 예외 발생
                // throw new JwtException("Provided token is not valid (as determined by isValidToken).");
            }

            // isValidToken이 true를 반환했거나, getClaims()가 성공적으로 Claims를 반환한 경우
            Claims claims = jwtProvider.getClaims(token); // 여기서 다시 호출하여 claims를 얻거나, isValidToken에서 claims를 반환받는 구조도 가능

            String userId = claims.get("userId", String.class);
            String roleString = claims.get("Role", String.class);

                String role = claims.get("role", String.class);

            if (userId == null || roleString == null) {
                System.err.println("[JwtAuthFilter] 토큰 클레임에 필수 필드(userId, Role) 누락");
                throw new JwtException("Token claims are incomplete."); // 아래 JwtException catch에서 처리
            }

            User user = new User();
            user.setUserId(userId);
            try {
                user.setRole(Role.valueOf(roleString.toUpperCase())); // Enum.valueOf는 대소문자를 구분하므로, DB나 토큰에 저장된 값의 대소문자 일관성 중요
            } catch (IllegalArgumentException e) {
                System.err.println("[JwtAuthFilter] 클레임의 Role 값('" + roleString + "')이 유효하지 않은 Enum 상수입니다.");
                throw new JwtException("Invalid role value in token claims: " + roleString, e); // 아래 JwtException catch에서 처리
            }

            DetailUser detailUser = new DetailUser();
            detailUser.setUser(user);

            AbstractAuthenticationToken authenticationToken =
                    UsernamePasswordAuthenticationToken.authenticated(
                            detailUser, null, detailUser.getAuthorities() // JWT 인증 후에는 비밀번호(token) 대신 null 사용 권장
                    );
            authenticationToken.setDetails(new WebAuthenticationDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            System.out.println("[JwtAuthFilter] SecurityContextHolder에 인증 정보 설정 완료: " + userId);

            chain.doFilter(request, response); // 인증 성공 후 다음 필터 진행

        } catch (SignatureException se) {
            System.err.println("[JwtAuthFilter] !!!! SignatureException !!!! : " + se.getMessage());
            sendErrorResponse(response, se); // sendErrorResponse가 SignatureException에 맞는 메시지 반환하도록 확인
        } catch (ExpiredJwtException eje) {
            System.err.println("[JwtAuthFilter] !!!! ExpiredJwtException !!!! : " + eje.getMessage());
            sendErrorResponse(response, eje);
        } catch (MalformedJwtException mje) { // 토큰 형식이 잘못된 경우
            System.err.println("[JwtAuthFilter] !!!! MalformedJwtException !!!! : " + mje.getMessage());
            sendErrorResponse(response, mje);
        } catch (JwtException je) { // 기타 JwtException (위에서 안 잡힌 것들, 또는 isValidToken이 false일 때 발생시킨 예외)
            System.err.println("[JwtAuthFilter] !!!! JwtException (일반) !!!! : " + je.getMessage());
            sendErrorResponse(response, je);
        } catch (Exception e) { // 그 외 예상치 못한 예외
            System.err.println("[JwtAuthFilter] !!!! 일반 Exception !!!! : " + e.getClass().getName() + " - " + e.getMessage());
            // e.printStackTrace(); // 개발 중에만 스택 트레이스 전체 확인
            sendErrorResponse(response, e); // 일반적인 오류 메시지
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