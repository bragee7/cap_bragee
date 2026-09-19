package com.internship.platform.repository;

import com.internship.platform.model.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    Optional<Offer> findByApplicationId(Long applicationId);
    List<Offer> findByApplicationStudentId(Long studentId);
    List<Offer> findByApplicationJobPostingCompanyId(Long companyId);
}
