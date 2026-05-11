package com.example.movietheater.service;

import com.example.movietheater.dto.ProfileUpdateRequest;
import com.example.movietheater.dto.RegisterRequest;
import com.example.movietheater.entity.User;
import com.example.movietheater.entity.UserProfile;
import com.example.movietheater.eenum.Role;
import com.example.movietheater.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .build();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())           // ← Thêm phone
                .build();

        user.setProfile(profile);
        userRepository.save(user);
    }

    @Transactional
    public void updateProfile(String oldUsername, ProfileUpdateRequest request) {
        User user = userRepository.findByUsername(oldUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra username mới có bị trùng không
        if (!oldUsername.equals(request.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập này đã tồn tại!");
        }

        user.setUsername(request.getUsername());

        // Cập nhật Profile
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = UserProfile.builder()
                    .user(user)
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .build();
            user.setProfile(profile);
        } else {
            profile.setFullName(request.getFullName());
            profile.setEmail(request.getEmail());
            profile.setPhone(request.getPhone());
        }

        // Đổi mật khẩu (tùy chọn)
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                throw new RuntimeException("Vui lòng nhập mật khẩu hiện tại");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Mật khẩu hiện tại không đúng");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.save(user);
    }
}