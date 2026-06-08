package com.health.audit.service;

import com.health.audit.entity.EntryApplication;
import com.health.audit.entity.Institution;
import com.health.audit.entity.PersonnelRecord;
import com.health.audit.entity.enums.ApprovalStatus;
import com.health.audit.entity.enums.OperationType;
import com.health.audit.exception.BusinessException;
import com.health.audit.repository.EntryApplicationRepository;
import com.health.audit.repository.InstitutionRepository;
import com.health.audit.repository.PersonnelRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntryApplicationService {
    private final EntryApplicationRepository entryApplicationRepository;
    private final InstitutionRepository institutionRepository;
    private final PersonnelRecordRepository personnelRecordRepository;
    private final BlacklistService blacklistService;
    private final AuditService auditService;

    public List<EntryApplication> getAllApplications() {
        return entryApplicationRepository.findAll();
    }

    public List<EntryApplication> getApplicationsByInstitution(Long institutionId) {
        return entryApplicationRepository.findByInstitutionId(institutionId);
    }

    public List<EntryApplication> getApplicationsByStatus(ApprovalStatus status) {
        return entryApplicationRepository.findByApprovalStatus(status);
    }

    @Transactional
    public EntryApplication createApplication(EntryApplication application, String operator) {
        Institution institution = institutionRepository.findById(application.getInstitution().getId())
                .orElseThrow(() -> new BusinessException("机构不存在", "INSTITUTION_NOT_FOUND"));

        if (institution.isFrozen()) {
            throw new BusinessException("机构已被冻结，无法提交申请", "INSTITUTION_FROZEN");
        }

        if (blacklistService.isBlacklisted(institution.getId())) {
            throw new BusinessException("机构在黑名单中，三年内禁止申请", "INSTITUTION_BLACKLISTED");
        }

        validatePersonnel(application.getAssignedPersonnel(), institution.getId());

        application.setInstitution(institution);
        application.setApprovalStatus(ApprovalStatus.PENDING_SCHOOL_REVIEW);

        EntryApplication saved = entryApplicationRepository.save(application);
        auditService.logOperation(operator, OperationType.CREATE, "EntryApplication:" + saved.getId(), null, saved);
        return saved;
    }

    private void validatePersonnel(List<Long> personnelIds, Long institutionId) {
        if (personnelIds == null || personnelIds.isEmpty()) {
            return;
        }

        List<PersonnelRecord> allInstitutionPersonnel = personnelRecordRepository.findByInstitutionId(institutionId);
        Set<Long> allPersonnelIds = allInstitutionPersonnel.stream()
                .map(PersonnelRecord::getId)
                .collect(Collectors.toSet());
        Set<Long> allowedPersonnelIds = allInstitutionPersonnel.stream()
                .filter(PersonnelRecord::isAllowedEntry)
                .map(PersonnelRecord::getId)
                .collect(Collectors.toSet());

        List<String> errors = new ArrayList<>();

        for (Long personnelId : personnelIds) {
            if (!allPersonnelIds.contains(personnelId)) {
                errors.add("人员ID " + personnelId + " 未在本机构备案");
            } else if (!allowedPersonnelIds.contains(personnelId)) {
                Optional<PersonnelRecord> personnel = allInstitutionPersonnel.stream()
                        .filter(p -> p.getId().equals(personnelId))
                        .findFirst();
                String name = personnel.map(PersonnelRecord::getName).orElse("未知");
                errors.add("人员 \"" + name + "\" (ID: " + personnelId + ") 不允许入校");
            }
        }

        if (!errors.isEmpty()) {
            throw new BusinessException(String.join("; ", errors), "PERSONNEL_VALIDATION_FAILED");
        }
    }

    @Transactional
    public EntryApplication approveBySchool(Long applicationId, String operator) {
        EntryApplication application = entryApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申请不存在", "APPLICATION_NOT_FOUND"));

        if (application.getApprovalStatus() != ApprovalStatus.PENDING_SCHOOL_REVIEW) {
            throw new BusinessException("当前状态不允许学校审批", "INVALID_STATUS_FOR_APPROVAL");
        }

        EntryApplication before = copyApplication(application);
        application.setApprovalStatus(ApprovalStatus.PENDING_EDUCATION_REVIEW);
        EntryApplication saved = entryApplicationRepository.save(application);
        auditService.logOperation(operator, OperationType.APPROVE, "EntryApplication:" + applicationId, before, saved);
        return saved;
    }

    @Transactional
    public EntryApplication approveByEducation(Long applicationId, String operator) {
        EntryApplication application = entryApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申请不存在", "APPLICATION_NOT_FOUND"));

        if (application.getApprovalStatus() != ApprovalStatus.PENDING_EDUCATION_REVIEW) {
            throw new BusinessException("当前状态不允许教育局审批", "INVALID_STATUS_FOR_APPROVAL");
        }

        EntryApplication before = copyApplication(application);
        application.setApprovalStatus(ApprovalStatus.PENDING_HEALTH_REVIEW);
        EntryApplication saved = entryApplicationRepository.save(application);
        auditService.logOperation(operator, OperationType.APPROVE, "EntryApplication:" + applicationId, before, saved);
        return saved;
    }

    @Transactional
    public EntryApplication approveByHealth(Long applicationId, String operator) {
        EntryApplication application = entryApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申请不存在", "APPLICATION_NOT_FOUND"));

        if (application.getApprovalStatus() != ApprovalStatus.PENDING_HEALTH_REVIEW) {
            throw new BusinessException("当前状态不允许卫健审批", "INVALID_STATUS_FOR_APPROVAL");
        }

        EntryApplication before = copyApplication(application);
        application.setApprovalStatus(ApprovalStatus.APPROVED);
        EntryApplication saved = entryApplicationRepository.save(application);
        auditService.logOperation(operator, OperationType.APPROVE, "EntryApplication:" + applicationId, before, saved);
        return saved;
    }

    @Transactional
    public EntryApplication rejectApplication(Long applicationId, String reason, String operator) {
        EntryApplication application = entryApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申请不存在", "APPLICATION_NOT_FOUND"));

        EntryApplication before = copyApplication(application);
        application.setApprovalStatus(ApprovalStatus.REJECTED);
        application.setRejectionReason(reason);
        EntryApplication saved = entryApplicationRepository.save(application);
        auditService.logOperation(operator, OperationType.REJECT, "EntryApplication:" + applicationId, before, saved);
        return saved;
    }

    private EntryApplication copyApplication(EntryApplication app) {
        EntryApplication copy = new EntryApplication();
        copy.setId(app.getId());
        copy.setSchoolName(app.getSchoolName());
        copy.setServiceProject(app.getServiceProject());
        copy.setAssignedPersonnel(app.getAssignedPersonnel() != null ? new ArrayList<>(app.getAssignedPersonnel()) : null);
        copy.setApprovalStatus(app.getApprovalStatus());
        copy.setRejectionReason(app.getRejectionReason());
        return copy;
    }
}
