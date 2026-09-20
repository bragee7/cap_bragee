package com.internship.platform.service;

import com.internship.platform.dto.ApplicationDto;
import com.internship.platform.dto.CompanyDto;
import com.internship.platform.dto.JobDto;
import com.internship.platform.dto.UserResponse;
import com.internship.platform.model.enums.ApplicationStatus;
import com.internship.platform.model.enums.JobStatus;
import com.internship.platform.repository.*;
import com.internship.platform.service.ApplicationService;
import com.internship.platform.service.CompanyService;
import com.internship.platform.service.JobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Admin dashboard statistics. */
@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository users;
    private final CompanyRepository companies;
    private final JobPostingRepository jobs;
    private final ApplicationRepository applications;

    public AdminService(UserRepository users, CompanyRepository companies,
                        JobPostingRepository jobs, ApplicationRepository applications) {
        this.users = users;
        this.companies = companies;
        this.jobs = jobs;
        this.applications = applications;
    }

    /** Aggregate counts for the admin dashboard. */
    public Map<String, Object> stats() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("users", users.count());
        m.put("companies", companies.count());
        m.put("unverifiedCompanies", companies.countByVerified(false));
        m.put("openJobs", jobs.countByStatus(JobStatus.OPEN));
        m.put("totalJobs", jobs.count());
        for (ApplicationStatus s : ApplicationStatus.values()) {
            m.put("applications_" + s.name().toLowerCase(), applications.countByStatus(s));
        }
        return m;
    }

    public List<UserResponse> listUsers() {
        return users.findAll().stream().map(u -> {
            UserResponse r = new UserResponse();
            r.setId(u.getId());
            r.setName(u.getName());
            r.setEmail(u.getEmail());
            r.setRole(u.getRole());
            r.setEnabled(u.isEnabled());
            return r;
        }).toList();
    }

    public List<CompanyDto.Response> listCompanies(Boolean verified) {
        List<com.internship.platform.model.entity.Company> list = verified == null ? companies.findAll() : companies.findByVerified(verified);
        return list.stream().map(CompanyService::toResponse).toList();
    }

    public List<JobDto.Response> listJobs(JobStatus status) {
        List<com.internship.platform.model.entity.JobPosting> list = status == null ? jobs.findAll() : jobs.findByStatus(status);
        return list.stream().map(j -> JobService.toResponse(j, 0.0)).toList();
    }

    public List<ApplicationDto.Response> listApplications(ApplicationStatus status) {
        List<com.internship.platform.model.entity.Application> list = status == null ? applications.findAll() : applications.findByStatus(status);
        return list.stream().map(ApplicationService::toResponse).toList();
    }
}
