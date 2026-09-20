package com.internship.platform.repository;

import com.internship.platform.model.entity.Application;
import com.internship.platform.model.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByStudentIdAndJobPostingId(Long studentId, Long jobPostingId);
    boolean existsByStudentIdAndJobPostingId(Long studentId, Long jobPostingId);
    List<Application> findByStudentId(Long studentId);
    Page<Application> findByStudentId(Long studentId, Pageable pageable);
    List<Application> findByJobPostingId(Long jobPostingId);
    List<Application> findByJobPostingCompanyId(Long companyId);
    List<Application> findByStatus(ApplicationStatus status);
    long countByStatus(ApplicationStatus status);
}
