package com.elearning.security;


import com.elearning.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetTokenStore {
    private  static  final Duration TTL = Duration.ofMinutes(15);
    private  final StringRedisTemplate redisTemplate;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public PasswordResetTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private  String keyFor(String token) {
        return "password-reset:" + token;
    }

    public  String createToken(String email) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(keyFor(token), email, TTL);
        return token;
    }
    public Optional<String> consumeToken(String token) {

        log.info("Consuming token {}", token);
        String key = keyFor(token);

        log.info("Key for token {} is {}", token, key);

        String email = redisTemplate.opsForValue().get(key);
        log.info("Email for token {} is {}", token, email);
        if (email != null) {
            // Xoá NGAY sau khi đọc — token dùng 1 lần. Nếu không xoá, ai nhặt
            // được token cũ (vd từ log, từ lịch sử trình duyệt) vẫn đặt lại được
            // mật khẩu bất cứ lúc nào trong 15 phút, kể cả sau khi đã dùng rồi.
//            redisTemplate.delete(key);
        }
        return Optional.ofNullable(email);
    }
}
