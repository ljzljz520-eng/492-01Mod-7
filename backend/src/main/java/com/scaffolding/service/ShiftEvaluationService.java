package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.ShiftEvaluation;
import java.util.List;

public interface ShiftEvaluationService extends IService<ShiftEvaluation> {
    List<ShiftEvaluation> getEvaluationsByShift(Long shiftId);

    List<ShiftEvaluation> getEvaluationsByUser(Long userId);

    boolean addEvaluation(ShiftEvaluation evaluation, Long evaluatorId);

    boolean batchEvaluate(Long shiftId, Long evaluatorId, List<ShiftEvaluation> evaluations);
}
