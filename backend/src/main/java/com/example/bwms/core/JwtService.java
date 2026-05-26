// Translated from: backend/app/core/security.py (create_access_token, decode_access_token)
// NOTE: JJWT 0.12.x API differs significantly from python-jose; see Change Log for details.
package com.example.bwms.core;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtService {

    private final SecretKey signingKey;
    private final long expireSeconds;

    public JwtService(AppConfig config) {
        byte[] keyBytes = config.getJwtSecretKey().getBytes(StandardCharsets.UTF_8);
        // TODO: verify key is >= 32 bytes (256 bits) for HS256 — JJWT throws WeakKeyException at startup if not
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expireSeconds = (long) config.getJwtExpireMinutes() * 60;
    }

    // Python: create_access_token(subject, role, email)
    public String generateToken(long userId, String role, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expireSeconds)))
                .signWith(signingKey)
                .compact();
    }

    // Python: decode_access_token(token) — raises JWTError on failure
    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
