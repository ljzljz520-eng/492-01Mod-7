package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.*;
import com.scaffolding.mapper.*;
import com.scaffolding.service.ShiftEvaluationService;
import com.scaffolding.service.UserSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShiftEvaluationServiceImpl extends ServiceImpl<ShiftEvaluationMapper, ShiftEvaluation> implements ShiftEvaluationService {

    @Autowired
    private UserSkillMapper userSkillMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShiftMapper shiftMapper;

    @Autowired
    private UserSkillService userSkillService;

    @Override
    public List<ShiftEvaluation> getEvaluationsByShift(Long shiftId) {
        LambdaQueryWrapper<ShiftEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShiftEvaluation::getShiftId, shiftId);
        wrapper.orderByAsc(ShiftEvaluation::getUserId);
        wrapper.orderByAsc(ShiftEvaluation::getSkillId);
        return this.list(wrapper);
    }

    @Override
    public List<ShiftEvaluation> getEvaluationsByUser(Long userId) {
        LambdaQueryWrapper<ShiftEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShiftEvaluation::getUserId, userId);
        wrapper.orderByDesc(ShiftEvaluation::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public boolean addEvaluation(ShiftEvaluation evaluation, Long evaluatorId) {
        User evaluator = userMapper.selectById(evaluatorId);
        if (evaluator == null || !"supervisor".equals(evaluator.getRole())) {
            throw new RuntimeException("只有主管才能进行班后评价");
        }

        if (evaluation.getUserId().equals(evaluatorId)) {
            throw new RuntimeException("不能评价自己");
        }

        evaluation.setEvaluatorId(evaluatorId);
        evaluation.setCreateTime(LocalDateTime.now());
        evaluation.setUpdateTime(LocalDateTime.now());

        boolean saved = this.save(evaluation);
        if (!saved) return false;

        if (evaluation.getProficiencyChange() != null && evaluation.getProficiencyChange() != 0) {
            updateProficiency(evaluation.getUserId(), evaluation.getSkillId(), evaluation.getProficiencyChange());
        }

        return true;
    }

    @Override
    @Transactional
    public boolean batchEvaluate(Long shiftId, Long evaluatorId, List<ShiftEvaluation> evaluations) {
        User evaluator = userMapper.selectById(evaluatorId);
        if (evaluator == null || !"supervisor".equals(evaluator.getRole())) {
            throw new RuntimeException("只有主管才能进行班后评价");
        }

        for (ShiftEvaluation evaluation : evaluations) {
            if (evaluation.getUserId().equals(evaluatorId)) {
                continue;
            }
            evaluation.setShiftId(shiftId);
            evaluation.setEvaluatorId(evaluatorId);
            evaluation.setCreateTime(LocalDateTime.now());
            evaluation.setUpdateTime(LocalDateTime.now());
            this.save(evaluation);

            if (evaluation.getProficiencyChange() != null && evaluation.getProficiencyChange() != 0) {
                updateProficiency(evaluation.getUserId(), evaluation.getSkillId(), evaluation.getProficiencyChange());
            }
        }
        return true;
    }

    private void updateProficiency(Long userId, Long skillId, int change) {
        LambdaQueryWrapper<UserSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSkill::getUserId, userId);
        wrapper.eq(UserSkill::getSkillId, skillId);
        UserSkill userSkill = userSkillMapper.selectOne(wrapper);

        if (userSkill != null) {
            int newProficiency = userSkill.getProficiency() + change;
            if (newProficiency < 1) newProficiency = 1;
            if (newProficiency > 5) newProficiency = 5;
            userSkill.setProficiency(newProficiency);
            userSkill.setUpdateTime(LocalDateTime.now());
            userSkillMapper.updateById(userSkill);
        } else if (change > 0) {
            UserSkill newUserSkill = new UserSkill();
            newUserSkill.setUserId(userId);
            newUserSkill.setSkillId(skillId);
            newUserSkill.setProficiency(Math.min(change, 5));
            newUserSkill.setCertified(0);
            newUserSkill.setCreateTime(LocalDateTime.now());
            newUserSkill.setUpdateTime(LocalDateTime.now());
            userSkillMapper.insert(newUserSkill);
        }
    }
}
