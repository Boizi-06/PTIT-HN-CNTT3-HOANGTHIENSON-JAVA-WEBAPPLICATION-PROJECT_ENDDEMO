package com.example.movietheater.controller;

import com.example.movietheater.dto.ProfileUpdateRequest;
import com.example.movietheater.dto.RegisterRequest;
import com.example.movietheater.entity.Movie;
import com.example.movietheater.entity.User;
import com.example.movietheater.entity.UserProfile;
import com.example.movietheater.repository.MovieRepository;
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

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    // ... (các method register, login, home giữ nguyên)

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "password.mismatch", "Mật khẩu xác nhận không khớp!");
        }
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.register(request);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Hãy đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication authentication) {
        List<Movie> movies = movieRepository.findByActiveTrueOrderByReleaseDateDesc();
        model.addAttribute("movies", movies);
        if (authentication != null) {
            String username = authentication.getName();

            User user = userRepository.findByUsername(username).orElse(null);

            String fullName = (user != null && user.getProfile() != null)
                    ? user.getProfile().getFullName()
                    : username;

            model.addAttribute("fullName", fullName);
        }

        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }
        return "user/home";
    }

    // ==================== HỒ SƠ CÁ NHÂN ====================

}