package com.example.hay_mart.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.hay_mart.models.User;

@Service
public class GetAuthoritiesImpl implements GetAuthorities {
    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Pengguna tidak terautentikasi");
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new RuntimeException("Detail pengguna tidak valid");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (userDetails.getUser() == null) {
            throw new RuntimeException("Detail pengguna tidak ditemukan");
        }

        return userDetails.getUser();
    }
}
