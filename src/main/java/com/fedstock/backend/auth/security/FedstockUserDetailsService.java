package com.fedstock.backend.auth.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fedstock.backend.auth.infrastructure.UserJpaRepository;

@Service
public class FedstockUserDetailsService implements UserDetailsService {

    private final UserJpaRepository userRepository;

    public FedstockUserDetailsService(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findFirstByEmailOrStoreIdOrUsername(username, username, username)
            .map(SecurityUserDetails::new)
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
}
