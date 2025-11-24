package com.event.bidding.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    // Read secret from properties or environment variable JWT_SECRET; fallback to a default (not recommended for prod)
    @Value("${jwt.secret:super-secret-key-for-jwt-token-generation-1234567890}")
    private String secret;

    @Value("${jwt.expirationMs:3600000}")
    private long expirationMs;

    private SecretKey key;

    private synchronized void ensureKey() {
        if (key == null) {
            // Ensure secret is of sufficient length for HMAC-SHA; if short, pad deterministically
            byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                // pad with a reproducible pattern
                StringBuilder sb = new StringBuilder(secret);
                while (sb.toString().getBytes(StandardCharsets.UTF_8).length < 32) {
                    sb.append("-jwt-pad");
                }
                bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            }
            key = Keys.hmacShaKeyFor(bytes);
        }
    }

    public String generateToken(String subject, String userType) {
        ensureKey();
        return Jwts.builder()
                .setSubject(subject)
                .claim("role", userType)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            ensureKey();
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            return null;
        }
    }
}
