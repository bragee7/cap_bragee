package com.internship.platform.controller;

import com.internship.platform.dto.*;
import com.internship.platform.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Student self-service profile. */
@RestController
@RequestMapping("/api/students")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final StudentService students;

    public StudentController(StudentService students) {
        this.students = students;
    }

    /** Get my profile. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StudentDto.Response>> me() {
        return ResponseEntity.ok(ApiResponse.ok(students.me(), "Profile"));
    }

    /** Update my profile. */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<StudentDto.Response>> update(
            @Valid @RequestBody StudentDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(students.update(req), "Updated"));
    }
}
