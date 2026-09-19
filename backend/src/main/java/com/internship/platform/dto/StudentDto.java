package com.internship.platform.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/** Student profile payloads. */
public class StudentDto {

    public static class UpdateRequest {
        @Size(max = 120) private String college;
        @Size(max = 120) private String department;
        @Min(1) @Max(8) private Integer year;
        @DecimalMin("0.0") @DecimalMax("10.0") private BigDecimal cgpa;
        private List<String> skills;
        @Size(max = 2000) private String resumeUrl;

        public String getCollege() { return college; }
        public void setCollege(String college) { this.college = college; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }
        public BigDecimal getCgpa() { return cgpa; }
        public void setCgpa(BigDecimal cgpa) { this.cgpa = cgpa; }
        public List<String> getSkills() { return skills; }
        public void setSkills(List<String> skills) { this.skills = skills; }
        public String getResumeUrl() { return resumeUrl; }
        public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }
    }

    public static class Response {
        private Long id; private Long userId; private String name; private String email;
        private String college; private String department; private Integer year;
        private BigDecimal cgpa; private List<String> skills; private String resumeUrl;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCollege() { return college; }
        public void setCollege(String college) { this.college = college; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }
        public BigDecimal getCgpa() { return cgpa; }
        public void setCgpa(BigDecimal cgpa) { this.cgpa = cgpa; }
        public List<String> getSkills() { return skills; }
        public void setSkills(List<String> skills) { this.skills = skills; }
        public String getResumeUrl() { return resumeUrl; }
        public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }
    }
}
