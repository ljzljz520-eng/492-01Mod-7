package com.scaffolding.dto;

import lombok.Data;

@Data
public class SkillDetailDTO {
    private Long skillId;
    private String skillCode;
    private String skillName;
    private String skillType;
    private Integer proficiency;
    private Integer certified;
    private Boolean hasSkill;
}
