package com.example.auth_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Email(message = "Email should be valid")  // Email validation
    @Column(unique = true, nullable = false)
    private String email;

    @Pattern(regexp = "^0\\d{9}$", message = "Phone number must be 10 digits and start with 0") // Phone number validation (example for Vietnam)
    @Column(unique = true, nullable = false)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Transient
    private String confirmPassword;

    private int facebookAccountId;

    private int googleAccountId;

    private boolean active;

    @Column(name = "img")
    private String avatarUrl;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> role.getName()); // Assuming Role has a 'name' field for role name
    }

    @Override
    public String getUsername() {
        return email;  // Use email as the username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // You can add your logic here if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // You can add your logic here if needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // You can add your logic here if needed
    }

    @Override
    public boolean isEnabled() {
        return active;  // Check if account is active
    }
}
