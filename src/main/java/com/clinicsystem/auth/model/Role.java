package com.clinicsystem.auth.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;

@Data
@NoArgsConstructor
@Entity
@Table(name = "roles") // Avoids conflicts with reserved keywords
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., "PATIENT", "DOCTOR", "ADMIN"

    @Override
    public String getAuthority() {
        return name; // Return the role name as the authority
    }
}
