package com.internship.platform.dto;

import com.internship.platform.model.enums.InterviewMode;
import com.internship.platform.model.enums.InterviewStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Interview payloads. */
public class InterviewDto {

    public static class Request {
        @NotNull private Long applicationId;
        @NotNull private Instant scheduledAt;
        private Integer durationMinutes = 30;
        @NotNull private InterviewMode mode;
        @Size(max = 500) private String locationOrLink;

        public Long getApplicationId() { return applicationId; }
        public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
        public Instant getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public InterviewMode getMode() { return mode; }
        public void setMode(InterviewMode mode) { this.mode = mode; }
        public String getLocationOrLink() { return locationOrLink; }
        public void setLocationOrLink(String locationOrLink) { this.locationOrLink = locationOrLink; }
    }

    public static class Response {
        private Long id; private Long applicationId;
        private Instant scheduledAt; private Integer durationMinutes;
        private InterviewMode mode; private InterviewStatus status; private String locationOrLink;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getApplicationId() { return applicationId; }
        public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
        public Instant getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public InterviewMode getMode() { return mode; }
        public void setMode(InterviewMode mode) { this.mode = mode; }
        public InterviewStatus getStatus() { return status; }
        public void setStatus(InterviewStatus status) { this.status = status; }
        public String getLocationOrLink() { return locationOrLink; }
        public void setLocationOrLink(String locationOrLink) { this.locationOrLink = locationOrLink; }
    }
}
