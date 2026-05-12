package com.example.movietheater.security;

import com.example.movietheater.entity.User;
import com.example.movietheater.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        System.out.println("🔍 DEBUG LOGIN - Username: " + username
                + " | Enabled: " + user.isEnabled()
                + " | Role: " + user.getRole()
                + " | Password hash length: " + user.getPassword().length());

        return new CustomUserDetails(user);
    }
}