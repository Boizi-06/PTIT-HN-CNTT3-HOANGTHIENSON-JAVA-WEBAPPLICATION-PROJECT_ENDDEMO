package com.example.movietheater.config;

import com.example.movietheater.eenum.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Công khai
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/static/**").permitAll()

                        // User & Staff & Admin
                        .requestMatchers("/user/**", "/booking/**", "/profile/**", "/home", "/movies/**").hasAnyRole("USER", "STAFF", "ADMIN")

                        // Staff
                        .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")

                        // Admin only
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")                    // ← Cách mới, không cần AntPathRequestMatcher
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());  // Tạm tắt khi đang dev

        return http.build();
    }
}