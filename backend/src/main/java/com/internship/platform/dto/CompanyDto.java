package com.internship.platform.dto;

import jakarta.validation.constraints.Size;

/** Company payloads. */
public class CompanyDto {

    public static class UpdateRequest {
        @Size(max = 200) private String name;
        @Size(max = 2000) private String description;
        @Size(max = 200) private String website;
        @Size(max = 200) private String industry;
        @Size(max = 200) private String location;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }
        public String getIndustry() { return industry; }
        public void setIndustry(String industry) { this.industry = industry; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public static class Response {
        private Long id; private Long userId; private String name; private String email;
        private String description; private String website; private String industry;
        private String location; private boolean verified;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }
        public String getIndustry() { return industry; }
        public void setIndustry(String industry) { this.industry = industry; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
    }
}
