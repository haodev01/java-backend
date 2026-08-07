package com.elearning.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(String email) {
        return buildToken(email, accessTokenExpirationMs);
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, refreshTokenExpirationMs);
    }
    private String buildToken(String subject, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)                  // "chủ" của token — ở đây là email user
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)                       // ký bằng secret — không biết secret thì không giả mạo được
                .compact();
    }

    // Trả về email nếu token hợp lệ (đúng chữ ký, chưa hết hạn).
    // Ném JwtException (ExpiredJwtException, SignatureException...) nếu không —
    // để nguyên, không nuốt lỗi, vì JwtAuthFilter cần biết CÓ lỗi để xử lý đúng.
    public String extractEmail(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

}
