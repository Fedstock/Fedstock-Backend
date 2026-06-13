package com.fedstock.backend.auth.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fedstock.backend.auth.infrastructure.UserEntity;

public class SecurityUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String storeId;
    private final String name;
    private final String password;
    private final String role;

    public SecurityUserDetails(UserEntity user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.storeId = user.getStoreId();
        this.name = user.getName();
        this.password = user.getPasswordHash();
        this.role = user.getRole();
    }

    public Long id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String storeId() {
        return storeId;
    }

    public String name() {
        return name;
    }

    public String role() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

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
