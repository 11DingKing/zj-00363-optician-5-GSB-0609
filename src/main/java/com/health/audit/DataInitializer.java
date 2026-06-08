package com.health.audit;

import com.health.audit.entity.*;
import com.health.audit.entity.enums.*;
import com.health.audit.repository.*;
import com.health.audit.service.BlacklistService;
import com.health.audit.service.QualificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final InstitutionRepository institutionRepository;
    private final QualificationRepository qualificationRepository;
    private final PersonnelRecordRepository personnelRecordRepository;
    private final EntryApplicationRepository entryApplicationRepository;
    private final BlacklistRecordRepository blacklistRecordRepository;
    private final QualificationAlertRepository qualificationAlertRepository;
    private final BlacklistService blacklistService;
    private final QualificationService qualificationService;

    private final Random random = new Random();

    @Override
    public void run(String... args) {
        List<Institution> institutions = createInstitutions();
        List<Qualification> qualifications = createQualifications(institutions);
        List<PersonnelRecord> personnelRecords = createPersonnelRecords(institutions);
        List<EntryApplication> applications = createEntryApplications(institutions, personnelRecords);
        createBlacklistRecords(institutions);
        createTestExpiringQualifications(institutions);

        qualificationService.checkQualificationsExpiry();

        System.out.println("=== 数据初始化完成 ===");
        System.out.println("机构数量: " + institutions.size());
        System.out.println("资质数量: " + qualificationRepository.count());
        System.out.println("人员数量: " + personnelRecords.size());
        System.out.println("申请数量: " + applications.size());
        System.out.println("黑名单数量: " + blacklistRecordRepository.count());
        System.out.println("预警数量: " + qualificationAlertRepository.count());
    }

    private List<Institution> createInstitutions() {
        List<Institution> institutions = new ArrayList<>();
        String[] names = {
            "市第一人民医院", "市第二人民医院", "市第三人民医院",
            "仁爱私立医院", "康美私立医院", "博仁私立医院",
            "华康检测中心", "瑞慈检测中心", "美年大健康检测",
            "眼科专科医院", "口腔专科医院", "妇幼保健院",
            "社区卫生服务中心", "中医医院", "体检中心"
        };
        
        InstitutionType[] types = {
            InstitutionType.PUBLIC_MEDICAL, InstitutionType.PUBLIC_MEDICAL, InstitutionType.PUBLIC_MEDICAL,
            InstitutionType.PRIVATE_MEDICAL, InstitutionType.PRIVATE_MEDICAL, InstitutionType.PRIVATE_MEDICAL,
            InstitutionType.THIRD_PARTY_TESTING, InstitutionType.THIRD_PARTY_TESTING, InstitutionType.THIRD_PARTY_TESTING,
            InstitutionType.PRIVATE_MEDICAL, InstitutionType.PRIVATE_MEDICAL, InstitutionType.PUBLIC_MEDICAL,
            InstitutionType.PUBLIC_MEDICAL, InstitutionType.PUBLIC_MEDICAL, InstitutionType.THIRD_PARTY_TESTING
        };

        for (int i = 0; i < 15; i++) {
            Institution institution = new Institution();
            institution.setName(names[i]);
            institution.setCreditCode("9131000" + String.format("%08d", i + 1000000) + "A" + (char)('A' + i));
            institution.setType(types[i]);
            institution.setLegalRepresentative("法人代表" + (i + 1));
            institution.setRegisteredCapital(BigDecimal.valueOf(1000000 + random.nextInt(9000000)));
            institution.setQualificationCertificates("[\"营业执照\",\"组织机构代码证\"]");
            institutions.add(institutionRepository.save(institution));
        }
        return institutions;
    }

    private List<Qualification> createQualifications(List<Institution> institutions) {
        List<Qualification> qualifications = new ArrayList<>();
        CertificateType[] certTypes = CertificateType.values();
        String[] authorities = {"市卫健委", "市质量监督局", "ISO认证中心"};

        int count = 0;
        for (Institution institution : institutions) {
            int certCount = 2 + random.nextInt(3);
            for (int i = 0; i < certCount && count < 40; i++) {
                Qualification q = new Qualification();
                q.setInstitution(institution);
                q.setCertificateType(certTypes[random.nextInt(certTypes.length)]);
                q.setCertificateNumber("CERT-" + (count + 10000));
                q.setIssuingAuthority(authorities[random.nextInt(authorities.length)]);
                q.setValidFrom(LocalDate.of(2022 + random.nextInt(2), 1 + random.nextInt(12), 1 + random.nextInt(28)));
                q.setValidTo(LocalDate.of(2025 + random.nextInt(3), 1 + random.nextInt(12), 1 + random.nextInt(28)));
                q.setAnnualInspectionStatus(AnnualInspectionStatus.values()[random.nextInt(3)]);
                qualifications.add(qualificationRepository.save(q));
                count++;
            }
        }
        return qualifications;
    }

    private List<PersonnelRecord> createPersonnelRecords(List<Institution> institutions) {
        List<PersonnelRecord> personnelRecords = new ArrayList<>();
        String[] firstNames = {"张", "李", "王", "刘", "陈", "杨", "黄", "赵", "周", "吴"};
        String[] lastNames = {"伟", "芳", "娜", "敏", "静", "丽", "强", "磊", "洋", "艳"};
        PositionType[] positions = PositionType.values();

        int count = 0;
        for (Institution institution : institutions) {
            int personCount = 3 + random.nextInt(4);
            for (int i = 0; i < personCount && count < 60; i++) {
                PersonnelRecord p = new PersonnelRecord();
                p.setInstitution(institution);
                p.setName(firstNames[random.nextInt(firstNames.length)] + lastNames[random.nextInt(lastNames.length)]);
                p.setIdCardNumber("310101199" + random.nextInt(10) + String.format("%02d", 1 + random.nextInt(12)) + String.format("%02d", 1 + random.nextInt(28)) + String.format("%04d", random.nextInt(10000)));
                p.setPracticeLicenseNumber("LIC-" + (count + 10000));
                p.setPositionType(positions[random.nextInt(positions.length)]);
                p.setAllowedEntry(random.nextDouble() > 0.1);
                personnelRecords.add(personnelRecordRepository.save(p));
                count++;
            }
        }
        return personnelRecords;
    }

    private List<EntryApplication> createEntryApplications(List<Institution> institutions, List<PersonnelRecord> personnelRecords) {
        List<EntryApplication> applications = new ArrayList<>();
        String[] schools = {"第一中学", "第二中学", "实验小学", "第一小学", "第三中学", "实验中学", "第四小学", "第五中学"};
        ServiceProject[] projects = ServiceProject.values();
        ApprovalStatus[] statuses = ApprovalStatus.values();

        for (int i = 0; i < 30; i++) {
            Institution institution = institutions.get(random.nextInt(institutions.size()));
            
            List<PersonnelRecord> allowedPersonnel = personnelRecords.stream()
                    .filter(p -> p.getInstitution().getId().equals(institution.getId()) && p.isAllowedEntry())
                    .toList();

            EntryApplication app = new EntryApplication();
            app.setInstitution(institution);
            app.setSchoolName(schools[random.nextInt(schools.length)]);
            app.setServiceProject(projects[random.nextInt(projects.length)]);
            
            if (!allowedPersonnel.isEmpty()) {
                int assignCount = Math.min(1 + random.nextInt(2), allowedPersonnel.size());
                List<Long> assignedIds = new ArrayList<>();
                for (int j = 0; j < assignCount; j++) {
                    assignedIds.add(allowedPersonnel.get(random.nextInt(allowedPersonnel.size())).getId());
                }
                app.setAssignedPersonnel(assignedIds);
            }
            
            app.setApprovalStatus(statuses[random.nextInt(statuses.length)]);
            if (app.getApprovalStatus() == ApprovalStatus.REJECTED) {
                app.setRejectionReason("申请材料不完整");
            }
            applications.add(entryApplicationRepository.save(app));
        }
        return applications;
    }

    private void createBlacklistRecords(List<Institution> institutions) {
        BlacklistReason[] reasons = BlacklistReason.values();
        List<Institution> blacklistedInstitutions = institutions.subList(institutions.size() - 5, institutions.size());

        for (int i = 0; i < 5; i++) {
            BlacklistRecord record = new BlacklistRecord();
            record.setInstitution(blacklistedInstitutions.get(i));
            record.setReason(reasons[i % reasons.length]);
            record.setListingDate(LocalDate.now().minusMonths(random.nextInt(12)));
            record.setRemovalConditions("整改并提交复核申请");
            record.setStatus(BlacklistStatus.ACTIVE);
            record.setExpiryDate(record.getListingDate().plusYears(3));
            blacklistRecordRepository.save(record);
        }
    }

    private void createTestExpiringQualifications(List<Institution> institutions) {
        LocalDate today = LocalDate.now();
        CertificateType[] certTypes = CertificateType.values();
        String[] authorities = {"市卫健委", "市质量监督局", "ISO认证中心"};

        Institution expiringInstitution = institutions.get(0);
        Qualification yellowQual = new Qualification();
        yellowQual.setInstitution(expiringInstitution);
        yellowQual.setCertificateType(certTypes[random.nextInt(certTypes.length)]);
        yellowQual.setCertificateNumber("CERT-WARN-001");
        yellowQual.setIssuingAuthority(authorities[random.nextInt(authorities.length)]);
        yellowQual.setValidFrom(today.minusYears(2));
        yellowQual.setValidTo(today.plusDays(15));
        yellowQual.setAnnualInspectionStatus(AnnualInspectionStatus.PASS);
        qualificationRepository.save(yellowQual);

        Institution expiredInstitution = institutions.get(1);
        Qualification redQual = new Qualification();
        redQual.setInstitution(expiredInstitution);
        redQual.setCertificateType(certTypes[random.nextInt(certTypes.length)]);
        redQual.setCertificateNumber("CERT-EXPIRED-001");
        redQual.setIssuingAuthority(authorities[random.nextInt(authorities.length)]);
        redQual.setValidFrom(today.minusYears(3));
        redQual.setValidTo(today.minusDays(10));
        redQual.setAnnualInspectionStatus(AnnualInspectionStatus.PASS);
        qualificationRepository.save(redQual);

        Qualification anotherExpired = new Qualification();
        anotherExpired.setInstitution(expiredInstitution);
        anotherExpired.setCertificateType(certTypes[random.nextInt(certTypes.length)]);
        anotherExpired.setCertificateNumber("CERT-EXPIRED-002");
        anotherExpired.setIssuingAuthority(authorities[random.nextInt(authorities.length)]);
        anotherExpired.setValidFrom(today.minusYears(2));
        anotherExpired.setValidTo(today.minusDays(5));
        anotherExpired.setAnnualInspectionStatus(AnnualInspectionStatus.PASS);
        qualificationRepository.save(anotherExpired);

        Institution anotherWarning = institutions.get(2);
        Qualification yellowQual2 = new Qualification();
        yellowQual2.setInstitution(anotherWarning);
        yellowQual2.setCertificateType(certTypes[random.nextInt(certTypes.length)]);
        yellowQual2.setCertificateNumber("CERT-WARN-002");
        yellowQual2.setIssuingAuthority(authorities[random.nextInt(authorities.length)]);
        yellowQual2.setValidFrom(today.minusYears(1));
        yellowQual2.setValidTo(today.plusDays(25));
        yellowQual2.setAnnualInspectionStatus(AnnualInspectionStatus.PASS);
        qualificationRepository.save(yellowQual2);
    }
}
