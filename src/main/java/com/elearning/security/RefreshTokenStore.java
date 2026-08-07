package com.elearning.security;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenStore {

    private final StringRedisTemplate redisTemplate;
    private final long refreshTokenExpirationMs;

    public RefreshTokenStore(
            StringRedisTemplate redisTemplate,
            @Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    private  String keyFor(String email) {
        return "refresh_token:" + email;
    }
    public void save(String email, String refreshToken) {
        redisTemplate.opsForValue().set(keyFor(email), refreshToken);
    }

    public  boolean isValid(String email, String refreshToken) {
        String storedToken = redisTemplate.opsForValue().get(keyFor(email));
        return refreshToken.equals(storedToken);
    }
    public void revoke(String email) {
        redisTemplate.delete(keyFor(email));
    }
}
