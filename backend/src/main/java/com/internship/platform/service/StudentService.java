package com.internship.platform.service;

import com.internship.platform.dto.StudentDto;
import com.internship.platform.exception.ResourceNotFoundException;
import com.internship.platform.model.entity.Student;
import com.internship.platform.model.entity.User;
import com.internship.platform.repository.StudentRepository;
import com.internship.platform.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Student profile management. */
@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository students;
    private final SecurityUtils security;
    private final AuditService audit;

    public StudentService(StudentRepository students, SecurityUtils security, AuditService audit) {
        this.students = students;
        this.security = security;
        this.audit = audit;
    }

    /** Current student's profile. */
    public StudentDto.Response me() {
        User u = security.currentUser();
        Student s = students.findByUserId(u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
        return toResponse(s);
    }

    /** Update current student's profile. */
    @Transactional
    public StudentDto.Response update(StudentDto.UpdateRequest req) {
        User u = security.currentUser();
        Student s = students.findByUserId(u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
        if (req.getCollege() != null) s.setCollegeName(req.getCollege());
        if (req.getDepartment() != null) s.setDepartment(req.getDepartment());
        if (req.getYear() != null) s.setGraduationYear(req.getYear());
        if (req.getCgpa() != null) s.setCgpa(req.getCgpa());
        if (req.getSkills() != null) s.setSkills(String.join(",", req.getSkills()));
        if (req.getResumeUrl() != null) s.setResumeUrl(req.getResumeUrl());
        audit.record(u.getId(), "UPDATE_PROFILE", "Student", s.getId());
        return toResponse(s);
    }

    /** Map entity to response DTO. */
    public static StudentDto.Response toResponse(Student s) {
        StudentDto.Response r = new StudentDto.Response();
        r.setId(s.getId());
        r.setUserId(s.getUser().getId());
        r.setName(s.getUser().getName());
        r.setEmail(s.getUser().getEmail());
        r.setCollege(s.getCollegeName());
        r.setDepartment(s.getDepartment());
        r.setYear(s.getGraduationYear());
        r.setCgpa(s.getCgpa());
        r.setSkills(com.internship.platform.util.MatchingService.split(s.getSkills()));
        r.setResumeUrl(s.getResumeUrl());
        return r;
    }
}
