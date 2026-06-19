package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.dto.PositionRequirementDTO;
import com.scaffolding.dto.ShiftDetailDTO;
import com.scaffolding.dto.ShiftMemberDTO;
import com.scaffolding.dto.ShiftRecommendResultDTO;
import com.scaffolding.dto.SkillDetailDTO;
import com.scaffolding.entity.*;
import com.scaffolding.mapper.*;
import com.scaffolding.service.ShiftService;
import com.scaffolding.service.UserSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShiftServiceImpl extends ServiceImpl<ShiftMapper, Shift> implements ShiftService {

    @Autowired
    private ShiftMemberMapper shiftMemberMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private UserSkillMapper userSkillMapper;

    @Autowired
    private UserSkillService userSkillService;

    @Override
    public Page<Shift> pageQuery(Long current, Long size, String shiftName, String status, LocalDate startDate, LocalDate endDate) {
        Page<Shift> page = new Page<>(current, size);
        LambdaQueryWrapper<Shift> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(shiftName)) {
            wrapper.like(Shift::getShiftName, shiftName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Shift::getStatus, status);
        }
        if (startDate != null) {
            wrapper.ge(Shift::getShiftDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Shift::getShiftDate, endDate);
        }

        wrapper.orderByDesc(Shift::getShiftDate);
        wrapper.orderByDesc(Shift::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public ShiftDetailDTO getShiftDetail(Long id) {
        Shift shift = this.getById(id);
        if (shift == null) {
            return null;
        }

        ShiftDetailDTO dto = new ShiftDetailDTO();
        dto.setShiftId(shift.getId());
        dto.setShiftName(shift.getShiftName());
        dto.setShiftDate(shift.getShiftDate() != null ? shift.getShiftDate().toString() : null);
        dto.setShiftType(shift.getShiftType());
        dto.setStartTime(shift.getStartTime() != null ? shift.getStartTime().toString() : null);
        dto.setEndTime(shift.getEndTime() != null ? shift.getEndTime().toString() : null);
        dto.setSupervisorId(shift.getSupervisorId());
        dto.setStatus(shift.getStatus());
        dto.setRemark(shift.getRemark());

        if (shift.getSupervisorId() != null) {
            User supervisor = userMapper.selectById(shift.getSupervisorId());
            if (supervisor != null) {
                dto.setSupervisorName(supervisor.getNickname());
            }
        }

        List<ShiftMember> members = shiftMemberMapper.selectList(
            new LambdaQueryWrapper<ShiftMember>()
                .eq(ShiftMember::getShiftId, id)
                .orderByAsc(ShiftMember::getSortOrder)
        );

        List<ShiftMemberDTO> memberDTOs = new ArrayList<>();
        List<Long> userIds = members.stream().map(ShiftMember::getUserId).collect(Collectors.toList());

        List<Skill> allSkills = skillMapper.selectList(
            new LambdaQueryWrapper<Skill>().orderByAsc(Skill::getSortOrder)
        );

        List<UserSkill> allUserSkills = new ArrayList<>();
        if (!userIds.isEmpty()) {
            allUserSkills = userSkillMapper.selectList(
                new LambdaQueryWrapper<UserSkill>().in(UserSkill::getUserId, userIds)
            );
        }

        Map<Long, List<UserSkill>> userSkillMap = allUserSkills.stream()
            .collect(Collectors.groupingBy(UserSkill::getUserId));

        for (ShiftMember member : members) {
            User user = userMapper.selectById(member.getUserId());
            if (user == null) continue;

            ShiftMemberDTO memberDTO = new ShiftMemberDTO();
            memberDTO.setUserId(user.getId());
            memberDTO.setNickname(user.getNickname());
            memberDTO.setPosition(member.getPosition());

            List<SkillDetailDTO> skillDetails = new ArrayList<>();
            List<UserSkill> userSkills = userSkillMap.getOrDefault(user.getId(), Collections.emptyList());
            Map<Long, UserSkill> usMap = userSkills.stream()
                .collect(Collectors.toMap(UserSkill::getSkillId, us -> us));

            for (Skill skill : allSkills) {
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
            memberDTO.setSkills(skillDetails);
            memberDTOs.add(memberDTO);
        }
        dto.setMembers(memberDTOs);
        return dto;
    }

    @Override
    @Transactional
    public boolean createShift(Shift shift, List<Long> userIds, Long supervisorId) {
        shift.setStatus("draft");
        shift.setCreateTime(LocalDateTime.now());
        shift.setUpdateTime(LocalDateTime.now());
        boolean saved = this.save(shift);
        if (!saved) return false;

        if (userIds != null && !userIds.isEmpty()) {
            int sortOrder = 1;
            for (Long userId : userIds) {
                ShiftMember member = new ShiftMember();
                member.setShiftId(shift.getId());
                member.setUserId(userId);
                member.setSortOrder(sortOrder++);
                member.setCreateTime(LocalDateTime.now());
                member.setUpdateTime(LocalDateTime.now());
                shiftMemberMapper.insert(member);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean updateShiftMembers(Long shiftId, List<Long> userIds, Long supervisorId) {
        shiftMemberMapper.delete(
            new LambdaQueryWrapper<ShiftMember>().eq(ShiftMember::getShiftId, shiftId)
        );

        if (userIds != null && !userIds.isEmpty()) {
            int sortOrder = 1;
            for (Long userId : userIds) {
                ShiftMember member = new ShiftMember();
                member.setShiftId(shiftId);
                member.setUserId(userId);
                member.setSortOrder(sortOrder++);
                member.setCreateTime(LocalDateTime.now());
                member.setUpdateTime(LocalDateTime.now());
                shiftMemberMapper.insert(member);
            }
        }

        Shift shift = new Shift();
        shift.setId(shiftId);
        shift.setUpdateTime(LocalDateTime.now());
        this.updateById(shift);
        return true;
    }

    @Override
    public boolean publishShift(Long id, Long supervisorId) {
        Shift shift = new Shift();
        shift.setId(id);
        shift.setStatus("published");
        shift.setUpdateTime(LocalDateTime.now());
        return this.updateById(shift);
    }

    @Override
    public boolean completeShift(Long id, Long supervisorId) {
        Shift shift = new Shift();
        shift.setId(id);
        shift.setStatus("completed");
        shift.setUpdateTime(LocalDateTime.now());
        return this.updateById(shift);
    }

    @Override
    public List<ShiftMemberDTO> recommendShiftMembers(Long shiftId, List<Long> requiredSkillIds, Integer memberCount) {
        if (requiredSkillIds == null || requiredSkillIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Skill> requiredSkills = skillMapper.selectBatchIds(requiredSkillIds);
        if (requiredSkills.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserSkill> allUserSkills = userSkillMapper.selectList(
            new LambdaQueryWrapper<UserSkill>().in(UserSkill::getSkillId, requiredSkillIds)
        );

        Map<Long, List<UserSkill>> userSkillMap = allUserSkills.stream()
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
            int certifiedCount = 0;

            for (Long skillId : requiredSkillIds) {
                if (!userSkillIds.contains(skillId)) {
                    hasAllSkills = false;
                    break;
                }
                UserSkill us = usList.stream()
                    .filter(u -> u.getSkillId().equals(skillId))
                    .findFirst().orElse(null);
                if (us != null) {
                    totalProficiency += us.getProficiency();
                    if (us.getCertified() != null && us.getCertified() == 1) {
                        certifiedCount++;
                    }
                }
            }

            if (hasAllSkills) {
                candidateUserIds.add(userId);
                int score = totalProficiency * 10 + certifiedCount * 20;
                userScoreMap.put(userId, score);
            }
        }

        candidateUserIds.sort((a, b) -> userScoreMap.get(b) - userScoreMap.get(a));

        if (memberCount != null && memberCount > 0 && candidateUserIds.size() > memberCount) {
            candidateUserIds = candidateUserIds.subList(0, memberCount);
        }

        if (candidateUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> users = userMapper.selectBatchIds(candidateUserIds);
        Map<Long, User> userMap = users.stream()
            .collect(Collectors.toMap(User::getId, u -> u));

        List<ShiftMemberDTO> result = new ArrayList<>();
        for (Long userId : candidateUserIds) {
            User user = userMap.get(userId);
            if (user == null) continue;

            ShiftMemberDTO dto = new ShiftMemberDTO();
            dto.setUserId(user.getId());
            dto.setNickname(user.getNickname());
            dto.setMatchScore(userScoreMap.get(userId));

            List<SkillDetailDTO> skillDetails = new ArrayList<>();
            List<UserSkill> usList = userSkillMap.getOrDefault(userId, Collections.emptyList());
            Map<Long, UserSkill> usMap = usList.stream()
                .collect(Collectors.toMap(UserSkill::getSkillId, us -> us));

            for (Skill skill : requiredSkills) {
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
    public List<ShiftRecommendResultDTO> recommendShiftTeam(Long shiftId, List<PositionRequirementDTO> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> allSkillIds = new ArrayList<>();
        for (PositionRequirementDTO req : requirements) {
            if (req.getRequiredSkillIds() != null) {
                allSkillIds.addAll(req.getRequiredSkillIds());
            }
        }

        List<Skill> allSkills = skillMapper.selectBatchIds(new ArrayList<>(new HashSet<>(allSkillIds)));
        Map<Long, Skill> skillMap = allSkills.stream()
            .collect(Collectors.toMap(Skill::getId, s -> s));

        List<UserSkill> allUserSkills = userSkillMapper.selectList(
            new LambdaQueryWrapper<UserSkill>().in(UserSkill::getSkillId, allSkillIds)
        );

        Map<Long, List<UserSkill>> userSkillMap = allUserSkills.stream()
            .collect(Collectors.groupingBy(UserSkill::getUserId));

        List<Long> allUserIds = new ArrayList<>(userSkillMap.keySet());
        List<User> allUsers = userMapper.selectBatchIds(allUserIds);
        Map<Long, User> userMap = allUsers.stream()
            .collect(Collectors.toMap(User::getId, u -> u));

        List<ShiftRecommendResultDTO> result = new ArrayList<>();
        Set<Long> selectedUserIds = new HashSet<>();

        for (PositionRequirementDTO req : requirements) {
            ShiftRecommendResultDTO positionResult = new ShiftRecommendResultDTO();
            positionResult.setPositionName(req.getPositionName());
            positionResult.setRequiredSkillIds(req.getRequiredSkillIds());

            List<ShiftMemberDTO> candidates = findCandidatesForPosition(
                req, userSkillMap, userMap, skillMap, selectedUserIds
            );

            positionResult.setCandidates(candidates);

            if (candidates.isEmpty()) {
                positionResult.setMessage("没有找到满足技能要求的工人");
            } else {
                int count = req.getCount() != null ? req.getCount() : 1;
                int selectCount = Math.min(count, candidates.size());
                List<ShiftMemberDTO> selectedList = candidates.subList(0, selectCount);
                
                if (selectCount > 0) {
                    positionResult.setSelected(selectedList.get(0));
                    positionResult.setTotalScore(selectedList.get(0).getMatchScore());
                }

                for (ShiftMemberDTO member : selectedList) {
                    selectedUserIds.add(member.getUserId());
                }
            }

            result.add(positionResult);
        }

        return result;
    }

    private List<ShiftMemberDTO> findCandidatesForPosition(
            PositionRequirementDTO req,
            Map<Long, List<UserSkill>> userSkillMap,
            Map<Long, User> userMap,
            Map<Long, Skill> skillMap,
            Set<Long> excludedUserIds) {

        List<Long> requiredSkillIds = req.getRequiredSkillIds();
        if (requiredSkillIds == null || requiredSkillIds.isEmpty()) {
            return Collections.emptyList();
        }

        Integer minProficiency = req.getMinProficiency() != null ? req.getMinProficiency() : 1;

        List<Map.Entry<Long, Integer>> scoredUsers = new ArrayList<>();

        for (Map.Entry<Long, List<UserSkill>> entry : userSkillMap.entrySet()) {
            Long userId = entry.getKey();
            if (excludedUserIds.contains(userId)) {
                continue;
            }

            List<UserSkill> usList = entry.getValue();
            Set<Long> userSkillIds = usList.stream()
                .map(UserSkill::getSkillId).collect(Collectors.toSet());

            boolean hasAllSkills = true;
            int totalProficiency = 0;
            int certifiedCount = 0;

            for (Long skillId : requiredSkillIds) {
                if (!userSkillIds.contains(skillId)) {
                    hasAllSkills = false;
                    break;
                }
                UserSkill us = usList.stream()
                    .filter(u -> u.getSkillId().equals(skillId))
                    .findFirst().orElse(null);
                if (us != null) {
                    if (us.getProficiency() < minProficiency) {
                        hasAllSkills = false;
                        break;
                    }
                    totalProficiency += us.getProficiency();
                    if (us.getCertified() != null && us.getCertified() == 1) {
                        certifiedCount++;
                    }
                }
            }

            if (hasAllSkills) {
                int score = totalProficiency * 10 + certifiedCount * 20;
                scoredUsers.add(new AbstractMap.SimpleEntry<>(userId, score));
            }
        }

        scoredUsers.sort((a, b) -> b.getValue() - a.getValue());

        List<ShiftMemberDTO> result = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : scoredUsers) {
            Long userId = entry.getKey();
            User user = userMap.get(userId);
            if (user == null) continue;

            ShiftMemberDTO dto = new ShiftMemberDTO();
            dto.setUserId(user.getId());
            dto.setNickname(user.getNickname());
            dto.setMatchScore(entry.getValue());

            List<SkillDetailDTO> skillDetails = new ArrayList<>();
            List<UserSkill> usList = userSkillMap.getOrDefault(userId, Collections.emptyList());
            Map<Long, UserSkill> usMap = usList.stream()
                .collect(Collectors.toMap(UserSkill::getSkillId, us -> us));

            for (Long skillId : requiredSkillIds) {
                Skill skill = skillMap.get(skillId);
                if (skill == null) continue;

                SkillDetailDTO detail = new SkillDetailDTO();
                detail.setSkillId(skill.getId());
                detail.setSkillCode(skill.getSkillCode());
                detail.setSkillName(skill.getSkillName());
                detail.setSkillType(skill.getSkillType());

                UserSkill us = usMap.get(skillId);
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
}
