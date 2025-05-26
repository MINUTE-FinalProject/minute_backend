package com.minute.auth.service;

import com.minute.user.service.UserService;
import org.springframework.security.authentication.AuthenticationServiceException;
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
            throw new AuthenticationServiceException(username + " is Empty!");
        }

        return userService.getUserEntityByEmail(username)
                .map(user -> new DetailUser(Optional.of(user)))
                .orElseThrow(() -> new AuthenticationServiceException(username));
    }
}
