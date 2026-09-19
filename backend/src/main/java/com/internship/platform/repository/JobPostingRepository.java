package com.internship.platform.repository;

import com.internship.platform.model.entity.JobPosting;
import com.internship.platform.model.enums.JobStatus;
import com.internship.platform.model.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByCompanyId(Long companyId);
    long countByStatus(JobStatus status);
    long countByCompanyIdAndStatus(Long companyId, JobStatus status);

    @Query("SELECT j FROM JobPosting j WHERE "
            + "(:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + " OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:jobType IS NULL OR j.jobType = :jobType) "
            + "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) "
            + "AND (:status IS NULL OR j.status = :status) "
            + "AND (:maxMinCgpa IS NULL OR j.minimumCgpa IS NULL OR j.minimumCgpa <= :maxMinCgpa)")
    Page<JobPosting> search(@Param("keyword") String keyword,
                            @Param("jobType") JobType jobType,
                            @Param("location") String location,
                            @Param("status") JobStatus status,
                            @Param("maxMinCgpa") BigDecimal studentCgpa,
                            Pageable pageable);
}
