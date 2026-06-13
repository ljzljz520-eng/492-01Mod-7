package com.scaffolding.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserSkillDTO {
    private Long userId;
    private String nickname;
    private String username;
    private List<SkillDetailDTO> skills;
}
