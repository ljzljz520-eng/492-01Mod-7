package com.scaffolding.controller;

import com.scaffolding.common.Result;
import com.scaffolding.entity.Skill;
import com.scaffolding.service.SkillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/skill")
@Api(tags = "技能管理")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @GetMapping("/list")
    @ApiOperation("获取所有技能列表")
    public Result<List<Skill>> list() {
        try {
            List<Skill> skills = skillService.listAll();
            return Result.success(skills);
        } catch (Exception e) {
            log.error("获取技能列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询技能")
    public Result<Skill> getById(@PathVariable Long id) {
        Skill skill = skillService.getById(id);
        if (skill == null) {
            return Result.error("技能不存在");
        }
        return Result.success(skill);
    }
}
