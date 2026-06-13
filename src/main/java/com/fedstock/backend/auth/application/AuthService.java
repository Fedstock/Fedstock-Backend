package com.fedstock.backend.auth.application;

import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fedstock.backend.auth.infrastructure.UserEntity;
import com.fedstock.backend.auth.infrastructure.UserJpaRepository;
import com.fedstock.backend.main.error.BadRequestException;
import com.fedstock.backend.main.error.ConflictException;
import com.fedstock.backend.main.error.UnauthorizedException;

@Service
public class AuthService {

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
        UserJpaRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResult register(String email, String username, String storeId, String password, String name) {
        if (userRepository.existsByEmailOrStoreIdOrUsername(email, storeId, username)) {
            throw new ConflictException("User already exists.");
        }

        UserEntity user = userRepository.save(new UserEntity(email, username, storeId, passwordEncoder.encode(password), name));
        return result(user);
    }

    @Transactional(readOnly = true)
    public AuthResult login(String email, String storeId, String username, String password) {
        String identifier = firstNonBlank(email, storeId, username);
        if (identifier == null) {
            throw new BadRequestException("email, storeId, or username is required.");
        }

        UserEntity user = userRepository.findFirstByEmailOrStoreIdOrUsername(identifier, identifier, identifier)
            .orElseThrow(() -> new UnauthorizedException("Email or password is invalid."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Email or password is invalid.");
        }

        return result(user);
    }

    @Transactional(readOnly = true)
    public UserPrincipal findPrincipal(Long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("User is not available."));

        return new UserPrincipal(user.getId(), user.getEmail(), user.getStoreId(), user.getName(), user.getRole());
    }

    @Transactional(readOnly = true)
    public UserPrincipal findUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found."));

        return new UserPrincipal(user.getId(), user.getEmail(), user.getStoreId(), user.getName(), user.getRole());
    }

    private AuthResult result(UserEntity user) {
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getEmail(), user.getStoreId(), user.getName(), user.getRole());
        return new AuthResult(principal);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
