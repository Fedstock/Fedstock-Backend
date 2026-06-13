package com.fedstock.backend.auth.security;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final FedstockUserDetailsService userDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
        JwtTokenProvider jwtTokenProvider,
        FedstockUserDetailsService userDetailsService,
        AuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!header.startsWith(BEARER_PREFIX)) {
            request.setAttribute("auth.error.message", "Authorization header must use Bearer token.");
            authenticationEntryPoint.commence(request, response, new BadCredentialsException("Invalid token type."));
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());

        try {
            jwtTokenProvider.isValid(token);
            String username = jwtTokenProvider.getUsername(token);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException exception) {
            request.setAttribute("auth.error.message", "JWT token is expired.");
            authenticationEntryPoint.commence(request, response, new BadCredentialsException("JWT token is expired.", exception));
            return;
        } catch (JwtException | IllegalArgumentException exception) {
            request.setAttribute("auth.error.message", "JWT token is invalid.");
            authenticationEntryPoint.commence(request, response, new BadCredentialsException("JWT token is invalid.", exception));
            return;
        } catch (AuthenticationException exception) {
            request.setAttribute("auth.error.message", "JWT token user is not available.");
            authenticationEntryPoint.commence(request, response, new BadCredentialsException("JWT token user is not available.", exception));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
