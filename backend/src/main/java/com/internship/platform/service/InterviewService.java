package com.internship.platform.service;

import com.internship.platform.dto.InterviewDto;
import com.internship.platform.exception.*;
import com.internship.platform.model.entity.*;
import com.internship.platform.model.enums.*;
import com.internship.platform.repository.*;
import com.internship.platform.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Interview scheduling with ownership guards. */
@Service
@Transactional(readOnly = true)
public class InterviewService {

    private final InterviewRepository interviews;
    private final ApplicationRepository applications;
    private final StudentRepository students;
    private final CompanyRepository companies;
    private final SecurityUtils security;
    private final NotificationService notifications;
    private final AuditService audit;

    public InterviewService(InterviewRepository interviews, ApplicationRepository applications,
                            StudentRepository students, CompanyRepository companies,
                            SecurityUtils security, NotificationService notifications,
                            AuditService audit) {
        this.interviews = interviews;
        this.applications = applications;
        this.students = students;
        this.companies = companies;
        this.security = security;
        this.notifications = notifications;
        this.audit = audit;
    }

    /** Schedule an interview for an application owned by the current company. */
    @Transactional
    public InterviewDto.Response schedule(InterviewDto.Request req) {
        User u = security.currentUser();
        Application a = applications.findById(req.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!a.getJobPosting().getCompany().getUser().getId().equals(u.getId())) {
            throw new ForbiddenException("Not your application");
        }
        if (req.getScheduledAt() == null || req.getScheduledAt().isBefore(Instant.now())) {
            throw new BadRequestException("Interview must be scheduled in the future");
        }
        Interview iv = new Interview();
        iv.setApplication(a);
        iv.setScheduledAt(req.getScheduledAt());
        iv.setMode(req.getMode());
        iv.setMeetingLink(req.getMeetingLink());
        iv.setInterviewerName(req.getInterviewerName());
        iv.setStatus(InterviewStatus.SCHEDULED);
        Interview saved = interviews.save(iv);
        if (a.getStatus() == ApplicationStatus.SHORTLISTED) {
            a.setStatus(ApplicationStatus.INTERVIEW);
        }
        audit.record(u.getId(), "SCHEDULE_INTERVIEW", "Interview", saved.getId());
        notifications.notify(a.getStudent().getUser(),
                "Interview scheduled",
                "Interview for " + a.getJobPosting().getTitle() + " at " + req.getScheduledAt());
        return toResponse(saved);
    }

    /** Interviews visible to the current user (student or company). */
    public List<InterviewDto.Response> mine() {
        User u = security.currentUser();
        return switch (u.getRole()) {
            case STUDENT -> {
                Student s = students.findByUserId(u.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
                yield interviews.findByApplicationStudentId(s.getId()).stream()
                        .map(InterviewService::toResponse).toList();
            }
            case COMPANY -> {
                Company c = companies.findByUserId(u.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
                yield interviews.findByApplicationJobPostingCompanyId(c.getId()).stream()
                        .map(InterviewService::toResponse).toList();
            }
            default -> interviews.findAll().stream().map(InterviewService::toResponse).toList();
        };
    }

    /** Map entity to response DTO. */
    public static InterviewDto.Response toResponse(Interview iv) {
        InterviewDto.Response r = new InterviewDto.Response();
        r.setId(iv.getId());
        r.setApplicationId(iv.getApplication().getId());
        r.setScheduledAt(iv.getScheduledAt());
        r.setMode(iv.getMode());
        r.setStatus(iv.getStatus());
        r.setMeetingLink(iv.getMeetingLink());
        r.setInterviewerName(iv.getInterviewerName());
        return r;
    }
}
