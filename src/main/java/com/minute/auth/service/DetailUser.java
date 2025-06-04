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
        if (user != null && user.getRoleList() != null) {
            for (String role : user.getRoleList()) {
                if (role != null && !role.trim().isEmpty()) {
                    // SimpleGrantedAuthority를 사용하여 명시적으로 권한 객체 생성
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                    authorities.add(authority);
                    // ⭐⭐⭐ 로그 추가: 실제로 생성된 권한 문자열 확인 ⭐⭐⭐
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