package com.minute.security.handler;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtProvider {

    // Secret Key는 예시이며, 실제 운영에서는 더 강력하고 안전하게 관리해야 합니다.
    // (예: 환경 변수 또는 외부 설정 파일에서 주입)
    private final String secretKeyString = "SecretK3ySecretK3ySecretK3y12345"; // 원본 변수명 유지

    private final SecretKey key;

    public JwtProvider(/*@Value("${jwt.secret}") String secretKeyString*/) { // 필요하다면 외부 설정 주입
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        this.key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String create(String subject) { // 파라미터 이름을 email에서 좀 더 일반적인 subject로 변경 (또는 userId)

        // 👇 [수정] 토큰 만료 시간을 1시간에서 예를 들어 24시간으로 변경
        // Date expiredDate = Date.from(Instant.now().plus(1, ChronoUnit.HOURS)); // 기존 1시간
        Date expiredDate = Date.from(Instant.now().plus(24, ChronoUnit.HOURS)); // 24시간으로 변경

        // 토큰의 주체(subject)는 로그인 시 사용한 ID (예: userId)로 하는 것이 일반적입니다.
        // 현재 email을 subject로 사용하고 계신데, 이것이 Spring Security에서 Principal로 사용될 ID와 일치하는지 확인이 필요합니다.
        // 만약 Spring Security가 userId를 기준으로 사용자를 찾는다면, subject도 userId로 설정해야 합니다.
        String jwt = Jwts.builder()
                .signWith(key, SignatureAlgorithm.HS256)
                .setSubject(subject) // 파라미터로 받은 subject (userId 또는 email)
                .setIssuedAt(new Date())
                .setExpiration(expiredDate)
                .compact();

        return jwt;
    }

    public String validate(String jwt) {
        Claims claims = null;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException eje) { // 구체적인 예외 처리
            System.err.println("JWT Token Expired: " + eje.getMessage());
            // eje.printStackTrace(); // 개발 중 상세 로그
            return null; // 만료 시 null 반환 또는 특정 예외 throw
        } catch (io.jsonwebtoken.JwtException je) { // 기타 JWT 관련 예외
            System.err.println("JWT Token Validation Error: " + je.getMessage());
            // je.printStackTrace(); // 개발 중 상세 로그
            return null;
        } catch (Exception exception) { // 그 외 일반 예외
            System.err.println("JWT Token General Validation Error: " + exception.getMessage());
            // exception.printStackTrace();
            return null;
        }
        return claims.getSubject(); // subject (userId 또는 email) 반환
    }
}