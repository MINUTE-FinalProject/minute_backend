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

    private final String secretKey = "SecretK3ySecretK3ySecretK3y12345"; // 충분히 긴 시크릿

    private final SecretKey key;

    public JwtProvider() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String create(String email) {

        Date expiredDate = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));

        String jwt = Jwts.builder()
                .signWith(key, SignatureAlgorithm.HS256)
                .setSubject(email)
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
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
        return claims.getSubject();
    }
}
