package com.internship.platform.service;

import com.internship.platform.dto.JobDto;
import com.internship.platform.exception.*;
import com.internship.platform.model.entity.*;
import com.internship.platform.model.enums.JobStatus;
import com.internship.platform.model.enums.JobType;
import com.internship.platform.repository.*;
import com.internship.platform.util.MatchingService;
import com.internship.platform.util.SecurityUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Job posting lifecycle with CGPA/eligibility enforcement. */
@Service
@Transactional(readOnly = true)
public class JobService {

    private final JobPostingRepository jobs;
    private final CompanyRepository companies;
    private final StudentRepository students;
    private final SecurityUtils security;
    private final MatchingService matching;
    private final AuditService audit;

    public JobService(JobPostingRepository jobs, CompanyRepository companies,
                      StudentRepository students, SecurityUtils security,
                      MatchingService matching, AuditService audit) {
        this.jobs = jobs;
        this.companies = companies;
        this.students = students;
        this.security = security;
        this.matching = matching;
        this.audit = audit;
    }

    /** Create a job for the current company. */
    @Transactional
    public JobDto.Response create(JobDto.Request req) {
        User u = security.currentUser();
        Company c = companies.findByUserId(u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
        JobPosting j = new JobPosting();
        j.setCompany(c);
        j.setTitle(req.getTitle());
        j.setDescription(req.getDescription());
        j.setLocation(req.getLocation());
        j.setJobType(req.getJobType());
        j.setSalary(req.getSalary());
        j.setDeadline(req.getDeadline());
        j.setMinimumCgpa(req.getMinimumCgpa());
        j.setRequiredSkills(req.getRequiredSkills());
        j.setStatus(JobStatus.OPEN);
        JobPosting saved = jobs.save(j);
        audit.record(u.getId(), "CREATE_JOB", "JobPosting", saved.getId());
        return toResponse(saved, 0.0);
    }

    /** Search open jobs; attaches match score for students. */
    public Page<JobDto.Response> search(String keyword, JobType jobType, String location,
                                        int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        BigDecimal cgpa = null;
        Student me = null;
        try {
            User u = security.currentUser();
            me = students.findByUserId(u.getId()).orElse(null);
            if (me != null) cgpa = me.getCgpa();
        } catch (Exception ignored) {
            // anonymous browsing allowed
        }
        Page<JobPosting> result = jobs.search(keyword, jobType, location, JobStatus.OPEN, cgpa, pageable);
        final Student fme = me;
        return result.map(j -> toResponse(j, fme == null ? 0.0 : matching.score(fme, j)));
    }

    /** Get one job by id. */
    public JobDto.Response get(Long id) {
        JobPosting j = jobs.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        double score = 0.0;
        try {
            User u = security.currentUser();
            Student s = students.findByUserId(u.getId()).orElse(null);
            if (s != null) score = matching.score(s, j);
        } catch (Exception ignored) {
        }
        return toResponse(j, score);
    }

    /** List jobs for the current company. */
    public List<JobDto.Response> myJobs() {
        User u = security.currentUser();
        Company c = companies.findByUserId(u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
        return jobs.findByCompanyId(c.getId()).stream()
                .map(j -> toResponse(j, 0.0)).toList();
    }

    /** Update a job owned by the current company. */
    @Transactional
    public JobDto.Response update(Long id, JobDto.Request req) {
        User u = security.currentUser();
        JobPosting j = jobs.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        if (!j.getCompany().getUser().getId().equals(u.getId())) {
            throw new ForbiddenException("Not your job posting");
        }
        if (req.getTitle() != null) j.setTitle(req.getTitle());
        if (req.getDescription() != null) j.setDescription(req.getDescription());
        if (req.getLocation() != null) j.setLocation(req.getLocation());
        if (req.getJobType() != null) j.setJobType(req.getJobType());
        if (req.getSalary() != null) j.setSalary(req.getSalary());
        if (req.getDeadline() != null) j.setDeadline(req.getDeadline());
        if (req.getMinimumCgpa() != null) j.setMinimumCgpa(req.getMinimumCgpa());
        if (req.getRequiredSkills() != null) j.setRequiredSkills(req.getRequiredSkills());
        audit.record(u.getId(), "UPDATE_JOB", "JobPosting", id);
        return toResponse(j, 0.0);
    }

    /** Close a job owned by the current company (or admin). */
    @Transactional
    public JobDto.Response close(Long id) {
        User u = security.currentUser();
        JobPosting j = jobs.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        boolean owner = j.getCompany().getUser().getId().equals(u.getId());
        boolean admin = u.getRole().name().equals("ADMIN");
        if (!owner && !admin) {
            throw new ForbiddenException("Not your job posting");
        }
        j.setStatus(JobStatus.CLOSED);
        audit.record(u.getId(), "CLOSE_JOB", "JobPosting", id);
        return toResponse(j, 0.0);
    }

    /** Map entity to response DTO. */
    public static JobDto.Response toResponse(JobPosting j, double score) {
        JobDto.Response r = new JobDto.Response();
        r.setId(j.getId());
        r.setCompanyId(j.getCompany().getId());
        r.setCompanyName(j.getCompany().getCompanyName());
        r.setTitle(j.getTitle());
        r.setDescription(j.getDescription());
        r.setLocation(j.getLocation());
        r.setJobType(j.getJobType());
        r.setStatus(j.getStatus());
        r.setSalary(j.getSalary());
        r.setDeadline(j.getDeadline());
        r.setMinimumCgpa(j.getMinimumCgpa());
        r.setRequiredSkills(j.getRequiredSkills());
        r.setMatchScore(score);
        return r;
    }
}
