package com.health.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.health.audit.entity.enums.AlertLevel;
import com.health.audit.entity.enums.DisposalResult;
import com.health.audit.entity.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "qualification_alerts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class QualificationAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qualification_id", nullable = false)
    private Qualification qualification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertLevel alertLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus notificationStatus = NotificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisposalResult disposalResult = DisposalResult.PENDING;

    @Column(nullable = false)
    private LocalDate expiryDate;

    private Integer daysUntilExpiry;

    @Column(nullable = false)
    private LocalDateTime alertTime;

    private LocalDateTime notificationTime;

    @Column(columnDefinition = "TEXT")
    private String remark;
}
