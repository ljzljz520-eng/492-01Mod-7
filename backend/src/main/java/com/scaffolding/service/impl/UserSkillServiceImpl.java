package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.dto.SkillDetailDTO;
import com.scaffolding.dto.SkillRiskDTO;
import com.scaffolding.dto.UserSkillDTO;
import com.scaffolding.entity.Skill;
import com.scaffolding.entity.User;
import com.scaffolding.entity.UserSkill;
import com.scaffolding.mapper.SkillMapper;
import com.scaffolding.mapper.UserMapper;
import com.scaffolding.mapper.UserSkillMapper;
import com.scaffolding.service.UserSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserSkillServiceImpl extends ServiceImpl<UserSkillMapper, UserSkill> implements UserSkillService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SkillMapper skillMapper;

    @Override
    public List<UserSkillDTO> getSkillMatrix(String nickname) {
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getRole, "worker");
        userWrapper.eq(User::getDeleted, 0);
        if (StringUtils.hasText(nickname)) {
            userWrapper.like(User::getNickname, nickname);
        }
        userWrapper.orderByAsc(User::getId);
        List<User> users = userMapper.selectList(userWrapper);

        List<Skill> skills = skillMapper.selectList(
            new LambdaQueryWrapper<Skill>().orderByAsc(Skill::getSortOrder)
        );

        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        List<UserSkill> userSkills = new ArrayList<>();
        if (!userIds.isEmpty()) {
            userSkills = this.list(
                new LambdaQueryWrapper<UserSkill>().in(UserSkill::getUserId, userIds)
            );
        }

        Map<Long, List<UserSkill>> userSkillMap = userSkills.stream()
            .collect(Collectors.groupingBy(UserSkill::getUserId));

        List<UserSkillDTO> result = new ArrayList<>();
        for (User user : users) {
            UserSkillDTO dto = new UserSkillDTO();
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());

            List<SkillDetailDTO> skillDetails = new ArrayList<>();
            List<UserSkill> userSkillList = userSkillMap.getOrDefault(user.getId(), Collections.emptyList());
            Map<Long, UserSkill> skillMap = userSkillList.stream()
                .collect(Collectors.toMap(UserSkill::getSkillId, us -> us));

            for (Skill skill : skills) {
                SkillDetailDTO detail = new SkillDetailDTO();
                detail.setSkillId(skill.getId());
                detail.setSkillCode(skill.getSkillCode());
                detail.setSkillName(skill.getSkillName());
                detail.setSkillType(skill.getSkillType());

                UserSkill us = skillMap.get(skill.getId());
                if (us != null) {
                    detail.setHasSkill(true);
                    detail.setProficiency(us.getProficiency());
                    detail.setCertified(us.getCertified());
                } else {
                    detail.setHasSkill(false);
                    detail.setProficiency(0);
                    detail.setCertified(0);
                }
                skillDetails.add(detail);
            }
            dto.setSkills(skillDetails);
            result.add(dto);
        }
        return result;
    }

    @Override
    public Page<UserSkillDTO> getSkillMatrixPage(Long current, Long size, String nickname) {
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getRole, "worker");
        userWrapper.eq(User::getDeleted, 0);
        if (StringUtils.hasText(nickname)) {
            userWrapper.like(User::getNickname, nickname);
        }
        userWrapper.orderByAsc(User::getId);
        Page<User> userPage = new Page<>(current, size);
        userPage = userMapper.selectPage(userPage, userWrapper);

        List<Skill> skills = skillMapper.selectList(
            new LambdaQueryWrapper<Skill>().orderByAsc(Skill::getSortOrder)
        );

        List<Long> userIds = userPage.getRecords().stream()
            .map(User::getId).collect(Collectors.toList());
        List<UserSkill> userSkills = new ArrayList<>();
        if (!userIds.isEmpty()) {
            userSkills = this.list(
                new LambdaQueryWrapper<UserSkill>().in(UserSkill::getUserId, userIds)
            );
        }

        Map<Long, List<UserSkill>> userSkillMap = userSkills.stream()
            .collect(Collectors.groupingBy(UserSkill::getUserId));

        List<UserSkillDTO> records = new ArrayList<>();
        for (User user : userPage.getRecords()) {
            UserSkillDTO dto = new UserSkillDTO();
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());

            List<SkillDetailDTO> skillDetails = new ArrayList<>();
            List<UserSkill> userSkillList = userSkillMap.getOrDefault(user.getId(), Collections.emptyList());
            Map<Long, UserSkill> skillMap = userSkillList.stream()
                .collect(Collectors.toMap(UserSkill::getSkillId, us -> us));

            for (Skill skill : skills) {
                SkillDetailDTO detail = new SkillDetailDTO();
                detail.setSkillId(skill.getId());
                detail.setSkillCode(skill.getSkillCode());
                detail.setSkillName(skill.getSkillName());
                detail.setSkillType(skill.getSkillType());

                UserSkill us = skillMap.get(skill.getId());
                if (us != null) {
                    detail.setHasSkill(true);
                    detail.setProficiency(us.getProficiency());
                    detail.setCertified(us.getCertified());
                } else {
                    detail.setHasSkill(false);
                    detail.setProficiency(0);
                    detail.setCertified(0);
                }
                skillDetails.add(detail);
            }
            dto.setSkills(skillDetails);
            records.add(dto);
        }

        Page<UserSkillDTO> result = new Page<>(current, size, userPage.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public List<SkillRiskDTO> getSkillRiskWarning() {
        List<Skill> skills = skillMapper.selectList(
            new LambdaQueryWrapper<Skill>().orderByAsc(Skill::getSortOrder)
        );

        List<SkillRiskDTO> result = new ArrayList<>();
        for (Skill skill : skills) {
            SkillRiskDTO dto = new SkillRiskDTO();
            dto.setSkillId(skill.getId());
            dto.setSkillCode(skill.getSkillCode());
            dto.setSkillName(skill.getSkillName());
            dto.setSkillType(skill.getSkillType());

            LambdaQueryWrapper<UserSkill> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserSkill::getSkillId, skill.getId());
            wrapper.eq(UserSkill::getDeleted, 0);
            List<UserSkill> userSkills = this.list(wrapper);

            int userCount = userSkills.size();
            int certifiedCount = (int) userSkills.stream()
                .filter(us -> us.getCertified() != null && us.getCertified() == 1)
                .count();

            dto.setUserCount(userCount);
            dto.setCertifiedCount(certifiedCount);

            boolean isRisk = false;
            String riskLevel = "normal";
            String riskMessage = "";

            if ("key".equals(skill.getSkillType())) {
                if (userCount <= 1) {
                    isRisk = true;
                    riskLevel = "critical";
                    riskMessage = "关键技能【" + skill.getSkillName() + "】仅剩" + userCount + "人，存在严重替补风险！";
                } else if (userCount <= 2) {
                    isRisk = true;
                    riskLevel = "warning";
                    riskMessage = "关键技能【" + skill.getSkillName() + "】仅有" + userCount + "人，替补人手不足，请注意补充。";
                }
            } else {
                if (userCount == 0) {
                    isRisk = true;
                    riskLevel = "warning";
                    riskMessage = "技能【" + skill.getSkillName() + "】暂无人员掌握。";
                }
            }

            dto.setIsRisk(isRisk);
            dto.setRiskLevel(riskLevel);
            dto.setRiskMessage(riskMessage);
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<UserSkillDTO> recommendWorkers(List<Long> skillIds, Integer minProficiency, Integer limit) {
        if (skillIds == null || skillIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Skill> skills = skillMapper.selectBatchIds(skillIds);
        List<UserSkill> userSkills = this.list(
            new LambdaQueryWrapper<UserSkill>().in(UserSkill::getSkillId, skillIds)
        );

        Map<Long, List<UserSkill>> userSkillMap = userSkills.stream()
            .collect(Collectors.groupingBy(UserSkill::getUserId));

        List<Long> candidateUserIds = new ArrayList<>();
        Map<Long, Integer> userScoreMap = new HashMap<>();

        for (Map.Entry<Long, List<UserSkill>> entry : userSkillMap.entrySet()) {
            Long userId = entry.getKey();
            List<UserSkill> usList = entry.getValue();

            Set<Long> userSkillIds = usList.stream()
                .map(UserSkill::getSkillId).collect(Collectors.toSet());

            boolean hasAllSkills = true;
            int totalProficiency = 0;
            for (Long skillId : skillIds) {
                if (!userSkillIds.contains(skillId)) {
                    hasAllSkills = false;
                    break;
                }
                UserSkill us = usList.stream()
                    .filter(u -> u.getSkillId().equals(skillId))
                    .findFirst().orElse(null);
                if (us != null) {
                    if (minProficiency != null && us.getProficiency() < minProficiency) {
                        hasAllSkills = false;
                        break;
                    }
                    totalProficiency += us.getProficiency();
                }
            }

            if (hasAllSkills) {
                candidateUserIds.add(userId);
                userScoreMap.put(userId, totalProficiency);
            }
        }

        candidateUserIds.sort((a, b) -> userScoreMap.get(b) - userScoreMap.get(a));

        if (limit != null && limit > 0 && candidateUserIds.size() > limit) {
            candidateUserIds = candidateUserIds.subList(0, limit);
        }

        if (candidateUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> users = userMapper.selectBatchIds(candidateUserIds);
        Map<Long, User> userMap = users.stream()
            .collect(Collectors.toMap(User::getId, u -> u));

        List<UserSkillDTO> result = new ArrayList<>();
        for (Long userId : candidateUserIds) {
            User user = userMap.get(userId);
            if (user == null) continue;

            UserSkillDTO dto = new UserSkillDTO();
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());

            List<SkillDetailDTO> skillDetails = new ArrayList<>();
            List<UserSkill> usList = userSkillMap.getOrDefault(userId, Collections.emptyList());
            Map<Long, UserSkill> usMap = usList.stream()
                .collect(Collectors.toMap(UserSkill::getSkillId, us -> us));

            for (Skill skill : skills) {
                SkillDetailDTO detail = new SkillDetailDTO();
                detail.setSkillId(skill.getId());
                detail.setSkillCode(skill.getSkillCode());
                detail.setSkillName(skill.getSkillName());
                detail.setSkillType(skill.getSkillType());

                UserSkill us = usMap.get(skill.getId());
                if (us != null) {
                    detail.setHasSkill(true);
                    detail.setProficiency(us.getProficiency());
                    detail.setCertified(us.getCertified());
                } else {
                    detail.setHasSkill(false);
                    detail.setProficiency(0);
                    detail.setCertified(0);
                }
                skillDetails.add(detail);
            }
            dto.setSkills(skillDetails);
            result.add(dto);
        }
        return result;
    }

    @Override
    public boolean saveUserSkill(Long userId, Long skillId, Integer proficiency, Integer certified, Long operatorId) {
        LambdaQueryWrapper<UserSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSkill::getUserId, userId);
        wrapper.eq(UserSkill::getSkillId, skillId);
        UserSkill existing = this.getOne(wrapper);

        if (existing != null) {
            existing.setProficiency(proficiency);
            existing.setCertified(certified);
            return this.updateById(existing);
        } else {
            UserSkill userSkill = new UserSkill();
            userSkill.setUserId(userId);
            userSkill.setSkillId(skillId);
            userSkill.setProficiency(proficiency);
            userSkill.setCertified(certified);
            return this.save(userSkill);
        }
    }

    @Override
    public boolean updateUserSkill(Long id, Integer proficiency, Integer certified, Long operatorId) {
        UserSkill userSkill = new UserSkill();
        userSkill.setId(id);
        userSkill.setProficiency(proficiency);
        userSkill.setCertified(certified);
        return this.updateById(userSkill);
    }
}
