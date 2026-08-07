package com.elearning.service;


import com.elearning.model.User;
import com.elearning.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
