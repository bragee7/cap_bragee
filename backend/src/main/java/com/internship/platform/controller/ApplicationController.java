package com.internship.platform.controller;

import com.internship.platform.dto.*;
import com.internship.platform.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Application endpoints for students and companies. */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applications;

    public ApplicationController(ApplicationService applications) {
        this.applications = applications;
    }

    /** Apply to a job (student). */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ApplicationDto.Response>> apply(
            @Valid @RequestBody ApplicationDto.ApplyRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(applications.apply(req), "Applied"));
    }

    /** My applications (student). */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<?>> mine() {
        return ResponseEntity.ok(ApiResponse.ok(applications.myApplications(), "Applications"));
    }

    /** Applications to my jobs (company). */
    @GetMapping("/company")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<?>> forCompany() {
        return ResponseEntity.ok(ApiResponse.ok(applications.companyApplications(), "Applications"));
    }

    /** Update application status (company/admin). */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('COMPANY','ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationDto.Response>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody ApplicationDto.StatusRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                applications.updateStatus(id, req.getStatus()), "Status updated"));
    }

    /** Withdraw my application (student). */
    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<?>> withdraw(@PathVariable Long id) {
        applications.withdraw(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Withdrawn"));
    }
}
