package com.internship.platform.service;

import com.internship.platform.dto.CompanyDto;
import com.internship.platform.exception.ResourceNotFoundException;
import com.internship.platform.model.entity.Company;
import com.internship.platform.model.entity.User;
import com.internship.platform.repository.CompanyRepository;
import com.internship.platform.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Company profile management + admin verification. */
@Service
public class CompanyService {

    private final CompanyRepository companies;
    private final SecurityUtils security;
    private final AuditService audit;

    public CompanyService(CompanyRepository companies, SecurityUtils security, AuditService audit) {
        this.companies = companies;
        this.security = security;
        this.audit = audit;
    }

    /** Current company's profile. */
    public CompanyDto.Response me() {
        User u = security.currentUser();
        Company c = companies.findByUserId(u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
        return toResponse(c);
    }

    /** Update current company's profile. */
    @Transactional
    public CompanyDto.Response update(CompanyDto.UpdateRequest req) {
        User u = security.currentUser();
        Company c = companies.findByUserId(u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
        if (req.getName() != null) c.setCompanyName(req.getName());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        if (req.getWebsite() != null) c.setWebsite(req.getWebsite());
        if (req.getIndustry() != null) c.setIndustry(req.getIndustry());
        if (req.getLocation() != null) c.setLocation(req.getLocation());
        audit.record(u.getId(), "UPDATE_PROFILE", "Company", c.getId());
        return toResponse(c);
    }

    /** List all companies (admin). */
    public List<CompanyDto.Response> listAll() {
        return companies.findAll().stream().map(CompanyService::toResponse).toList();
    }

    /** Verify a company (admin only). */
    @Transactional
    public CompanyDto.Response verify(Long id, boolean verified) {
        Company c = companies.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
        c.setVerified(verified);
        audit.record(security.currentUser().getId(), verified ? "VERIFY_COMPANY" : "UNVERIFY_COMPANY",
                "Company", id);
        return toResponse(c);
    }

    /** Map entity to response DTO. */
    public static CompanyDto.Response toResponse(Company c) {
        CompanyDto.Response r = new CompanyDto.Response();
        r.setId(c.getId());
        r.setUserId(c.getUser().getId());
        r.setName(c.getCompanyName());
        r.setEmail(c.getUser().getEmail());
        r.setDescription(c.getDescription());
        r.setWebsite(c.getWebsite());
        r.setIndustry(c.getIndustry());
        r.setLocation(c.getLocation());
        r.setVerified(c.isVerified());
        return r;
    }
}
