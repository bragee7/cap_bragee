package com.internship.platform.model.entity;

import com.internship.platform.model.enums.JobStatus;
import com.internship.platform.model.enums.JobType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Job / internship posting owned by a Company.
 */
@Entity
@Table(name = "job_postings", indexes = {
        @Index(name = "idx_jobs_company", columnList = "company_id"),
        @Index(name = "idx_jobs_status", columnList = "status"),
        @Index(name = "idx_jobs_deadline", columnList = "deadline"),
        @Index(name = "idx_jobs_type", columnList = "job_type")
})
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Size(max = 5000)
    @Column(nullable = false, length = 5000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 20)
    private JobType jobType;

    @NotBlank
    @Size(max = 180)
    @Column(nullable = false, length = 180)
    private String location;

    @DecimalMin(value = "0.0")
    @Column(precision = 12, scale = 2)
    private BigDecimal salary;

    /** Comma-separated required skills, e.g. "Java,Spring Boot,SQL". */
    @Size(max = 1000)
    @Column(length = 1000)
    private String requiredSkills;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    @Column(precision = 4, scale = 2)
    private BigDecimal minimumCgpa;

    @NotNull
    @Future
    @Column(nullable = false)
    private Instant deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status = JobStatus.OPEN;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public JobType getJobType() { return jobType; }
    public void setJobType(JobType jobType) { this.jobType = jobType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    public BigDecimal getMinimumCgpa() { return minimumCgpa; }
    public void setMinimumCgpa(BigDecimal minimumCgpa) { this.minimumCgpa = minimumCgpa; }
    public Instant getDeadline() { return deadline; }
    public void setDeadline(Instant deadline) { this.deadline = deadline; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
