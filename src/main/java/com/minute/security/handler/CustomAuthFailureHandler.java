package com.minute.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

@Component
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        String failType = "UNKNOWN";
        String failMsg = "로그인 실패";

        if (exception instanceof UsernameNotFoundException) {
            failType = "SF"; // Sign-in Failure
            failMsg = "사용자를 찾을 수 없습니다.";
        } else if (exception instanceof BadCredentialsException) {
                failType = "IP"; // Incorrect Password
                failMsg = "아이디 또는 비밀번호가 올바르지 않습니다.";
        } else if (exception instanceof DisabledException) {
            failType = "BAN";
            failMsg = "정지된 계정입니다.";
        }

        JSONObject result = new JSONObject();
        result.put("code", failType);
        result.put("message", failMsg);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(result.toJSONString());
        response.getWriter().flush();
    }
}