package com.health.audit.repository;

import com.health.audit.entity.BlacklistRecord;
import com.health.audit.entity.enums.BlacklistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlacklistRecordRepository extends JpaRepository<BlacklistRecord, Long> {
    List<BlacklistRecord> findByInstitutionId(Long institutionId);

    Optional<BlacklistRecord> findByInstitutionIdAndStatus(Long institutionId, BlacklistStatus status);

    List<BlacklistRecord> findByStatus(BlacklistStatus status);
}
