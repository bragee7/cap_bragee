package com.internship.platform.service;

import com.internship.platform.dto.ApplicationDto;
import com.internship.platform.exception.*;
import com.internship.platform.model.entity.*;
import com.internship.platform.model.enums.*;
import com.internship.platform.repository.*;
import com.internship.platform.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Application workflow with eligibility + state-machine guards. */
@Service
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applications;
    private final JobPostingRepository jobs;
    private final StudentRepository students;
    private final CompanyRepository companies;
    private final SecurityUtils security;
    private final NotificationService notifications;
    private final AuditService audit;

    public ApplicationService(ApplicationRepository applications, JobPostingRepository jobs,
                              StudentRepository students, CompanyRepository companies,
                              SecurityUtils security, NotificationService notifications,
                              AuditService audit) {
        this.applications = applications;
        this.jobs = jobs;
        this.students = students;
        this.companies = companies;
        this.security = security;
        this.notifications = notifications;
        this.audit = audit;
    }

    /** Apply to a job (BR-01..BR-04 enforced). */
    @Transactional
    public ApplicationDto.Response apply(ApplicationDto.ApplyRequest req) {
        User u = security.currentUser();
        Student s = students.findByUserId(u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
        JobPosting job = jobs.findById(req.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + req.getJobId()));

        // BR-02: only OPEN jobs
        if (job.getStatus() != JobStatus.OPEN) {
            throw new BadRequestException("Job is not open for applications");
        }
        // BR-03: deadline
        if (job.getDeadline() != null && Instant.now().isAfter(job.getDeadline())) {
            throw new BadRequestException("Application deadline has passed");
        }
        // BR-01: duplicate
        if (applications.existsByStudentIdAndJobPostingId(s.getId(), job.getId())) {
            throw new BadRequestException("You have already applied to this job");
        }
        // BR-04: CGPA
        if (job.getMinimumCgpa() != null && s.getCgpa() != null
                && s.getCgpa().compareTo(job.getMinimumCgpa()) < 0) {
            throw new BadRequestException("CGPA below minimum requirement (" + job.getMinimumCgpa() + ")");
        }

        Application a = new Application();
        a.setStudent(s);
        a.setJobPosting(job);
        a.setStatus(ApplicationStatus.APPLIED);
        a.setCoverLetter(req.getCoverLetter());
        Application saved = applications.save(a);
        audit.record(u.getId(), "APPLY", "Application", saved.getId());
        notifications.notify(job.getCompany().getUser(),
                "New application: " + job.getTitle(),
                s.getUser().getName() + " applied to " + job.getTitle());
        return toResponse(saved);
    }

    /** Current student's applications. */
    public List<ApplicationDto.Response> myApplications() {
        User u = security.currentUser();
        Student s = students.findByUserId(u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
        return applications.findByStudentId(s.getId()).stream()
                .map(ApplicationService::toResponse).toList();
    }

    /** Applications for the current company's jobs. */
    public List<ApplicationDto.Response> companyApplications() {
        User u = security.currentUser();
        Company c = companies.findByUserId(u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
        return applications.findByJobPostingCompanyId(c.getId()).stream()
                .map(ApplicationService::toResponse).toList();
    }

    /** Update status with forward-only state machine (BR-05). */
    @Transactional
    public ApplicationDto.Response updateStatus(Long id, ApplicationStatus next) {
        User u = security.currentUser();
        Application a = applications.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
        boolean owner = a.getJobPosting().getCompany().getUser().getId().equals(u.getId());
        boolean admin = u.getRole() == com.internship.platform.model.enums.Role.ADMIN;
        if (!owner && !admin) {
            throw new ForbiddenException("Not your application to update");
        }
        if (!isForward(a.getStatus(), next)) {
            throw new BadRequestException(
                    "Invalid transition: " + a.getStatus() + " -> " + next);
        }
        a.setStatus(next);
        audit.record(u.getId(), "UPDATE_STATUS_" + next, "Application", id);
        notifications.notify(a.getStudent().getUser(),
                "Application " + next.name().toLowerCase(),
                "Your application for " + a.getJobPosting().getTitle() + " is now " + next);
        return toResponse(a);
    }

    /** Withdraw own application (student, only before decision). */
    @Transactional
    public void withdraw(Long id) {
        User u = security.currentUser();
        Application a = applications.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
        if (!a.getStudent().getUser().getId().equals(u.getId())) {
            throw new ForbiddenException("Not your application");
        }
        if (a.getStatus() != ApplicationStatus.APPLIED
                && a.getStatus() != ApplicationStatus.SHORTLISTED) {
            throw new BadRequestException("Cannot withdraw after decision: " + a.getStatus());
        }
        a.setStatus(ApplicationStatus.WITHDRAWN);
        audit.record(u.getId(), "WITHDRAW", "Application", id);
    }

    /** Forward-only transitions: APPLIED -> UNDER_REVIEW -> SHORTLISTED
     * -> INTERVIEW -> SELECTED -> OFFERED -> ACCEPTED, with REJECTED /
     * WITHDRAWN exits along the way. */
    static boolean isForward(ApplicationStatus from, ApplicationStatus to) {
        return switch (from) {
            case APPLIED -> to == ApplicationStatus.UNDER_REVIEW
                    || to == ApplicationStatus.SHORTLISTED
                    || to == ApplicationStatus.REJECTED
                    || to == ApplicationStatus.WITHDRAWN;
            case UNDER_REVIEW -> to == ApplicationStatus.SHORTLISTED
                    || to == ApplicationStatus.REJECTED
                    || to == ApplicationStatus.WITHDRAWN;
            case SHORTLISTED -> to == ApplicationStatus.INTERVIEW
                    || to == ApplicationStatus.REJECTED
                    || to == ApplicationStatus.WITHDRAWN;
            case INTERVIEW -> to == ApplicationStatus.SELECTED
                    || to == ApplicationStatus.REJECTED;
            case SELECTED -> to == ApplicationStatus.OFFERED
                    || to == ApplicationStatus.REJECTED;
            case OFFERED -> to == ApplicationStatus.ACCEPTED
                    || to == ApplicationStatus.REJECTED;
            default -> false; // terminal states
        };
    }

    /** Map entity to response DTO. */
    public static ApplicationDto.Response toResponse(Application a) {
        ApplicationDto.Response r = new ApplicationDto.Response();
        r.setId(a.getId());
        r.setStudentId(a.getStudent().getId());
        r.setStudentName(a.getStudent().getUser().getName());
        r.setJobId(a.getJobPosting().getId());
        r.setJobTitle(a.getJobPosting().getTitle());
        r.setCompanyId(a.getJobPosting().getCompany().getId());
        r.setCompanyName(a.getJobPosting().getCompany().getCompanyName());
        r.setStatus(a.getStatus());
        r.setCoverLetter(a.getCoverLetter());
        r.setAppliedAt(a.getAppliedAt());
        return r;
    }
}
