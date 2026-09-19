package com.internship.platform.service;

import com.internship.platform.dto.*;
import com.internship.platform.exception.BadRequestException;
import com.internship.platform.exception.ResourceNotFoundException;
import com.internship.platform.model.entity.*;
import com.internship.platform.model.enums.*;
import com.internship.platform.repository.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/** End-to-end business-rule tests (BR-01..BR-10) on H2. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlacementWorkflowTest {

    @Autowired AuthService authService;
    @Autowired JobService jobService;
    @Autowired ApplicationService applicationService;
    @Autowired InterviewService interviewService;
    @Autowired OfferService offerService;
    @Autowired AdminService adminService;
    @Autowired StudentRepository students;
    @Autowired CompanyRepository companies;
    @Autowired JobPostingRepository jobs;
    @Autowired UserRepository users;

    private User studentUser;
    private User companyUser;
    private Student student;
    private Company company;
    private JobPosting job;

    @BeforeEach
    void setup() {
        studentUser = register("stu", "stu@test.com", Role.STUDENT);
        companyUser = register("comp", "comp@test.com", Role.COMPANY);
        student = students.findByUserId(studentUser.getId()).orElseThrow();
        student.setCgpa(new BigDecimal("8.50"));
        student.setSkills("Java,Spring,SQL");
        students.save(student);
        company = companies.findByUserId(companyUser.getId()).orElseThrow();
        loginAs(companyUser);
        JobDto.Request req = new JobDto.Request();
        req.setTitle("SDE Intern");
        req.setDescription("Java role");
        req.setLocation("Bengaluru");
        req.setJobType(JobType.INTERNSHIP);
        req.setSalary(new BigDecimal("60000"));
        req.setMinimumCgpa(new BigDecimal("7.0"));
        req.setRequiredSkills("Java,Spring");
        req.setDeadline(Instant.now().plus(30, ChronoUnit.DAYS));
        job = jobs.findById(jobService.create(req).getId()).orElseThrow();
        SecurityContextHolder.clearContext();
    }

    private User register(String name, String email, Role role) {
        RegisterRequest r = new RegisterRequest();
        r.setName(name); r.setEmail(email); r.setPassword("Password1!"); r.setRole(role);
        AuthResponse resp = authService.register(r);
        return users.findById(resp.getId()).orElseThrow();
    }

    private void loginAs(User u) {
        var auth = new UsernamePasswordAuthenticationToken(u.getEmail(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private ApplicationDto.Response applyAsStudent() {
        loginAs(studentUser);
        ApplicationDto.ApplyRequest ar = new ApplicationDto.ApplyRequest();
        ar.setJobId(job.getId());
        ar.setCoverLetter("I am a fit");
        return applicationService.apply(ar);
    }

    @Test
    void br01_duplicateApplicationRejected() {
        applyAsStudent();
        assertThrows(BadRequestException.class, this::applyAsStudent);
    }

    @Test
    void br02_closedJobRejectsApplications() {
        loginAs(companyUser);
        jobService.close(job.getId());
        assertThrows(BadRequestException.class, this::applyAsStudent);
    }

    @Test
    void br03_pastDeadlineRejectsApplications() {
        job.setDeadline(Instant.now().minus(1, ChronoUnit.DAYS));
        jobs.save(job);
        assertThrows(BadRequestException.class, this::applyAsStudent);
    }

    @Test
    void br04_cgpaBelowMinimumRejected() {
        job.setMinimumCgpa(new BigDecimal("9.9"));
        jobs.save(job);
        assertThrows(BadRequestException.class, this::applyAsStudent);
    }

    @Test
    void br05_stateMachineOnlyForward() {
        ApplicationDto.Response a = applyAsStudent();
        loginAs(companyUser);
        applicationService.updateStatus(a.getId(), ApplicationStatus.SHORTLISTED);
        // backward transition must fail
        assertThrows(BadRequestException.class,
                () -> applicationService.updateStatus(a.getId(), ApplicationStatus.APPLIED));
        // skipping ahead must fail (SHORTLISTED -> OFFERED)
        assertThrows(BadRequestException.class,
                () -> applicationService.updateStatus(a.getId(), ApplicationStatus.OFFERED));
    }

    @Test
    void br06_interviewInFutureAndAdvancesStatus() {
        ApplicationDto.Response a = applyAsStudent();
        loginAs(companyUser);
        applicationService.updateStatus(a.getId(), ApplicationStatus.SHORTLISTED);
        InterviewDto.Request bad = new InterviewDto.Request();
        bad.setApplicationId(a.getId());
        bad.setScheduledAt(Instant.now().minus(2, ChronoUnit.HOURS));
        bad.setMode(InterviewMode.ONLINE);
        assertThrows(BadRequestException.class, () -> interviewService.schedule(bad));
        InterviewDto.Request good = new InterviewDto.Request();
        good.setApplicationId(a.getId());
        good.setScheduledAt(Instant.now().plus(2, ChronoUnit.DAYS));
        good.setMode(InterviewMode.ONLINE);
        good.setMeetingLink("https://meet.example/1");
        good.setInterviewerName("Jane");
        interviewService.schedule(good);
        loginAs(studentUser);
        assertEquals(ApplicationStatus.INTERVIEW,
                applicationService.myApplications().get(0).getStatus());
    }

    @Test
    void br07_singleOfferPerApplication() {
        ApplicationDto.Response a = applyAsStudent();
        loginAs(companyUser);
        OfferDto.Request or = new OfferDto.Request();
        or.setApplicationId(a.getId());
        or.setSalary(new BigDecimal("900000"));
        or.setJoiningDate(LocalDate.now().plusDays(30));
        OfferDto.Response issued = offerService.issue(or);
        assertEquals(OfferStatus.PENDING, issued.getStatus());
        assertThrows(BadRequestException.class, () -> offerService.issue(or));
        // student accepts
        loginAs(studentUser);
        OfferDto.Response decided = offerService.respond(issued.getId(), true);
        assertEquals(OfferStatus.ACCEPTED, decided.getStatus());
    }

    @Test
    void br08_matchScoreBounded() {
        loginAs(studentUser);
        var page = jobService.search(null, null, null, 0, 10);
        assertFalse(page.isEmpty());
        page.forEach(j -> assertTrue(j.getMatchScore() >= 0 && j.getMatchScore() <= 1));
        assertTrue(page.getContent().get(0).getMatchScore() > 0);
    }

    @Test
    void br09_adminStatsPresent() {
        Map<String, Object> stats = adminService.stats();
        assertNotNull(stats);
        assertTrue(stats.containsKey("users") || !stats.isEmpty());
    }

    @Test
    void br10_onlyStudentCanApply() {
        loginAs(companyUser);
        ApplicationDto.ApplyRequest ar = new ApplicationDto.ApplyRequest();
        ar.setJobId(job.getId());
        assertThrows(ResourceNotFoundException.class, () -> applicationService.apply(ar));
    }
}
