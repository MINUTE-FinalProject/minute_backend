package com.minute.auth.service;

import com.minute.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class DetailUser implements UserDetails {

    private User user;

    public DetailUser() {
    }
    public DetailUser(Optional<User> user) {
        this.user = user.get();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        Collection<GrantedAuthority> authorities = new ArrayList<>();
//        user.getRoleList().forEach(role -> authorities.add(() -> role));
//        return authorities;
//    }

    @Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    // user 객체와 role 목록이 null이 아닌지 확인 (feature/qna의 안정성)
    if (user != null && user.getRoleList() != null) {
        for (String role : user.getRoleList()) {
            // 각 role 문자열이 유효한지 확인 (feature/qna의 안정성)
            if (role != null && !role.trim().isEmpty()) {
                // "ROLE_" 접두사 추가 (main 브랜치의 관례)
                // 문자열 앞뒤 공백 제거 후 접두사 추가
                String authorityString = "ROLE_" + role.trim();
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityString);
                authorities.add(authority);
                // 디버그 로그 (feature/qna의 로깅)
                // 참고: 운영 환경에서는 System.out.println 대신 로깅 프레임워크(예: SLF4J, Logback) 사용을 권장합니다.
                System.out.println("[DetailUser DEBUG] GrantedAuthority created: " + authority.getAuthority());
            }
        }
    }
    return authorities;
}



    public String getUserId(){
        return user.getUserId();
    }

    @Override
    public String getPassword() {
        return user.getUserPw();
    }

    @Override
    public String getUsername() {
        return user.getUserId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}