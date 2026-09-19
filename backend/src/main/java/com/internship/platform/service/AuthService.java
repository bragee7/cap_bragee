package com.internship.platform.service;

import com.internship.platform.dto.*;
import com.internship.platform.exception.BadRequestException;
import com.internship.platform.model.entity.*;
import com.internship.platform.model.enums.Role;
import com.internship.platform.repository.*;
import com.internship.platform.security.JwtService;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** Registration + login with JWT issuance. */
@Service
public class AuthService {

    private final UserRepository users;
    private final StudentRepository students;
    private final CompanyRepository companies;
    private final PasswordEncoder passwords;
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final AuditService audit;

    public AuthService(UserRepository users, StudentRepository students, CompanyRepository companies,
                       PasswordEncoder passwords, AuthenticationManager authManager,
                       JwtService jwt, AuditService audit) {
        this.users = users;
        this.students = students;
        this.companies = companies;
        this.passwords = passwords;
        this.authManager = authManager;
        this.jwt = jwt;
        this.audit = audit;
    }

    /** Register a new STUDENT/COMPANY user (ADMIN seeded via migration). */
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (req.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts are seeded, not self-registered");
        }
        String email = req.getEmail().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }
        User u = new User();
        u.setName(req.getName().trim());
        u.setEmail(email);
        u.setPassword(passwords.encode(req.getPassword()));
        u.setRole(req.getRole());
        u.setEnabled(true);
        User saved = users.save(u);

        if (req.getRole() == Role.STUDENT) {
            Student s = new Student();
            s.setUser(saved);
            s.setCollegeName("Unspecified");
            s.setDepartment("Unspecified");
            s.setGraduationYear(java.time.Year.now().getValue() + 4);
            s.setCgpa(java.math.BigDecimal.ZERO);
            students.save(s);
        } else {
            Company c = new Company();
            c.setUser(saved);
            c.setCompanyName(saved.getName());
            c.setVerified(false);
            companies.save(c);
        }
        audit.record(saved.getId(), "REGISTER", "User", saved.getId());
        String token = jwt.generate(saved.getEmail(), Map.of("role", saved.getRole().name()));
        return new AuthResponse(token, saved.getId(), saved.getName(), saved.getEmail(), saved.getRole());
    }

    /** Authenticate and issue a JWT. */
    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.getPassword()));
        User u = users.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        if (!u.isEnabled()) {
            throw new BadRequestException("Account disabled");
        }
        audit.record(u.getId(), "LOGIN", "User", u.getId());
        String token = jwt.generate(u.getEmail(), Map.of("role", u.getRole().name()));
        return new AuthResponse(token, u.getId(), u.getName(), u.getEmail(), u.getRole());
    }
}
