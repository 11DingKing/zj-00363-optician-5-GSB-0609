package com.health.audit.service;

import com.health.audit.entity.PersonnelRecord;
import com.health.audit.entity.enums.OperationType;
import com.health.audit.exception.BusinessException;
import com.health.audit.repository.PersonnelRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonnelService {
    private final PersonnelRecordRepository personnelRecordRepository;
    private final AuditService auditService;

    public List<PersonnelRecord> getPersonnelByInstitution(Long institutionId) {
        return personnelRecordRepository.findByInstitutionId(institutionId);
    }

    public List<PersonnelRecord> getAllowedPersonnelByInstitution(Long institutionId) {
        return personnelRecordRepository.findByInstitutionIdAndAllowedEntryTrue(institutionId);
    }

    @Transactional
    public PersonnelRecord addPersonnel(PersonnelRecord personnel, String operator) {
        personnel.setIdCardNumber(maskIdCard(personnel.getIdCardNumber()));
        PersonnelRecord saved = personnelRecordRepository.save(personnel);
        auditService.logOperation(operator, OperationType.CREATE, "PersonnelRecord:" + saved.getId(), null, saved);
        return saved;
    }

    @Transactional
    public void setAllowedEntry(Long personnelId, boolean allowed, String operator) {
        PersonnelRecord personnel = personnelRecordRepository.findById(personnelId)
                .orElseThrow(() -> new BusinessException("人员不存在", "PERSONNEL_NOT_FOUND"));
        PersonnelRecord before = copyPersonnelRecord(personnel);

        personnel.setAllowedEntry(allowed);
        PersonnelRecord saved = personnelRecordRepository.save(personnel);
        auditService.logOperation(operator, OperationType.UPDATE, "PersonnelRecord:" + personnelId, before, saved);
    }

    private PersonnelRecord copyPersonnelRecord(PersonnelRecord record) {
        PersonnelRecord copy = new PersonnelRecord();
        copy.setId(record.getId());
        copy.setName(record.getName());
        copy.setIdCardNumber(record.getIdCardNumber());
        copy.setPracticeLicenseNumber(record.getPracticeLicenseNumber());
        copy.setPositionType(record.getPositionType());
        copy.setAllowedEntry(record.isAllowedEntry());
        return copy;
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }
}
