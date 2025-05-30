package com.minute.security.handler;

import com.minute.user.entity.User;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.DatatypeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    @Value("${jwt.key}")
    private String secretKey;

    @Value("${jwt.time}")
    private long tokenValidity;

    private static Key key;

    @PostConstruct
    public void init() {
        byte[] secretBytes = DatatypeConverter.parseBase64Binary(secretKey);
        key = new SecretKeySpec(secretBytes, SignatureAlgorithm.HS256.getJcaName());
    }


    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        Date expireDate = new Date(now + tokenValidity);

        return Jwts.builder()
                .setHeader(createHeader())
                .setClaims(createClaims(user))
                .setSubject(user.getUserId())
                .setIssuedAt(new Date(now))
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)

                
                .compact();
    }

    public boolean isValidToken(String token) {
        try {
            getClaims(token); // 파싱 시도
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private static Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();
        header.put("type", "JWT");
        header.put("alg", "HS256");
        header.put("created", System.currentTimeMillis());
        return header;
    }

    private static Map<String, Object> createClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId",user.getUserId());
        claims.put("role", user.getRole().name());

        return claims;
    }
}