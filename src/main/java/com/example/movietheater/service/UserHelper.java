package com.example.movietheater.service;

import com.example.movietheater.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userHelper")
@RequiredArgsConstructor
public class UserHelper {

    private final UserRepository userRepository;

    public String getFullName(Authentication auth) {
        if (auth == null) return "Khách";

        String username = auth.getName();

        return userRepository.findByUsername(username)
                .map(u -> u.getProfile() != null
                        ? u.getProfile().getFullName()
                        : u.getUsername())
                .orElse(username);
    }
}