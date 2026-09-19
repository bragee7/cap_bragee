package com.internship.platform.dto;

import com.internship.platform.model.enums.OfferStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

/** Offer payloads. */
public class OfferDto {

    public static class Request {
        @NotNull private Long applicationId;
        private BigDecimal salary;
        private java.time.LocalDate joiningDate;

        public Long getApplicationId() { return applicationId; }
        public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
        public BigDecimal getSalary() { return salary; }
        public void setSalary(BigDecimal salary) { this.salary = salary; }
        public java.time.LocalDate getJoiningDate() { return joiningDate; }
        public void setJoiningDate(java.time.LocalDate joiningDate) { this.joiningDate = joiningDate; }
    }

    public static class Response {
        private Long id; private Long applicationId;
        private BigDecimal salary; private java.time.LocalDate joiningDate;
        private OfferStatus status;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getApplicationId() { return applicationId; }
        public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
        public BigDecimal getSalary() { return salary; }
        public void setSalary(BigDecimal salary) { this.salary = salary; }
        public java.time.LocalDate getJoiningDate() { return joiningDate; }
        public void setJoiningDate(java.time.LocalDate joiningDate) { this.joiningDate = joiningDate; }
        public OfferStatus getStatus() { return status; }
        public void setStatus(OfferStatus status) { this.status = status; }
    }
}
