package com.internship.platform.repository;

import com.internship.platform.model.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByApplicationId(Long applicationId);
    List<Interview> findByApplicationStudentId(Long studentId);
    List<Interview> findByApplicationJobPostingCompanyId(Long companyId);
}
