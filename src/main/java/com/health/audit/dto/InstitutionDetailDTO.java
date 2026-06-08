package com.health.audit.dto;

import com.health.audit.entity.Institution;
import com.health.audit.entity.QualificationAlert;
import com.health.audit.entity.enums.AlertLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionDetailDTO {
    private Institution institution;
    private List<QualificationAlert> activeAlerts;
    private AlertLevel highestAlertLevel;
    private boolean hasAlert;

    public static InstitutionDetailDTO from(Institution institution, List<QualificationAlert> activeAlerts) {
        InstitutionDetailDTO dto = new InstitutionDetailDTO();
        dto.setInstitution(institution);
        dto.setActiveAlerts(activeAlerts);
        dto.setHasAlert(!activeAlerts.isEmpty());

        AlertLevel highest = null;
        for (QualificationAlert alert : activeAlerts) {
            if (alert.getAlertLevel() == AlertLevel.RED_EXPIRED) {
                highest = AlertLevel.RED_EXPIRED;
                break;
            } else if (alert.getAlertLevel() == AlertLevel.YELLOW_30_DAYS && highest == null) {
                highest = AlertLevel.YELLOW_30_DAYS;
            }
        }
        dto.setHighestAlertLevel(highest);

        return dto;
    }
}
