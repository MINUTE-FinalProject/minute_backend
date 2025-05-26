package com.minute.security.handler;

import com.minute.auth.common.AuthConstants;
import com.minute.auth.common.utils.ConvertUtil;
import com.minute.auth.service.DetailUser;
import com.minute.user.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

@Component
public class CustomAuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    public CustomAuthSuccessHandler(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        User user = ((DetailUser) authentication.getPrincipal()).getUser();

        HashMap<String, Object> responseMap = new HashMap<>();

        if(user.getUserStatus().equals("N")) {
            responseMap.put("userInfo", user);  // 필요시 JSON 변환
            responseMap.put("message", "정지된 계정입니다.");
        } else {
            String token = jwtProvider.generateToken(user);
            responseMap.put("userInfo", user);
            responseMap.put("message", "로그인 성공입니다.");

            response.addHeader(AuthConstants.AUTH_HEADER, AuthConstants.TOKEN_TYPE + " " + token);
        }

        response.setContentType("application/json");
        PrintWriter printWriter = response.getWriter();
        printWriter.print(new JSONObject(responseMap));
        printWriter.flush();
        printWriter.close();
    }
}

