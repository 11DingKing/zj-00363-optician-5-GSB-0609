package com.health.audit.controller;

import com.health.audit.entity.QualificationAlert;
import com.health.audit.entity.enums.DisposalResult;
import com.health.audit.repository.QualificationAlertRepository;
import com.health.audit.service.QualificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/qualification-alerts")
@RequiredArgsConstructor
public class QualificationAlertController {
    private final QualificationAlertRepository qualificationAlertRepository;
    private final QualificationService qualificationService;

    @GetMapping
    public ResponseEntity<List<QualificationAlert>> getAll() {
        return ResponseEntity.ok(qualificationAlertRepository.findAll());
    }

    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<List<QualificationAlert>> getByInstitutionId(@PathVariable Long institutionId) {
        return ResponseEntity.ok(qualificationAlertRepository.findByInstitutionId(institutionId));
    }

    @GetMapping("/institution/{institutionId}/active")
    public ResponseEntity<List<QualificationAlert>> getActiveByInstitutionId(@PathVariable Long institutionId) {
        return ResponseEntity.ok(qualificationService.getActiveAlertsByInstitution(institutionId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QualificationAlert> getById(@PathVariable Long id) {
        return qualificationAlertRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<QualificationAlert> resolveAlert(@PathVariable Long id,
                                                           @RequestHeader(value = "X-Operator", defaultValue = "admin") String operator) {
        Optional<QualificationAlert> alertOpt = qualificationAlertRepository.findById(id);
        if (alertOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        QualificationAlert alert = alertOpt.get();
        alert.setDisposalResult(DisposalResult.RESOLVED);
        alert.setRemark("预警已由 " + operator + " 手动处理");
        return ResponseEntity.ok(qualificationAlertRepository.save(alert));
    }

    @PostMapping("/trigger-scan")
    public ResponseEntity<String> triggerScan() {
        qualificationService.checkQualificationsExpiry();
        return ResponseEntity.ok("资质扫描已触发");
    }
}
