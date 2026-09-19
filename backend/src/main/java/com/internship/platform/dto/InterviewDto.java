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
        @NotNull private InterviewMode mode;
        @Size(max = 500) private String meetingLink;
        @Size(max = 120) private String interviewerName;

        public Long getApplicationId() { return applicationId; }
        public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
        public Instant getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }
        public String getMeetingLink() { return meetingLink; }
        public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }
        public String getInterviewerName() { return interviewerName; }
        public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }
    }

    public static class Response {
        private Long id; private Long applicationId;
        private Instant scheduledAt;
        private InterviewMode mode; private InterviewStatus status;
        private String meetingLink; private String interviewerName;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getApplicationId() { return applicationId; }
        public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
        public Instant getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }
        public String getMeetingLink() { return meetingLink; }
        public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }
        public String getInterviewerName() { return interviewerName; }
        public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }
    }
}
