package com.minute.security.handler;

import com.minute.user.entity.User;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.DatatypeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// SLF4J 로거를 사용한다면 import 추가 (권장)
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProvider {

    // SLF4J 로거 선언 (권장)
    // private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);

    @Value("${jwt.key}")
    private String secretKey;

    @Value("${jwt.time}")
    private long tokenValidity;

    private static Key key;

    @PostConstruct
    public void init() {
        try {
            System.out.println("[JwtProvider] init: Initializing JWT key...");
            // log.info("[JwtProvider] init: Initializing JWT key...");
            if (secretKey == null || secretKey.trim().isEmpty()) {
                System.err.println("[JwtProvider] init: jwt.key is null or empty! Please check your configuration.");
                // log.error("[JwtProvider] init: jwt.key is null or empty! Please check your configuration.");
                throw new IllegalArgumentException("jwt.key cannot be null or empty");
            }
            byte[] secretBytes = DatatypeConverter.parseBase64Binary(secretKey);
            key = new SecretKeySpec(secretBytes, SignatureAlgorithm.HS256.getJcaName());
            System.out.println("[JwtProvider] init: JWT key initialized successfully.");
            // log.info("[JwtProvider] init: JWT key initialized successfully.");
        } catch (Exception e) {
            System.err.println("[JwtProvider] init: Error initializing JWT key - " + e.getMessage());
            // log.error("[JwtProvider] init: Error initializing JWT key", e);
            throw new RuntimeException("Failed to initialize JWT key", e);
        }
    }

    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        Date expireDate = new Date(now + tokenValidity);

        System.out.println("[JwtProvider] generateToken: Generating token for userId: " + user.getUserId());
        // log.info("[JwtProvider] generateToken: Generating token for userId: {}", user.getUserId());

        return Jwts.builder()
                .setHeader(createHeader())
                .setClaims(createClaims(user))
                .setSubject(user.getUserName()) // Assuming User entity has getUserName()
                .setIssuedAt(new Date(now))
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            System.err.println("[JwtProvider] isValidToken: Token string is null or empty.");
            // log.warn("[JwtProvider] isValidToken: Token string is null or empty.");
            return false;
        }
        try {
            System.out.println("[JwtProvider] isValidToken: Attempting to validate and parse token: " + token.substring(0, Math.min(token.length(), 30)) + "..."); // 토큰 일부만 로깅
            // log.debug("[JwtProvider] isValidToken: Attempting to validate and parse token starting with: {}", token.substring(0, Math.min(token.length(), 30)));

            getClaims(token); // 파싱 시도 (성공하면 예외 없음)

            System.out.println("[JwtProvider] isValidToken: Token validation successful.");
            // log.info("[JwtProvider] isValidToken: Token validation successful for token starting with: {}", token.substring(0, Math.min(token.length(), 30)));
            return true;
        } catch (ExpiredJwtException eje) {
            System.err.println("[JwtProvider] isValidToken: JWT EXPIRED - " + eje.getMessage());
            // log.warn("[JwtProvider] isValidToken: JWT EXPIRED - Token: {} (Message: {})", token.substring(0, Math.min(token.length(), 10)), eje.getMessage());
            // eje.printStackTrace(); // 스택 트레이스가 필요하다면 이 줄 주석 해제
            return false;
        } catch (io.jsonwebtoken.security.SignatureException se) { // 정식 SignatureException import 사용 권장
            System.err.println("[JwtProvider] isValidToken: JWT SIGNATURE INVALID - " + se.getMessage());
            // log.warn("[JwtProvider] isValidToken: JWT SIGNATURE INVALID - Token: {} (Message: {})", token.substring(0, Math.min(token.length(), 10)), se.getMessage());
            // se.printStackTrace();
            return false;
        } catch (MalformedJwtException mje) {
            System.err.println("[JwtProvider] isValidToken: JWT MALFORMED - " + mje.getMessage());
            // log.warn("[JwtProvider] isValidToken: JWT MALFORMED - Token: {} (Message: {})", token.substring(0, Math.min(token.length(), 10)), mje.getMessage());
            // mje.printStackTrace();
            return false;
        } catch (UnsupportedJwtException uje) {
            System.err.println("[JwtProvider] isValidToken: JWT UNSUPPORTED - " + uje.getMessage());
            // log.warn("[JwtProvider] isValidToken: JWT UNSUPPORTED - Token: {} (Message: {})", token.substring(0, Math.min(token.length(), 10)), uje.getMessage());
            // uje.printStackTrace();
            return false;
        }
        catch (IllegalArgumentException iae) { // Jwts.parserBuilder() 또는 getClaims() 내부에서 발생 가능
            System.err.println("[JwtProvider] isValidToken: JWT ILLEGAL ARGUMENT (e.g., empty token string after parsing) - " + iae.getMessage());
            // log.warn("[JwtProvider] isValidToken: JWT ILLEGAL ARGUMENT - Token: {} (Message: {})", token, iae.getMessage());
            // iae.printStackTrace();
            return false;
        } catch (JwtException je) { // 기타 모든 JwtException 포괄
            System.err.println("[JwtProvider] isValidToken: GENERIC JWT EXCEPTION - " + je.getClass().getSimpleName() + ": " + je.getMessage());
            // log.warn("[JwtProvider] isValidToken: GENERIC JWT EXCEPTION - {}: {} (Token: {})", je.getClass().getSimpleName(), je.getMessage(), token.substring(0, Math.min(token.length(), 10)));
            // je.printStackTrace();
            return false;
        }
    }

    public Claims getClaims(String token) {
        // System.out.println("[JwtProvider] getClaims: Parsing token: " + token.substring(0, Math.min(token.length(), 30)) + "...");
        // log.trace("[JwtProvider] getClaims: Parsing token starting with: {}", token.substring(0, Math.min(token.length(), 30)));
        if (key == null) {
            System.err.println("[JwtProvider] getClaims: JWT signing key is not initialized!");
            // log.error("[JwtProvider] getClaims: JWT signing key is not initialized!");
            throw new IllegalStateException("JWT signing key is not initialized. Check 'jwt.key' configuration.");
        }
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token) // 이 부분에서 ExpiredJwtException, SignatureException 등 발생
                .getBody();
    }

    private static Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();
        header.put("type", "JWT");
        header.put("alg", "HS256");
        // header.put("created", System.currentTimeMillis()); // 굳이 헤더에 생성시간을 넣을 필요는 없습니다. JWT 표준 페이로드의 'iat'로 충분합니다.
        return header;
    }

    private static Map<String, Object> createClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("Role", String.valueOf(user.getRole())); // Enum을 String으로 명시적 변환
        System.out.println("[JwtProvider] createClaims: Created claims with userId: " + user.getUserId() + ", Role: " + user.getRole());
        // log.debug("[JwtProvider] createClaims: Created claims with userId: {}, Role: {}", user.getUserId(), user.getRole());
        return claims;
    }
}