package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.dto.SkillRiskDTO;
import com.scaffolding.dto.UserSkillDTO;
import com.scaffolding.entity.UserSkill;
import com.scaffolding.service.UserSkillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user-skill")
@Api(tags = "用户技能管理")
public class UserSkillController {

    @Autowired
    private UserSkillService userSkillService;

    @GetMapping("/matrix")
    @ApiOperation("获取技能矩阵")
    public Result<List<UserSkillDTO>> getSkillMatrix(
            @RequestParam(required = false) String nickname) {
        try {
            List<UserSkillDTO> matrix = userSkillService.getSkillMatrix(nickname);
            return Result.success(matrix);
        } catch (Exception e) {
            log.error("获取技能矩阵失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/matrix/page")
    @ApiOperation("分页获取技能矩阵")
    public Result<PageResult<UserSkillDTO>> getSkillMatrixPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String nickname) {
        try {
            Page<UserSkillDTO> page = userSkillService.getSkillMatrixPage(current, size, nickname);
            PageResult<UserSkillDTO> pageResult = new PageResult<>(
                    page.getTotal(),
                    page.getRecords(),
                    page.getCurrent(),
                    page.getSize()
            );
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取技能矩阵失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/risk-warning")
    @ApiOperation("获取技能风险预警")
    public Result<List<SkillRiskDTO>> getSkillRiskWarning() {
        try {
            List<SkillRiskDTO> risks = userSkillService.getSkillRiskWarning();
            return Result.success(risks);
        } catch (Exception e) {
            log.error("获取技能风险预警失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/recommend")
    @ApiOperation("推荐符合技能要求的工人")
    public Result<List<UserSkillDTO>> recommendWorkers(
            @RequestParam List<Long> skillIds,
            @RequestParam(required = false) Integer minProficiency,
            @RequestParam(required = false) Integer limit) {
        try {
            List<UserSkillDTO> workers = userSkillService.recommendWorkers(skillIds, minProficiency, limit);
            return Result.success(workers);
        } catch (Exception e) {
            log.error("推荐工人失败", e);
            return Result.error("推荐失败：" + e.getMessage());
        }
    }

    @PostMapping
    @ApiOperation("新增/更新用户技能")
    public Result<?> saveUserSkill(
            @RequestParam Long userId,
            @RequestParam Long skillId,
            @RequestParam Integer proficiency,
            @RequestParam(defaultValue = "0") Integer certified,
            @RequestParam(required = false) Long operatorId) {
        try {
            boolean result = userSkillService.saveUserSkill(userId, skillId, proficiency, certified, operatorId);
            if (result) {
                return Result.success("操作成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error("保存用户技能失败", e);
            return Result.error("保存失败：" + e.getMessage());
        }
    }
}
