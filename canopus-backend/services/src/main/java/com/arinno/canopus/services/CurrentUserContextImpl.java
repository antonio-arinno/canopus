package com.arinno.canopus.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.organization.user.application.UserService;

@Service
public class CurrentUserContextImpl implements CurrentUserContext {

    private final UserService userService;

    public CurrentUserContextImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("No authenticated user found in SecurityContext");
        }

        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
    }

    @Override
    public Company getCurrentCompany() {
        return getCurrentUser().getCompany();
    }
}