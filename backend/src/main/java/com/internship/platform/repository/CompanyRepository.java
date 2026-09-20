package com.internship.platform.repository;

import com.internship.platform.model.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByUserId(Long userId);
    List<Company> findByVerified(boolean verified);
    long countByVerified(boolean verified);
}
