package com.elearning.service;


import com.elearning.model.User;
import com.elearning.repository.UserRepository;
import com.elearning.security.PasswordResetTokenStore;
import com.elearning.security.RefreshTokenStore;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenStore passwordResetTokenStore;
    private final RefreshTokenStore refreshTokenStore;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       PasswordResetTokenStore passwordResetTokenStore,
                       RefreshTokenStore refreshTokenStore) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenStore = passwordResetTokenStore;
        this.refreshTokenStore = refreshTokenStore;
    }
    public User register(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email đã tồn tại");
        }
        // encode() sinh ra chuỗi hash kèm salt ngẫu nhiên bên trong — không bao
        // giờ lưu "password" gốc xuống DB nữa từ đây trở đi.
        String hashed = passwordEncoder.encode(password);
        User user = new User(email, hashed);
        return userRepository.save(user);
    }

    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                // Cố tình dùng CHUNG một thông báo lỗi cho "email không tồn tại" và
                // "sai mật khẩu" — nếu báo riêng ("email không tồn tại"), kẻ tấn công
                // dò được danh sách email nào đã đăng ký (user enumeration).
                .orElseThrow(() -> new BadCredentialsException("Sai email hoặc mật khẩu"));

        // matches(raw, hashed): tự tách salt ra khỏi hash đã lưu rồi so sánh —
        // không bao giờ tự so sánh 2 chuỗi hash bằng equals().
        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            return user;
        }
        throw new BadCredentialsException("Sai email hoặc mật khẩu");
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User không tồn tại"));
    }

    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = getByEmail(email);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Mật khẩu cũ không đúng");
        }
        user.changePassword(passwordEncoder.encode(newPassword));
        refreshTokenStore.revoke(email);
    }

    public void requestPasswordReset(String email) {

        userRepository.findByEmail(email).ifPresent(user -> {
            String token = passwordResetTokenStore.createToken(email);
            // TODO(Phase 8): thay dòng log này bằng mailSender.send(...) thật.
            log.info("[DEV] Link đặt lại mật khẩu cho {}: http://localhost:3000/reset-password?token={}",
                    email, token);
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password for token {}", token);
        String email = passwordResetTokenStore.consumeToken(token)
                .orElseThrow(() -> new BadCredentialsException("Token không hợp lệ hoặc đã hết hạn"));
        User user = getByEmail(email);
        user.changePassword(passwordEncoder.encode(newPassword));
        refreshTokenStore.revoke(email); // giống changePassword() — xem lý do ở trên
    }
}
