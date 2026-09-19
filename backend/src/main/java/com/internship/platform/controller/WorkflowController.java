package com.internship.platform.controller;

import com.internship.platform.dto.*;
import com.internship.platform.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Interviews, offers, admin, health, notifications. */
@RestController
public class WorkflowController {

    private final InterviewService interviews;
    private final OfferService offers;
    private final AdminService admin;
    private final CompanyService companies;
    private final NotificationService notifications;
    private final com.internship.platform.util.SecurityUtils security;

    public WorkflowController(InterviewService interviews, OfferService offers,
                              AdminService admin, CompanyService companies,
                              NotificationService notifications,
                              com.internship.platform.util.SecurityUtils security) {
        this.interviews = interviews;
        this.offers = offers;
        this.admin = admin;
        this.companies = companies;
        this.notifications = notifications;
        this.security = security;
    }

    @PostMapping("/api/interviews")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<InterviewDto.Response>> scheduleInterview(
            @Valid @RequestBody InterviewDto.Request req) {
        return ResponseEntity.ok(ApiResponse.ok(interviews.schedule(req), "Scheduled"));
    }

    @GetMapping("/api/interviews/mine")
    @PreAuthorize("hasAnyRole('STUDENT','COMPANY','ADMIN')")
    public ResponseEntity<ApiResponse<?>> myInterviews() {
        return ResponseEntity.ok(ApiResponse.ok(interviews.mine(), "Interviews"));
    }

    @PostMapping("/api/offers")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<OfferDto.Response>> issueOffer(
            @Valid @RequestBody OfferDto.Request req) {
        return ResponseEntity.ok(ApiResponse.ok(offers.issue(req), "Offer issued"));
    }

    @GetMapping("/api/offers/mine")
    @PreAuthorize("hasAnyRole('STUDENT','COMPANY','ADMIN')")
    public ResponseEntity<ApiResponse<?>> myOffers() {
        return ResponseEntity.ok(ApiResponse.ok(offers.mine(), "Offers"));
    }

    @PostMapping("/api/offers/{id}/respond")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<OfferDto.Response>> respond(
            @PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean accept = Boolean.TRUE.equals(body.get("accept"));
        return ResponseEntity.ok(ApiResponse.ok(offers.respond(id, accept), "Recorded"));
    }

    @GetMapping("/api/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(admin.stats(), "Stats"));
    }

    @GetMapping("/api/admin/companies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> allCompanies() {
        return ResponseEntity.ok(ApiResponse.ok(companies.listAll(), "Companies"));
    }

    @PostMapping("/api/admin/companies/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CompanyDto.Response>> verify(
            @PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean verified = !Boolean.FALSE.equals(body.get("verified"));
        return ResponseEntity.ok(ApiResponse.ok(companies.verify(id, verified), "Verified"));
    }

    @GetMapping("/api/notifications/mine")
    @PreAuthorize("hasAnyRole('STUDENT','COMPANY','ADMIN')")
    public ResponseEntity<ApiResponse<?>> myNotifications() {
        Long uid = security.currentUser().getId();
        return ResponseEntity.ok(ApiResponse.ok(notifications.forUser(uid), "Notifications"));
    }

    @GetMapping("/api/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("status", "UP"), "OK"));
    }
}
