package com.health.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.health.audit.entity.enums.ApprovalStatus;
import com.health.audit.entity.enums.ServiceProject;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "entry_applications")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EntryApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false)
    private String schoolName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceProject serviceProject;

    @ElementCollection
    @CollectionTable(name = "application_personnel", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "personnel_id")
    private List<Long> assignedPersonnel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus approvalStatus;

    private String rejectionReason;
}
