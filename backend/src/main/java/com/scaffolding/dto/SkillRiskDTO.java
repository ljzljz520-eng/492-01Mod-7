package com.scaffolding.dto;

import lombok.Data;

@Data
public class SkillRiskDTO {
    private Long skillId;
    private String skillCode;
    private String skillName;
    private String skillType;
    private Integer userCount;
    private Integer certifiedCount;
    private Boolean isRisk;
    private String riskLevel;
    private String riskMessage;
}
