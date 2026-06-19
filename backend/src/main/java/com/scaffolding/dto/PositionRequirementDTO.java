package com.scaffolding.dto;

import lombok.Data;
import java.util.List;

@Data
public class PositionRequirementDTO {
    private String positionName;
    private List<Long> requiredSkillIds;
    private Integer minProficiency;
    private Integer count;
}
