package com.health.audit.controller;

import com.health.audit.entity.EntryApplication;
import com.health.audit.entity.enums.ApprovalStatus;
import com.health.audit.service.EntryApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class EntryApplicationController {
    private final EntryApplicationService entryApplicationService;

    @GetMapping
    public ResponseEntity<List<EntryApplication>> getAll() {
        return ResponseEntity.ok(entryApplicationService.getAllApplications());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EntryApplication>> getByStatus(@PathVariable ApprovalStatus status) {
        return ResponseEntity.ok(entryApplicationService.getApplicationsByStatus(status));
    }

    @PostMapping
    public ResponseEntity<EntryApplication> create(@RequestBody EntryApplication application,
                                                   @RequestHeader(value = "X-Operator", defaultValue = "admin") String operator) {
        return ResponseEntity.ok(entryApplicationService.createApplication(application, operator));
    }

    @PostMapping("/{id}/approve/school")
    public ResponseEntity<EntryApplication> approveBySchool(@PathVariable Long id,
                                                            @RequestHeader(value = "X-Operator", defaultValue = "school_admin") String operator) {
        return ResponseEntity.ok(entryApplicationService.approveBySchool(id, operator));
    }

    @PostMapping("/{id}/approve/education")
    public ResponseEntity<EntryApplication> approveByEducation(@PathVariable Long id,
                                                               @RequestHeader(value = "X-Operator", defaultValue = "education_admin") String operator) {
        return ResponseEntity.ok(entryApplicationService.approveByEducation(id, operator));
    }

    @PostMapping("/{id}/approve/health")
    public ResponseEntity<EntryApplication> approveByHealth(@PathVariable Long id,
                                                            @RequestHeader(value = "X-Operator", defaultValue = "health_admin") String operator) {
        return ResponseEntity.ok(entryApplicationService.approveByHealth(id, operator));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<EntryApplication> reject(@PathVariable Long id,
                                                   @RequestParam String reason,
                                                   @RequestHeader(value = "X-Operator", defaultValue = "admin") String operator) {
        return ResponseEntity.ok(entryApplicationService.rejectApplication(id, reason, operator));
    }
}
