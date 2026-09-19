package com.internship.platform.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Student profile linked 1-1 to a User with role STUDENT.
 */
@Entity
@Table(name = "students", indexes = {
        @Index(name = "idx_students_user", columnList = "user_id", unique = true)
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank
    @Size(max = 180)
    @Column(nullable = false, length = 180)
    private String collegeName;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String department;

    @NotNull
    @Min(2000)
    @Max(2100)
    @Column(nullable = false)
    private Integer graduationYear;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal cgpa;

    @Size(max = 20)
    @Column(length = 20)
    private String phone;

    /** Resume URL / storage reference (cloud storage can replace later). */
    @Size(max = 500)
    @Column(length = 500)
    private String resumeUrl;

    @Size(max = 2000)
    @Column(length = 2000)
    private String bio;

    /** Comma-separated skills, e.g. "Java,Spring Boot,SQL". */
    @Size(max = 1000)
    @Column(length = 1000)
    private String skills;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }
    public BigDecimal getCgpa() { return cgpa; }
    public void setCgpa(BigDecimal cgpa) { this.cgpa = cgpa; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
