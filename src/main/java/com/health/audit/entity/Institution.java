package com.health.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.health.audit.entity.enums.InstitutionType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "institutions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String creditCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstitutionType type;

    @Column(columnDefinition = "TEXT")
    private String qualificationCertificates;

    private String legalRepresentative;

    @Column(precision = 15, scale = 2)
    private BigDecimal registeredCapital;

    private boolean frozen = false;
}
