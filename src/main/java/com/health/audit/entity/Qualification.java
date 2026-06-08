package com.health.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.health.audit.entity.enums.AnnualInspectionStatus;
import com.health.audit.entity.enums.CertificateType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "qualifications")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Qualification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateType certificateType;

    @Column(nullable = false)
    private String certificateNumber;

    private String issuingAuthority;

    private LocalDate validFrom;

    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    private AnnualInspectionStatus annualInspectionStatus;
}
