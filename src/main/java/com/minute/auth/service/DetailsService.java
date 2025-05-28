package com.minute.auth.service;

import com.minute.user.service.UserService;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DetailsService implements UserDetailsService {

    private final UserService userService;

    public DetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isEmpty()) {
            throw new AuthenticationServiceException("이메일이 비어 있습니다.");
        }

        return userService.getUserEntityByEmail(username)
                .map(user -> {
                    if ("N".equals(user.getUserStatus())) {
                        // 정지된 계정이라면 로그인 실패 처리
                        throw new DisabledException("정지된 계정입니다.");
                    }
                    return new DetailUser(Optional.of(user));
                })
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다: " + username));
    }
}