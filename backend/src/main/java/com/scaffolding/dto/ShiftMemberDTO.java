package com.scaffolding.dto;

import lombok.Data;
import java.util.List;

@Data
public class ShiftMemberDTO {
    private Long userId;
    private String nickname;
    private String position;
    private List<SkillDetailDTO> skills;
    private Integer matchScore;
}
