package com.health.audit.repository;

import com.health.audit.entity.PersonnelRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonnelRecordRepository extends JpaRepository<PersonnelRecord, Long> {
    List<PersonnelRecord> findByInstitutionId(Long institutionId);

    List<PersonnelRecord> findByInstitutionIdAndAllowedEntryTrue(Long institutionId);
}
