package com.health.audit.repository;

import com.health.audit.entity.QualificationAlert;
import com.health.audit.entity.enums.AlertLevel;
import com.health.audit.entity.enums.DisposalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface QualificationAlertRepository extends JpaRepository<QualificationAlert, Long> {
    List<QualificationAlert> findByInstitutionId(Long institutionId);

    List<QualificationAlert> findByInstitutionIdAndDisposalResult(Long institutionId, DisposalResult disposalResult);

    Optional<QualificationAlert> findByQualificationIdAndAlertLevelAndDisposalResult(Long qualificationId, AlertLevel alertLevel, DisposalResult disposalResult);

    @Query("SELECT qa FROM QualificationAlert qa WHERE qa.institution.id = :institutionId AND qa.disposalResult != 'RESOLVED' ORDER BY qa.alertTime DESC")
    List<QualificationAlert> findActiveAlertsByInstitutionId(Long institutionId);

    boolean existsByQualificationIdAndAlertLevelAndDisposalResult(Long qualificationId, AlertLevel alertLevel, DisposalResult disposalResult);

    @Query("SELECT qa FROM QualificationAlert qa WHERE qa.alertTime >= :date ORDER BY qa.alertTime DESC")
    List<QualificationAlert> findAlertsGeneratedAfter(LocalDate date);
}
