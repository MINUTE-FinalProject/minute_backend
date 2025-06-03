package com.minute.security.handler;

import com.minute.auth.common.AuthConstants;
import com.minute.auth.common.utils.ConvertUtil;
import com.minute.auth.service.DetailUser;
import com.minute.user.entity.User;
import com.minute.user.enumpackage.UserStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;
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

    @Autowired
    public CustomAuthSuccessHandler(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        User user = ((DetailUser) authentication.getPrincipal()).getUser();

        JSONObject jsonValue = (JSONObject) ConvertUtil.converObjectToJsonObject(user);
        HashMap<String, Object> responseMap = new HashMap<>();

        JSONObject jsonObject;
        if(user.getUserStatus() == UserStatus.Y) {
            responseMap.put("userInfo", jsonValue);
            responseMap.put("message", "정지된 계정입니다.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            // 인스턴스를 통해 메서드 호출
            String token = jwtProvider.generateToken(user);
            responseMap.put("userInfo", jsonValue);
            responseMap.put("message", "로그인 성공입니다.");

            //토큰발행(/user용)
            responseMap.put("token", token);
            response.addHeader(AuthConstants.AUTH_HEADER, AuthConstants.TOKEN_TYPE + " " + token);
        }

        jsonObject = new JSONObject(responseMap);
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter printWriter = response.getWriter();
        printWriter.print(jsonObject);
        printWriter.flush();
        printWriter.close();
    }
}