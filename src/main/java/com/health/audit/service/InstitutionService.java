package com.health.audit.service;

import com.health.audit.entity.Institution;
import com.health.audit.entity.enums.OperationType;
import com.health.audit.exception.BusinessException;
import com.health.audit.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InstitutionService {
    private final InstitutionRepository institutionRepository;
    private final AuditService auditService;

    public List<Institution> getAllInstitutions() {
        return institutionRepository.findAll();
    }

    public Optional<Institution> getInstitutionById(Long id) {
        return institutionRepository.findById(id);
    }

    @Transactional
    public Institution createInstitution(Institution institution, String operator) {
        Institution saved = institutionRepository.save(institution);
        auditService.logOperation(operator, OperationType.CREATE, "Institution:" + saved.getId(), null, saved);
        return saved;
    }

    @Transactional
    public Institution updateInstitution(Long id, Institution institution, String operator) {
        Institution existing = institutionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("机构不存在", "INSTITUTION_NOT_FOUND"));
        Institution before = copyInstitution(existing);

        existing.setName(institution.getName());
        existing.setLegalRepresentative(institution.getLegalRepresentative());
        existing.setRegisteredCapital(institution.getRegisteredCapital());
        existing.setQualificationCertificates(institution.getQualificationCertificates());

        Institution saved = institutionRepository.save(existing);
        auditService.logOperation(operator, OperationType.UPDATE, "Institution:" + id, before, saved);
        return saved;
    }

    @Transactional
    public void freezeInstitution(Long id, String operator) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("机构不存在", "INSTITUTION_NOT_FOUND"));
        Institution before = copyInstitution(institution);

        institution.setFrozen(true);
        Institution saved = institutionRepository.save(institution);
        auditService.logOperation(operator, OperationType.FREEZE, "Institution:" + id, before, saved);
    }

    @Transactional
    public void unfreezeInstitution(Long id, String operator) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("机构不存在", "INSTITUTION_NOT_FOUND"));
        Institution before = copyInstitution(institution);

        institution.setFrozen(false);
        Institution saved = institutionRepository.save(institution);
        auditService.logOperation(operator, OperationType.UNFREEZE, "Institution:" + id, before, saved);
    }

    private Institution copyInstitution(Institution inst) {
        Institution copy = new Institution();
        copy.setId(inst.getId());
        copy.setName(inst.getName());
        copy.setCreditCode(inst.getCreditCode());
        copy.setType(inst.getType());
        copy.setQualificationCertificates(inst.getQualificationCertificates());
        copy.setLegalRepresentative(inst.getLegalRepresentative());
        copy.setRegisteredCapital(inst.getRegisteredCapital());
        copy.setFrozen(inst.isFrozen());
        return copy;
    }
}
