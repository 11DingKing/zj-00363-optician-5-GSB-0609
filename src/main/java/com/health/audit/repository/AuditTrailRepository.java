package com.health.audit.repository;

import com.health.audit.entity.AuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
    List<AuditTrail> findByTargetObjectOrderByOperationTimeDesc(String targetObject);
}
