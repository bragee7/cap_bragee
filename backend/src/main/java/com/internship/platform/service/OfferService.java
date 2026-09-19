package com.internship.platform.service;

import com.internship.platform.dto.OfferDto;
import com.internship.platform.exception.*;
import com.internship.platform.model.entity.*;
import com.internship.platform.model.enums.*;
import com.internship.platform.repository.*;
import com.internship.platform.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Offer issue/accept/decline with single-active-offer guard. */
@Service
public class OfferService {

    private final OfferRepository offers;
    private final ApplicationRepository applications;
    private final StudentRepository students;
    private final CompanyRepository companies;
    private final SecurityUtils security;
    private final NotificationService notifications;
    private final AuditService audit;

    public OfferService(OfferRepository offers, ApplicationRepository applications,
                        StudentRepository students, CompanyRepository companies,
                        SecurityUtils security, NotificationService notifications,
                        AuditService audit) {
        this.offers = offers;
        this.applications = applications;
        this.students = students;
        this.companies = companies;
        this.security = security;
        this.notifications = notifications;
        this.audit = audit;
    }

    /** Issue an offer for an application owned by the current company. */
    @Transactional
    public OfferDto.Response issue(OfferDto.Request req) {
        User u = security.currentUser();
        Application a = applications.findById(req.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!a.getJobPosting().getCompany().getUser().getId().equals(u.getId())) {
            throw new ForbiddenException("Not your application");
        }
        if (offers.findByApplicationId(a.getId()).isPresent()) {
            throw new BadRequestException("Offer already issued for this application");
        }
        Offer o = new Offer();
        o.setApplication(a);
        o.setStipend(req.getStipend());
        o.setJoiningDate(req.getJoiningDate());
        o.setDetails(req.getDetails());
        o.setStatus(OfferStatus.PENDING);
        Offer saved = offers.save(o);
        a.setStatus(ApplicationStatus.OFFERED);
        audit.record(u.getId(), "ISSUE_OFFER", "Offer", saved.getId());
        notifications.notify(a.getStudent().getUser(),
                "Offer received",
                "You received an offer for " + a.getJobPosting().getTitle());
        return toResponse(saved);
    }

    /** Respond to an offer (student owns the application). */
    @Transactional
    public OfferDto.Response respond(Long offerId, boolean accept) {
        User u = security.currentUser();
        Offer o = offers.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + offerId));
        if (!o.getApplication().getStudent().getUser().getId().equals(u.getId())) {
            throw new ForbiddenException("Not your offer");
        }
        if (o.getStatus() != OfferStatus.PENDING) {
            throw new BadRequestException("Offer already decided: " + o.getStatus());
        }
        o.setStatus(accept ? OfferStatus.ACCEPTED : OfferStatus.DECLINED);
        o.getApplication().setStatus(accept ? ApplicationStatus.ACCEPTED : ApplicationStatus.REJECTED);
        audit.record(u.getId(), accept ? "ACCEPT_OFFER" : "DECLINE_OFFER", "Offer", offerId);
        return toResponse(o);
    }

    /** Offers visible to the current user. */
    public List<OfferDto.Response> mine() {
        User u = security.currentUser();
        return switch (u.getRole()) {
            case STUDENT -> {
                Student s = students.findByUserId(u.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
                yield offers.findByApplicationStudentId(s.getId()).stream()
                        .map(OfferService::toResponse).toList();
            }
            case COMPANY -> {
                Company c = companies.findByUserId(u.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
                yield offers.findByApplicationJobPostingCompanyId(c.getId()).stream()
                        .map(OfferService::toResponse).toList();
            }
            default -> offers.findAll().stream().map(OfferService::toResponse).toList();
        };
    }

    /** Map entity to response DTO. */
    public static OfferDto.Response toResponse(Offer o) {
        OfferDto.Response r = new OfferDto.Response();
        r.setId(o.getId());
        r.setApplicationId(o.getApplication().getId());
        r.setStipend(o.getStipend());
        r.setJoiningDate(o.getJoiningDate());
        r.setStatus(o.getStatus());
        r.setDetails(o.getDetails());
        return r;
    }
}
