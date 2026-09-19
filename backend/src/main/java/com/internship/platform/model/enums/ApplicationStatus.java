package com.internship.platform.model.enums;

/**
 * Controlled application workflow states.
 * APPLIED -&gt; UNDER_REVIEW -&gt; SHORTLISTED -&gt; INTERVIEW -&gt; SELECTED,
 * UNDER_REVIEW/SHORTLISTED/INTERVIEW -&gt; REJECTED, APPLIED -&gt; WITHDRAWN.
 */
public enum ApplicationStatus {
    APPLIED,
    UNDER_REVIEW,
    SHORTLISTED,
    INTERVIEW,
    SELECTED,
    OFFERED,
    ACCEPTED,
    REJECTED,
    WITHDRAWN
}
