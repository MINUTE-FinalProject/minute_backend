package com.minute.security.config;

import com.minute.security.filter.JwtAuthenticationFilter;
import com.minute.security.filter.JwtLoginFilter;
import com.minute.security.handler.CustomAuthFailureHandler;
import com.minute.security.handler.CustomAuthSuccessHandler;
import com.minute.security.handler.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomAuthSuccessHandler customAuthSuccessHandler;
    private final CustomAuthFailureHandler customAuthFailureHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new JwtAuthenticationFilter(authenticationManager, jwtProvider);
    }

    @Bean
    public JwtLoginFilter jwtLoginFilter(AuthenticationManager authenticationManager) {
        JwtLoginFilter jwtLoginFilter = new JwtLoginFilter(authenticationManager, jwtProvider);
        jwtLoginFilter.setAuthenticationSuccessHandler(customAuthSuccessHandler);
        jwtLoginFilter.setAuthenticationFailureHandler(customAuthFailureHandler);
        System.out.println("JwtLoginFilter 등록됨");
        return jwtLoginFilter;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity httpSecurity, AuthenticationManager authenticationManager) throws Exception {
        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        // 1. Publicly accessible paths (Swagger, Auth validation, etc.)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/auth/sign-up/validate").permitAll()
                        .requestMatchers("/api/v1/auth/sign-up").permitAll()
                        .requestMatchers("/upload/**").permitAll()
                        .requestMatchers("/", "/api/v1/auth/**", "/api/v1/search/**", "/file/**").permitAll() // General public paths

                        // 2. Admin-specific paths (Most restrictive first)
                        //    Assuming DetailUser provides "ADMIN" as GrantedAuthority string
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN") // Catch-all for admin paths

                        // Freeboard ADMIN paths (already covered by /api/v1/admin/** if paths start with /admin)
                        // If paths are /api/v1/board/free/admin/**, then /api/v1/admin/** won't cover them.
                        // Ensure these specific admin paths are also correctly configured or covered.
                        // For now, let's assume /api/v1/admin/** covers them.
                        // If not, they need individual .hasAuthority("ADMIN") rules before more general board rules.
                        // Example:
                        // .requestMatchers(HttpMethod.GET, "/api/v1/board/free/admin/reports/all").hasAuthority("ADMIN")
                        // .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/posts/{postId}/visibility").hasAuthority("ADMIN")
                        // .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/comments/{commentId}/visibility").hasAuthority("ADMIN")

                        // Notices ADMIN paths
                        .requestMatchers(HttpMethod.POST, "/api/notices").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/notices/**").hasAuthority("ADMIN")


                        // 3. Authenticated user paths (More general than admin, less general than permitAll for specific sub-paths)

                        // Freeboard (Authenticated User - Non-Admin)
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/board/free/{postId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/board/free/{postId}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/{postId}/comments").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/board/free/comments/{commentId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/board/free/comments/{commentId}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/{postId}/like").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/comments/{commentId}/like").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/{postId}/report").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/free/comments/{commentId}/report").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/activity/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/comments/by-user").authenticated()

                        // QnA (User) - Authenticated
                        // This rule should come AFTER /api/v1/admin/** if there's an overlap (e.g. /api/v1/admin/qna)
                        // Since /api/v1/admin/** is already defined with hasAuthority("ADMIN"),
                        // /api/v1/qna/** will correctly apply to non-admin qna paths.
                        .requestMatchers("/api/v1/qna/**").authenticated()


                        // 4. General GET permitAll paths (if not covered by more specific permitAll above)
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/**", "/api/v1/user/*").permitAll() // Broad GET access to boards and user info
                        .requestMatchers(HttpMethod.GET,"/api/v1/videos/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/v1/search/**").permitAll() // Duplicates above general search, can be consolidated
                        .requestMatchers(HttpMethod.GET, "/api/notices/**").permitAll() // Notices list/detail
                        .requestMatchers(HttpMethod.GET,"/api/v1/mypage/**").permitAll() // mypage related
                        .requestMatchers(HttpMethod.GET,"/api/v1/plans/**").permitAll() // plans related
                        .requestMatchers(HttpMethod.GET,"/api/v1/caldendars/**").permitAll() // calendars related
                        .requestMatchers(HttpMethod.GET,"/api/v1/weather/**").permitAll() // weather related
                        .requestMatchers(HttpMethod.GET, "/api/v1/youtube/shorts").permitAll() // youtube shorts


                        // 5. Other specific permitAll paths (if any)
                        .requestMatchers("/api/v1/watch-history/**").permitAll()
                        .requestMatchers("/api/v1/youtube/**").permitAll() // Could be broad, ensure no admin subpaths are unintentionally permitted
                        .requestMatchers("/api/v1/videos/**").permitAll() // Duplicates GET videos, can be consolidated if only GET
                        .requestMatchers("/api/v1/youtube/shorts/save").permitAll() // Assuming this is a public save endpoint


                        // 6. User modification paths (permitAll might be too open, consider 'authenticated' or more specific roles if needed)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/user/*").permitAll() // User patching info
                        .requestMatchers(HttpMethod.POST, "/api/v1/user/*").permitAll()  // User creation/other POST actions


                        // 자유게시판 중복된 ADMIN 규칙들 (위에서 /api/v1/admin/** 이나 개별 ADMIN 경로로 커버되는지 확인)
                        // 만약 /api/v1/board/free/admin/** 형태가 아니라면, 이 규칙들은 유지되어야 함.
                        // .hasAuthority("ADMIN")으로 통일
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/reports/posts").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/reports/comments").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/free/admin/reports/all").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/posts/{postId}/visibility").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/board/free/comments/{commentId}/visibility").hasAuthority("ADMIN")


                        // 7. Default: any other request must be authenticated
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new FailedAuthenticationEntryPoint()))
                .addFilterAt(jwtLoginFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter(authenticationManager), JwtLoginFilter.class);

        return httpSecurity.build();
    }

    class FailedAuthenticationEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
                throws IOException, ServletException {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // This is what frontend receives as 401
            response.getWriter().write("{\"code\":\"AP\",\"message\":\"Authorization Failed\"}");
        }
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:[*]")); // Allow requests from any localhost port
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}