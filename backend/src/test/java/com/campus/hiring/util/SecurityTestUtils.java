package com.campus.hiring.util;

import com.campus.hiring.domain.User;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityTestUtils {
    private SecurityTestUtils() {}
    public static void loginAs(User user) {
        var auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    public static void clear() { SecurityContextHolder.clearContext(); }
}
