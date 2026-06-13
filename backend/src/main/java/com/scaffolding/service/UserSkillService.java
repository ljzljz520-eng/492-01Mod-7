package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.dto.SkillRiskDTO;
import com.scaffolding.dto.UserSkillDTO;
import com.scaffolding.entity.UserSkill;
import java.util.List;

public interface UserSkillService extends IService<UserSkill> {
    List<UserSkillDTO> getSkillMatrix(String nickname);

    Page<UserSkillDTO> getSkillMatrixPage(Long current, Long size, String nickname);

    List<SkillRiskDTO> getSkillRiskWarning();

    List<UserSkillDTO> recommendWorkers(List<Long> skillIds, Integer minProficiency, Integer limit);

    boolean saveUserSkill(Long userId, Long skillId, Integer proficiency, Integer certified, Long operatorId);

    boolean updateUserSkill(Long id, Integer proficiency, Integer certified, Long operatorId);
}
