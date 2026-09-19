package com.internship.platform.controller;

import com.internship.platform.dto.*;
import com.internship.platform.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Company self-service profile. */
@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companies;

    public CompanyController(CompanyService companies) {
        this.companies = companies;
    }

    /** Get my company profile. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<CompanyDto.Response>> me() {
        return ResponseEntity.ok(ApiResponse.ok(companies.me(), "Profile"));
    }

    /** Update my company profile. */
    @PutMapping("/me")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<CompanyDto.Response>> update(
            @Valid @RequestBody CompanyDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(companies.update(req), "Updated"));
    }
}
