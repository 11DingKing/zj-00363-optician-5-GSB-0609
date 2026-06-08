package com.health.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.health.audit.entity.enums.PositionType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "personnel_records")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PersonnelRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String idCardNumber;

    private String practiceLicenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PositionType positionType;

    private boolean allowedEntry = true;
}
