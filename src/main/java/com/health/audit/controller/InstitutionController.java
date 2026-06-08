package com.health.audit.controller;

import com.health.audit.dto.InstitutionDetailDTO;
import com.health.audit.entity.Institution;
import com.health.audit.entity.QualificationAlert;
import com.health.audit.service.InstitutionService;
import com.health.audit.service.QualificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/institutions")
@RequiredArgsConstructor
public class InstitutionController {
    private final InstitutionService institutionService;
    private final QualificationService qualificationService;

    @GetMapping
    public ResponseEntity<List<Institution>> getAll() {
        return ResponseEntity.ok(institutionService.getAllInstitutions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Institution> getById(@PathVariable Long id) {
        return institutionService.getInstitutionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<InstitutionDetailDTO> getDetailById(@PathVariable Long id) {
        Optional<Institution> institutionOpt = institutionService.getInstitutionById(id);
        if (institutionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Institution institution = institutionOpt.get();
        List<QualificationAlert> activeAlerts = qualificationService.getActiveAlertsByInstitution(id);
        return ResponseEntity.ok(InstitutionDetailDTO.from(institution, activeAlerts));
    }

    @PostMapping
    public ResponseEntity<Institution> create(@RequestBody Institution institution,
                                              @RequestHeader(value = "X-Operator", defaultValue = "admin") String operator) {
        return ResponseEntity.ok(institutionService.createInstitution(institution, operator));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Institution> update(@PathVariable Long id,
                                              @RequestBody Institution institution,
                                              @RequestHeader(value = "X-Operator", defaultValue = "admin") String operator) {
        return ResponseEntity.ok(institutionService.updateInstitution(id, institution, operator));
    }

    @PostMapping("/{id}/freeze")
    public ResponseEntity<Void> freeze(@PathVariable Long id,
                                       @RequestHeader(value = "X-Operator", defaultValue = "admin") String operator) {
        institutionService.freezeInstitution(id, operator);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unfreeze")
    public ResponseEntity<Void> unfreeze(@PathVariable Long id,
                                         @RequestHeader(value = "X-Operator", defaultValue = "admin") String operator) {
        institutionService.unfreezeInstitution(id, operator);
        return ResponseEntity.ok().build();
    }
}
