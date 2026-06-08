package com.health.audit.repository;

import com.health.audit.entity.Qualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface QualificationRepository extends JpaRepository<Qualification, Long> {
    List<Qualification> findByInstitutionId(Long institutionId);

    @Query("SELECT q FROM Qualification q WHERE q.validTo < :date AND q.institution.frozen = false")
    List<Qualification> findExpiredQualifications(LocalDate date);

    @Query("SELECT q FROM Qualification q WHERE q.validTo BETWEEN :startDate AND :endDate AND q.institution.frozen = false")
    List<Qualification> findQualificationsExpiringBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT q FROM Qualification q WHERE q.validTo <= :warningDate AND q.institution.frozen = false")
    List<Qualification> findQualificationsNeedingWarning(LocalDate warningDate);
}
