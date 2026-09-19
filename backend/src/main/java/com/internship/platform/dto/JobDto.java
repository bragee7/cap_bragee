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
        private BigDecimal stipend;
        private Integer durationWeeks;
        private Instant applicationDeadline;
        @DecimalMin("0.0") @DecimalMax("10.0") private BigDecimal minimumCgpa;
        private List<String> requiredSkills;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public JobType getJobType() { return jobType; }
        public void setJobType(JobType jobType) { this.jobType = jobType; }
        public BigDecimal getStipend() { return stipend; }
        public void setStipend(BigDecimal stipend) { this.stipend = stipend; }
        public Integer getDurationWeeks() { return durationWeeks; }
        public void setDurationWeeks(Integer durationWeeks) { this.durationWeeks = durationWeeks; }
        public Instant getApplicationDeadline() { return applicationDeadline; }
        public void setApplicationDeadline(Instant applicationDeadline) { this.applicationDeadline = applicationDeadline; }
        public BigDecimal getMinimumCgpa() { return minimumCgpa; }
        public void setMinimumCgpa(BigDecimal minimumCgpa) { this.minimumCgpa = minimumCgpa; }
        public List<String> getRequiredSkills() { return requiredSkills; }
        public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }
    }

    public static class Response {
        private Long id; private Long companyId; private String companyName;
        private String title; private String description; private String location;
        private JobType jobType; private JobStatus status;
        private BigDecimal stipend; private Integer durationWeeks;
        private Instant applicationDeadline; private BigDecimal minimumCgpa;
        private List<String> requiredSkills; private double matchScore;

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
        public BigDecimal getStipend() { return stipend; }
        public void setStipend(BigDecimal stipend) { this.stipend = stipend; }
        public Integer getDurationWeeks() { return durationWeeks; }
        public void setDurationWeeks(Integer durationWeeks) { this.durationWeeks = durationWeeks; }
        public Instant getApplicationDeadline() { return applicationDeadline; }
        public void setApplicationDeadline(Instant applicationDeadline) { this.applicationDeadline = applicationDeadline; }
        public BigDecimal getMinimumCgpa() { return minimumCgpa; }
        public void setMinimumCgpa(BigDecimal minimumCgpa) { this.minimumCgpa = minimumCgpa; }
        public List<String> getRequiredSkills() { return requiredSkills; }
        public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }
        public double getMatchScore() { return matchScore; }
        public void setMatchScore(double matchScore) { this.matchScore = matchScore; }
    }
}
