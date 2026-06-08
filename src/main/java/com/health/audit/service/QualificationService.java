package com.health.audit.service;

import com.health.audit.entity.EntryApplication;
import com.health.audit.entity.Institution;
import com.health.audit.entity.Qualification;
import com.health.audit.entity.QualificationAlert;
import com.health.audit.entity.enums.AlertLevel;
import com.health.audit.entity.enums.ApprovalStatus;
import com.health.audit.entity.enums.DisposalResult;
import com.health.audit.entity.enums.OperationType;
import com.health.audit.repository.EntryApplicationRepository;
import com.health.audit.repository.InstitutionRepository;
import com.health.audit.repository.QualificationAlertRepository;
import com.health.audit.repository.QualificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QualificationService {
    private final QualificationRepository qualificationRepository;
    private final InstitutionRepository institutionRepository;
    private final EntryApplicationRepository entryApplicationRepository;
    private final QualificationAlertRepository qualificationAlertRepository;
    private final AuditService auditService;

    public List<Qualification> getQualificationsByInstitution(Long institutionId) {
        return qualificationRepository.findByInstitutionId(institutionId);
    }

    @Transactional
    public Qualification addQualification(Qualification qualification, String operator) {
        Qualification saved = qualificationRepository.save(qualification);
        auditService.logOperation(operator, OperationType.CREATE, "Qualification:" + saved.getId(), null, saved);
        return saved;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void checkQualificationsExpiry() {
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(30);

        List<Qualification> qualificationsNeedingWarning = qualificationRepository.findQualificationsNeedingWarning(warningDate);

        for (Qualification qualification : qualificationsNeedingWarning) {
            processQualificationExpiry(qualification, today);
        }
    }

    private void processQualificationExpiry(Qualification qualification, LocalDate today) {
        LocalDate validTo = qualification.getValidTo();
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, validTo);
        Institution institution = qualification.getInstitution();

        if (daysUntilExpiry < 0) {
            if (!qualificationAlertRepository.existsByQualificationIdAndAlertLevelAndDisposalResult(
                    qualification.getId(), AlertLevel.RED_EXPIRED, DisposalResult.PENDING)) {
                createAlert(qualification, institution, AlertLevel.RED_EXPIRED, daysUntilExpiry, validTo);
                freezeInstitutionAndApplications(institution.getId(), "资质证书已过期");
            }
        } else if (daysUntilExpiry <= 30) {
            if (!qualificationAlertRepository.existsByQualificationIdAndAlertLevelAndDisposalResult(
                    qualification.getId(), AlertLevel.YELLOW_30_DAYS, DisposalResult.PENDING)) {
                createAlert(qualification, institution, AlertLevel.YELLOW_30_DAYS, daysUntilExpiry, validTo);
            }
        }
    }

    private void createAlert(Qualification qualification, Institution institution, AlertLevel alertLevel,
                             long daysUntilExpiry, LocalDate expiryDate) {
        QualificationAlert alert = new QualificationAlert();
        alert.setQualification(qualification);
        alert.setInstitution(institution);
        alert.setAlertLevel(alertLevel);
        alert.setExpiryDate(expiryDate);
        alert.setDaysUntilExpiry((int) daysUntilExpiry);
        alert.setAlertTime(LocalDateTime.now());
        alert.setRemark(alertLevel == AlertLevel.RED_EXPIRED ? "资质已过期，系统自动冻结相关业务" : "资质将在30天内到期，请及时更新");
        qualificationAlertRepository.save(alert);
    }

    @Transactional
    public void freezeInstitutionAndApplications(Long institutionId, String reason) {
        institutionRepository.findById(institutionId).ifPresent(institution -> {
            Institution before = new Institution();
            before.setId(institution.getId());
            before.setFrozen(institution.isFrozen());

            if (!institution.isFrozen()) {
                institution.setFrozen(true);
                institutionRepository.save(institution);
                auditService.logOperation("System", OperationType.FREEZE, "Institution:" + institutionId, before, institution);
            }

            List<EntryApplication> applications = entryApplicationRepository.findActiveApplicationsByInstitutionId(institutionId);
            for (EntryApplication application : applications) {
                EntryApplication appBefore = new EntryApplication();
                appBefore.setId(application.getId());
                appBefore.setApprovalStatus(application.getApprovalStatus());

                application.setApprovalStatus(ApprovalStatus.FROZEN);
                application.setRejectionReason(reason);
                entryApplicationRepository.save(application);
                auditService.logOperation("System", OperationType.FREEZE, "EntryApplication:" + application.getId(), appBefore, application);
            }

            List<QualificationAlert> alerts = qualificationAlertRepository
                    .findByInstitutionIdAndDisposalResult(institutionId, DisposalResult.PENDING);
            for (QualificationAlert alert : alerts) {
                if (alert.getAlertLevel() == AlertLevel.RED_EXPIRED) {
                    alert.setDisposalResult(DisposalResult.AUTO_FROZEN);
                    qualificationAlertRepository.save(alert);
                }
            }
        });
    }

    public List<QualificationAlert> getActiveAlertsByInstitution(Long institutionId) {
        return qualificationAlertRepository.findActiveAlertsByInstitutionId(institutionId);
    }
}
