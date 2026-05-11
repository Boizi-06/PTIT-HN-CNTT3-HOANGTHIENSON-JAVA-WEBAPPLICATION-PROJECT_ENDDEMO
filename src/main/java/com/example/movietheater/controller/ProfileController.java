package com.example.movietheater.controller;

import com.example.movietheater.dto.ProfileUpdateRequest;
import com.example.movietheater.entity.User;
import com.example.movietheater.entity.UserProfile;
import com.example.movietheater.repository.UserRepository;
import com.example.movietheater.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final AuthService authService;


    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        System.out.println("🔍 DEBUG - Username từ Security: [" + username + "]");

        // Tìm user theo username
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    System.out.println("⚠️ Không tìm thấy user, thử tìm theo tên đăng nhập không phân biệt hoa thường...");
                    return userRepository.findByUsernameIgnoreCase(username).orElse(null);
                });

        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng với username: " + username + ". Vui lòng đăng ký lại hoặc kiểm tra database.");
        }

        ProfileUpdateRequest profileRequest = new ProfileUpdateRequest();

        profileRequest.setUsername(user.getUsername());

        if (user.getProfile() != null) {
            UserProfile p = user.getProfile();
            profileRequest.setFullName(p.getFullName() != null ? p.getFullName() : "");
            profileRequest.setEmail(p.getEmail() != null ? p.getEmail() : "");
            profileRequest.setPhone(p.getPhone() != null ? p.getPhone() : "");
        } else {
            profileRequest.setFullName("");
            profileRequest.setEmail("");
            profileRequest.setPhone("");
        }

        model.addAttribute("profileRequest", profileRequest);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profileRequest") ProfileUpdateRequest request,
                                BindingResult result,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "user/profile";
        }

        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            String oldUsername = authentication.getName();
            boolean usernameChanged = !oldUsername.equals(request.getUsername());

            authService.updateProfile(oldUsername, request);

            redirectAttributes.addFlashAttribute("success", "✅ Cập nhật thông tin thành công!");

            if (usernameChanged) {
                redirectAttributes.addFlashAttribute("info", "Tên đăng nhập đã thay đổi. Vui lòng đăng nhập lại.");
                return "redirect:/logout";   // Buộc logout để đăng nhập lại với username mới
            }

            return "redirect:/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
            return "redirect:/profile";
        }
    }
}