package org.tutorbooking.security;

import lombok.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPrincipal implements UserDetails {

    private Long id;
    private String email;
    private String password;
    private String role;

    // =========================================
    // GRANTED AUTHORITY (ROLE)
    // =========================================
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    // =========================================
    // USERNAME = EMAIL
    // =========================================
    @Override
    public String getUsername() {
        return email;
    }

    // =========================================
    // PASSWORD
    // =========================================
    @Override
    public String getPassword() {
        return password;
    }

    // =========================================
    // ACCOUNT STATUS
    // =========================================
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}