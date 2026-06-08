package com.health.audit.repository;

import com.health.audit.entity.EntryApplication;
import com.health.audit.entity.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface EntryApplicationRepository extends JpaRepository<EntryApplication, Long> {
    List<EntryApplication> findByInstitutionId(Long institutionId);

    List<EntryApplication> findByApprovalStatus(ApprovalStatus status);

    List<EntryApplication> findByInstitutionIdAndApprovalStatusNot(Long institutionId, ApprovalStatus status);

    @Query("SELECT e FROM EntryApplication e WHERE e.institution.id = :institutionId AND e.approvalStatus IN " +
           "(com.health.audit.entity.enums.ApprovalStatus.PENDING_SCHOOL_REVIEW, " +
           "com.health.audit.entity.enums.ApprovalStatus.PENDING_EDUCATION_REVIEW, " +
           "com.health.audit.entity.enums.ApprovalStatus.PENDING_HEALTH_REVIEW, " +
           "com.health.audit.entity.enums.ApprovalStatus.APPROVED)")
    List<EntryApplication> findActiveApplicationsByInstitutionId(Long institutionId);
}
