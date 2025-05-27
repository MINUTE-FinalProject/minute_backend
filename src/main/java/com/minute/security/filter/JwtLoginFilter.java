package com.minute.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minute.auth.dto.request.auth.LoginDTO;
import com.minute.auth.service.DetailUser;
import com.minute.security.handler.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private AuthenticationSuccessHandler successHandler;
    private AuthenticationFailureHandler failureHandler;

    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }


    public JwtLoginFilter(AuthenticationManager authenticationManager, JwtProvider jwtProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        setAuthenticationManager(authenticationManager);
        setFilterProcessesUrl("/api/v1/auth/sign-in"); // 로그인 처리 URL
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LoginDTO loginRequest = objectMapper.readValue(request.getInputStream(), LoginDTO.class);

            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(loginRequest.getUserId(), loginRequest.getUserPw());

            return authenticationManager.authenticate(authRequest);
        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 파싱 실패", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException, ServletException {

        if (successHandler != null) {
            successHandler.onAuthenticationSuccess(request, response, authResult);
        } else {
            super.successfulAuthentication(request, response, chain, authResult);
        }
    }


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        System.out.println("❌ 로그인 실패: " + failed.getMessage());
        if (failureHandler != null) {
            failureHandler.onAuthenticationFailure(request, response, failed);
        } else {
            super.unsuccessfulAuthentication(request, response, failed);
        }
    }

}
