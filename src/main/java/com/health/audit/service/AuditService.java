package com.health.audit.service;

import com.health.audit.entity.AuditTrail;
import com.health.audit.entity.enums.OperationType;
import com.health.audit.repository.AuditTrailRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditTrailRepository auditTrailRepository;
    private final ObjectMapper objectMapper;

    public void logOperation(String operator, OperationType operationType, String targetObject, Object beforeChange, Object afterChange) {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setOperator(operator);
        auditTrail.setOperationType(operationType);
        auditTrail.setTargetObject(targetObject);
        auditTrail.setOperationTime(LocalDateTime.now());

        try {
            if (beforeChange != null) {
                auditTrail.setBeforeChange(objectMapper.writeValueAsString(beforeChange));
            }
            if (afterChange != null) {
                auditTrail.setAfterChange(objectMapper.writeValueAsString(afterChange));
            }
        } catch (JsonProcessingException e) {
            auditTrail.setBeforeChange(beforeChange != null ? beforeChange.toString() : null);
            auditTrail.setAfterChange(afterChange != null ? afterChange.toString() : null);
        }

        auditTrailRepository.save(auditTrail);
    }

    public List<AuditTrail> getAuditTrails(String targetObject) {
        return auditTrailRepository.findByTargetObjectOrderByOperationTimeDesc(targetObject);
    }
}
