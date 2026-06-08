package com.health.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.health.audit.entity.enums.BlacklistReason;
import com.health.audit.entity.enums.BlacklistStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "blacklist_records")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BlacklistRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlacklistReason reason;

    @Column(nullable = false)
    private LocalDate listingDate;

    private String removalConditions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlacklistStatus status;

    private LocalDate expiryDate;
}
