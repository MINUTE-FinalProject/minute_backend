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
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    // permitAll()로 지정된 경로들 목록 - 필요에 따라 WebSecurityConfig와 동기화
    private static final List<String> PERMIT_ALL_PATHS = Arrays.asList(
            "/api/v1/auth",
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars",
            "/file",        // WebSecurityConfig에 /file/** 가 있어서 추가
            "/api/v1/search"// WebSecurityConfig에 /api/v1/search/** 가 있어서 추가
            // 루트 경로 "/" 도 필요하다면 추가
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String path = request.getRequestURI();

            // permitAll() 경로인지 확인
            boolean isPermitAllPath = PERMIT_ALL_PATHS.stream().anyMatch(p -> path.startsWith(p));
            if (path.equals("/")) { // 루트 경로 명시적 허용 (필요시)
                isPermitAllPath = true;
            }


            if (isPermitAllPath) {
                filterChain.doFilter(request, response); // 토큰 검증 없이 다음 필터로 진행
                return;
            }

            String token = parseBearerToken(request);

            if (token == null) {
                // permitAll 경로가 아닌데 토큰이 없으면, Spring Security의 다음 단계에서 처리됨
                // (보통 AuthenticationEntryPoint가 호출되어 401 반환)
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtProvider.validate(token);

            if (email == null) {
                // 토큰은 있었지만 유효하지 않은 경우
                // 이 역시 Spring Security의 다음 단계에서 처리됨
                filterChain.doFilter(request, response);
                return;
            }

            AbstractAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(email, null, AuthorityUtils.NO_AUTHORITIES);
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(securityContext);

        } catch (Exception exception) {
            // 실제 운영 환경에서는 로깅 프레임워크(SLF4J 등)를 사용하여 예외를 기록하는 것이 좋습니다.
            // 예를 들어, logger.error("JWT Filter Error: ", exception);
            // 여기서는 스택 트레이스를 출력하여 개발 중 문제를 빠르게 파악하도록 합니다.
            exception.printStackTrace();
            // 예외 발생 시 클라이언트에게 오류 응답을 명확히 전달할 수도 있습니다.
            // response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // response.getWriter().write("{\"error\":\"Internal Server Error during authentication\"}");
            // return; // 예외 발생 시 필터 체인 진행을 멈추려면 return 추가
        }

        filterChain.doFilter(request, response);
    }

    private String parseBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        boolean hasAuthorization = StringUtils.hasText(authorization);
        if (!hasAuthorization) return null;
        boolean isBearer = authorization.startsWith("Bearer ");
        if (!isBearer) return null;
        return authorization.substring(7);
    }
}