package com.scaffolding.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaffolding.common.Result;
import com.scaffolding.entity.ShiftEvaluation;
import com.scaffolding.service.ShiftEvaluationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/shift-evaluation")
@Api(tags = "班后评价")
public class ShiftEvaluationController {

    @Autowired
    private ShiftEvaluationService shiftEvaluationService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/shift/{shiftId}")
    @ApiOperation("获取排班的所有评价")
    public Result<List<ShiftEvaluation>> getByShift(@PathVariable Long shiftId) {
        try {
            List<ShiftEvaluation> evaluations = shiftEvaluationService.getEvaluationsByShift(shiftId);
            return Result.success(evaluations);
        } catch (Exception e) {
            log.error("获取评价失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    @ApiOperation("获取用户的所有评价")
    public Result<List<ShiftEvaluation>> getByUser(@PathVariable Long userId) {
        try {
            List<ShiftEvaluation> evaluations = shiftEvaluationService.getEvaluationsByUser(userId);
            return Result.success(evaluations);
        } catch (Exception e) {
            log.error("获取评价失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @PostMapping
    @ApiOperation("新增评价")
    public Result<?> addEvaluation(
            @RequestBody ShiftEvaluation evaluation,
            @RequestParam(required = false) Long evaluatorId) {
        try {
            boolean result = shiftEvaluationService.addEvaluation(evaluation, evaluatorId);
            if (result) {
                return Result.success("评价成功");
            } else {
                return Result.error("评价失败");
            }
        } catch (Exception e) {
            log.error("评价失败", e);
            return Result.error("评价失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch/{shiftId}")
    @ApiOperation("批量评价")
    public Result<?> batchEvaluate(
            @PathVariable Long shiftId,
            @RequestBody Map<String, Object> params) {
        try {
            Object evaluationsObj = params.get("evaluations");
            List<ShiftEvaluation> evaluations = new ArrayList<>();
            if (evaluationsObj != null) {
                evaluations = objectMapper.convertValue(evaluationsObj, 
                    new TypeReference<List<ShiftEvaluation>>() {});
            }
            Long evaluatorId = params.get("evaluatorId") != null ?
                    Long.valueOf(params.get("evaluatorId").toString()) : null;

            boolean result = shiftEvaluationService.batchEvaluate(shiftId, evaluatorId, evaluations);
            if (result) {
                return Result.success("评价成功");
            } else {
                return Result.error("评价失败");
            }
        } catch (Exception e) {
            log.error("批量评价失败", e);
            return Result.error("评价失败：" + e.getMessage());
        }
    }
}
