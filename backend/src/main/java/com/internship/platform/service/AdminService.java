package com.internship.platform.service;

import com.internship.platform.model.enums.ApplicationStatus;
import com.internship.platform.model.enums.JobStatus;
import com.internship.platform.repository.*;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/** Admin dashboard statistics. */
@Service
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
}
