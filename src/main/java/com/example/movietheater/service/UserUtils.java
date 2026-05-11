package com.example.movietheater.service;

import com.example.movietheater.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {

    private final UserRepository userRepository;

    public UserUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getFullName(Authentication authentication) {

        if (authentication == null) return "";

        String username = authentication.getName(); // ✅ OK

        return userRepository.findByUsername(username)
                .map(u -> u.getProfile() != null
                        ? u.getProfile().getFullName()
                        : u.getUsername())
                .orElse(username);
    }
}