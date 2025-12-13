package com.clinicsystem.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor // Required for frameworks like Jackson (JSON parsing)
@AllArgsConstructor // This fixes the constructor issue
public class AuthResponse {
    private String token;
    private Long userId;
    private Set<String> roles;
}
