package com.health.audit.service;

import com.health.audit.entity.BlacklistRecord;
import com.health.audit.entity.Institution;
import com.health.audit.entity.enums.BlacklistStatus;
import com.health.audit.entity.enums.OperationType;
import com.health.audit.repository.BlacklistRecordRepository;
import com.health.audit.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlacklistService {
    private final BlacklistRecordRepository blacklistRecordRepository;
    private final InstitutionRepository institutionRepository;
    private final AuditService auditService;

    public List<BlacklistRecord> getActiveBlacklistRecords() {
        return blacklistRecordRepository.findByStatus(BlacklistStatus.ACTIVE);
    }

    public Optional<BlacklistRecord> getActiveBlacklistByInstitution(Long institutionId) {
        return blacklistRecordRepository.findByInstitutionIdAndStatus(institutionId, BlacklistStatus.ACTIVE);
    }

    @Transactional
    public BlacklistRecord addToBlacklist(Long institutionId, BlacklistRecord record, String operator) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new RuntimeException("机构不存在"));
        
        record.setInstitution(institution);
        record.setListingDate(LocalDate.now());
        record.setStatus(BlacklistStatus.ACTIVE);
        record.setExpiryDate(LocalDate.now().plusYears(3));

        BlacklistRecord saved = blacklistRecordRepository.save(record);
        auditService.logOperation(operator, OperationType.CREATE, "BlacklistRecord:" + saved.getId(), null, saved);
        return saved;
    }

    public boolean isBlacklisted(Long institutionId) {
        return blacklistRecordRepository.findByInstitutionIdAndStatus(institutionId, BlacklistStatus.ACTIVE)
                .isPresent();
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void checkExpiredBlacklist() {
        LocalDate today = LocalDate.now();
        blacklistRecordRepository.findByStatus(BlacklistStatus.ACTIVE).stream()
                .filter(record -> record.getExpiryDate() != null && record.getExpiryDate().isBefore(today))
                .forEach(record -> {
                    record.setStatus(BlacklistStatus.EXPIRED);
                    blacklistRecordRepository.save(record);
                    auditService.logOperation("System", OperationType.UPDATE, "BlacklistRecord:" + record.getId(), null, record);
                });
    }

    @Transactional
    public void removeFromBlacklist(Long recordId, String operator) {
        BlacklistRecord record = blacklistRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("黑名单记录不存在"));
        BlacklistRecord before = new BlacklistRecord();
        before.setId(record.getId());
        before.setStatus(record.getStatus());

        record.setStatus(BlacklistStatus.REMOVED);
        BlacklistRecord saved = blacklistRecordRepository.save(record);
        auditService.logOperation(operator, OperationType.UPDATE, "BlacklistRecord:" + recordId, before, saved);
    }
}
