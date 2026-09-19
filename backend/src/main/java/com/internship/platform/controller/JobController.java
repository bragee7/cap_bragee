package com.internship.platform.controller;

import com.internship.platform.dto.*;
import com.internship.platform.model.enums.JobType;
import com.internship.platform.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Job browsing (public) + posting (company). */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobs;

    public JobController(JobService jobs) {
        this.jobs = jobs;
    }

    /** Search open jobs with optional filters. */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobDto.Response>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobs.search(keyword, jobType, location, page, size), "Jobs"));
    }

    /** Get one job. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobDto.Response>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(jobs.get(id), "Job"));
    }

    /** Post a job (company). */
    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<JobDto.Response>> create(
            @Valid @RequestBody JobDto.Request req) {
        return ResponseEntity.ok(ApiResponse.ok(jobs.create(req), "Created"));
    }

    /** My company's jobs. */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<?>> mine() {
        return ResponseEntity.ok(ApiResponse.ok(jobs.myJobs(), "My jobs"));
    }

    /** Update my job. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<JobDto.Response>> update(
            @PathVariable Long id, @RequestBody JobDto.Request req) {
        return ResponseEntity.ok(ApiResponse.ok(jobs.update(id, req), "Updated"));
    }

    /** Close my job. */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('COMPANY','ADMIN')")
    public ResponseEntity<ApiResponse<JobDto.Response>> close(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(jobs.close(id), "Closed"));
    }
}
