package com.internship.platform.util;

import com.internship.platform.model.entity.JobPosting;
import com.internship.platform.model.entity.Student;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Deterministic skill/CGPA matcher (rule-based placeholder for an ML model).
 * Returns 0..1 so it can be calibrated/replaced later without API changes.
 */
@Component
public class MatchingService {

    /**
     * Score a student against a job.
     *
     * @param student candidate profile (skills/CGPA may be null)
     * @param job target posting
     * @return 0.0..1.0 match score
     */
    public double score(Student student, JobPosting job) {
        if (student == null || job == null) {
            return 0.0;
        }
        List<String> have = student.getSkills() == null ? List.of() : student.getSkills();
        List<String> need = job.getRequiredSkills() == null ? List.of() : job.getRequiredSkills();
        Set<String> norm = new HashSet<>();
        for (String s : have) {
            if (s != null) {
                norm.add(s.trim().toLowerCase());
            }
        }
        double skillPart;
        if (need.isEmpty()) {
            skillPart = 0.6;
        } else {
            long hits = need.stream()
                    .filter(Objects::nonNull)
                    .map(s -> s.trim().toLowerCase())
                    .filter(norm::contains)
                    .count();
            skillPart = (double) hits / need.size();
        }
        double cgpaPart = 0.5;
        if (job.getMinimumCgpa() != null && student.getCgpa() != null) {
            cgpaPart = student.getCgpa().compareTo(job.getMinimumCgpa()) >= 0 ? 1.0 : 0.2;
        }
        return Math.round((0.7 * skillPart + 0.3 * cgpaPart) * 100.0) / 100.0;
    }
}
