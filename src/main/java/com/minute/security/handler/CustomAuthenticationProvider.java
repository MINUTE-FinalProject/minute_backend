package com.minute.security.handler;

import com.minute.auth.service.DetailUser;
import com.minute.auth.service.DetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/* 인증 요청에서 전달받은 ID/PWD를 DB정보와 비교하여 실제 인증을 수행한다. */

public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private DetailsService detailsService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken loginToken = (UsernamePasswordAuthenticationToken) authentication;

        String id = loginToken.getName();
        String pass = (String) loginToken.getCredentials();

        DetailUser detailsUser = (DetailUser) detailsService.loadUserByUsername(id);

        System.out.println("입력 비밀번호(평문): " + pass);
        System.out.println("DB 저장 암호화 비밀번호: " + detailsUser.getPassword());

        boolean matches = passwordEncoder.matches(pass, detailsUser.getPassword());
        System.out.println("비밀번호 매칭 결과: " + matches);

        if (!matches) {
            throw new BadCredentialsException(pass + "는 틀린 비밀번호입니다.");
        }

        return new UsernamePasswordAuthenticationToken(detailsUser, pass, detailsUser.getAuthorities());
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
