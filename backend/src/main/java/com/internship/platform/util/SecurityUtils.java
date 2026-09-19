package com.internship.platform.util;

import com.internship.platform.model.entity.User;
import com.internship.platform.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/** Resolves the currently authenticated user entity. */
@Component
public class SecurityUtils {

    private final UserRepository users;

    public SecurityUtils(UserRepository users) {
        this.users = users;
    }

    /** Current user email from the SecurityContext. */
    public String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UsernameNotFoundException("Not authenticated");
        }
        return auth.getName();
    }

    /** Current user entity. */
    public User currentUser() {
        return users.findByEmail(currentEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
