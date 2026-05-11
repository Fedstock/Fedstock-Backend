package com.fedstock.backend.auth.application;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fedstock.backend.auth.infrastructure.UserEntity;
import com.fedstock.backend.auth.infrastructure.UserJpaRepository;
import com.fedstock.backend.main.error.ConflictException;
import com.fedstock.backend.main.error.UnauthorizedException;

@Service
public class AuthService {

    private final UserJpaRepository userRepository;
    private final PasswordHashService passwordHashService;
    private final JwtService jwtService;

    public AuthService(
        UserJpaRepository userRepository,
        PasswordHashService passwordHashService,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordHashService = passwordHashService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResult register(String email, String password, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists.");
        }

        UserEntity user = userRepository.save(new UserEntity(email, passwordHashService.hash(password), name));
        return result(user);
    }

    @Transactional(readOnly = true)
    public AuthResult login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Email or password is invalid."));

        if (!passwordHashService.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Email or password is invalid.");
        }

        return result(user);
    }

    @Transactional(readOnly = true)
    public UserPrincipal findPrincipal(Long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("User is not available."));

        return new UserPrincipal(user.getId(), user.getEmail(), user.getName());
    }

    @Transactional(readOnly = true)
    public UserPrincipal findUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found."));

        return new UserPrincipal(user.getId(), user.getEmail(), user.getName());
    }

    private AuthResult result(UserEntity user) {
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getEmail(), user.getName());
        return new AuthResult(jwtService.issue(user.getId(), user.getEmail()), principal);
    }
}
