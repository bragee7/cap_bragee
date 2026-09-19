package com.internship.platform.dto;

import com.internship.platform.model.enums.JobStatus;
import com.internship.platform.model.enums.JobType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Job posting payloads. */
public class JobDto {

    public static class Request {
        @NotBlank @Size(max = 200) private String title;
        @NotBlank private String description;
        @Size(max = 200) private String location;
        @NotNull private JobType jobType;
        private BigDecimal salary;
        private Instant deadline;
        @DecimalMin("0.0") @DecimalMax("10.0") private BigDecimal minimumCgpa;
        @Size(max = 1000) private String requiredSkills;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public JobType getJobType() { return jobType; }
        public void setJobType(JobType jobType) { this.jobType = jobType; }
        public BigDecimal getSalary() { return salary; }
        public void setSalary(BigDecimal salary) { this.salary = salary; }
        public Instant getDeadline() { return deadline; }
        public void setDeadline(Instant deadline) { this.deadline = deadline; }
        public BigDecimal getMinimumCgpa() { return minimumCgpa; }
        public void setMinimumCgpa(BigDecimal minimumCgpa) { this.minimumCgpa = minimumCgpa; }
        public String getRequiredSkills() { return requiredSkills; }
        public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    }

    public static class Response {
        private Long id; private Long companyId; private String companyName;
        private String title; private String description; private String location;
        private JobType jobType; private JobStatus status;
        private BigDecimal salary;
        private Instant deadline; private BigDecimal minimumCgpa;
        private String requiredSkills; private double matchScore;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public JobType getJobType() { return jobType; }
        public void setJobType(JobType jobType) { this.jobType = jobType; }
        public JobStatus getStatus() { return status; }
        public void setStatus(JobStatus status) { this.status = status; }
        public BigDecimal getSalary() { return salary; }
        public void setSalary(BigDecimal salary) { this.salary = salary; }
        public Instant getDeadline() { return deadline; }
        public void setDeadline(Instant deadline) { this.deadline = deadline; }
        public BigDecimal getMinimumCgpa() { return minimumCgpa; }
        public void setMinimumCgpa(BigDecimal minimumCgpa) { this.minimumCgpa = minimumCgpa; }
        public String getRequiredSkills() { return requiredSkills; }
        public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
        public double getMatchScore() { return matchScore; }
        public void setMatchScore(double matchScore) { this.matchScore = matchScore; }
    }
}
