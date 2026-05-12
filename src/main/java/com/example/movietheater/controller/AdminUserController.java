package com.example.movietheater.controller;

import com.example.movietheater.entity.User;
import com.example.movietheater.eenum.Role;
import com.example.movietheater.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    public String userList(Model model) {
        List<User> users = userRepository.findAllNotAdmin();
        model.addAttribute("users", users);
        return "admin/users";
    }

    // ====================== KHÓA / MỞ KHÓA ======================
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // ❌ Không cho khóa Admin
        if (user.getRole() == Role.ADMIN) {
            ra.addFlashAttribute("error", "Không thể khóa tài khoản Admin!");
            return "redirect:/admin/users";
        }

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        String msg = user.isEnabled() ? "✅ Đã mở khóa tài khoản!" : "🔒 Đã khóa tài khoản!";
        ra.addFlashAttribute("success", msg);
        return "redirect:/admin/users";
    }

    // ====================== XÓA NGƯỜI DÙNG ======================
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // ❌ NGĂN CHẶN XÓA ADMIN
        if (user.getRole() == Role.ADMIN) {
            ra.addFlashAttribute("error", "❌ Không được xóa tài khoản Admin!");
            return "redirect:/admin/users";
        }

        try {
            userRepository.deleteById(id);
            ra.addFlashAttribute("success", "🗑️ Đã xóa người dùng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa người dùng này!");
        }
        return "redirect:/admin/users";
    }
}