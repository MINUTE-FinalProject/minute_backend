package com.minute.security.filter;

import com.minute.security.handler.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@Component
@RequiredArgsConstructor//필수 매개변수 생성자 생성
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (path.startsWith("/api/v1/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = parseBearerToken(request);

            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtProvider.validate(token);

            if(email == null) {
                filterChain.doFilter(request, response);
                return;
            }

            AbstractAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(email, null, AuthorityUtils.NO_AUTHORITIES);
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authenticationToken);

            SecurityContextHolder.setContext(securityContext);

        }catch (Exception exception) {
            exception.printStackTrace();
        }
        filterChain.doFilter(request,response);
    }


    private String parseBearerToken(HttpServletRequest request) {

        String authorization = request.getHeader("Authorization");

        boolean hasAuthorization = StringUtils.hasText(authorization);
        if (!hasAuthorization) return null;

        boolean isBearer = authorization.startsWith("Bearer ");
        if(!isBearer) return null;

        String token = authorization.substring(7);
        return token;


    }
}
