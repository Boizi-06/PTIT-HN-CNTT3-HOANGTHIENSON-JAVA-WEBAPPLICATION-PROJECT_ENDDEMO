package com.example.movietheater.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String role;
}