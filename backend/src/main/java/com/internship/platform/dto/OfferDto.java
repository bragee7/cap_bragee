package com.internship.platform.dto;

import com.internship.platform.model.enums.OfferStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

/** Offer payloads. */
public class OfferDto {

    public static class Request {
        @NotNull private Long applicationId;
        private BigDecimal stipend;
        private Instant joiningDate;
        private String details;

        public Long getApplicationId() { return applicationId; }
        public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
        public BigDecimal getStipend() { return stipend; }
        public void setStipend(BigDecimal stipend) { this.stipend = stipend; }
        public Instant getJoiningDate() { return joiningDate; }
        public void setJoiningDate(Instant joiningDate) { this.joiningDate = joiningDate; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
    }

    public static class Response {
        private Long id; private Long applicationId;
        private BigDecimal stipend; private Instant joiningDate;
        private OfferStatus status; private String details;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getApplicationId() { return applicationId; }
        public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
        public BigDecimal getStipend() { return stipend; }
        public void setStipend(BigDecimal stipend) { this.stipend = stipend; }
        public Instant getJoiningDate() { return joiningDate; }
        public void setJoiningDate(Instant joiningDate) { this.joiningDate = joiningDate; }
        public OfferStatus getStatus() { return status; }
        public void setStatus(OfferStatus status) { this.status = status; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
    }
}
