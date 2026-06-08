package com.health.audit.entity;

import com.health.audit.entity.enums.OperationType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_trails")
public class AuditTrail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String operator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationType operationType;

    @Column(nullable = false)
    private String targetObject;

    @Column(columnDefinition = "TEXT")
    private String beforeChange;

    @Column(columnDefinition = "TEXT")
    private String afterChange;

    @Column(nullable = false)
    private LocalDateTime operationTime;
}
