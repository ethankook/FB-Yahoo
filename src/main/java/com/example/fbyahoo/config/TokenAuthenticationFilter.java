package com.example.fbyahoo.config;

import com.example.fbyahoo.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public TokenAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip filter for non-API paths
        String path = request.getRequestURI();
        if (!path.startsWith("/api/") || path.equals("/api/auth/status")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if we have a valid token in the database
        try {
            String token = tokenService.getValidAccessToken();
            if (token != null) {
                // Set authentication in security context
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            // No valid token - let Spring Security handle 401
        }

        filterChain.doFilter(request, response);
    }
}
