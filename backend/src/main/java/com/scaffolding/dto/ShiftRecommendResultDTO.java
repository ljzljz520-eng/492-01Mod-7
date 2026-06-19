package com.scaffolding.dto;

import lombok.Data;
import java.util.List;

@Data
public class ShiftRecommendResultDTO {
    private String positionName;
    private List<Long> requiredSkillIds;
    private List<ShiftMemberDTO> candidates;
    private ShiftMemberDTO selected;
    private Integer totalScore;
    private String message;
}
