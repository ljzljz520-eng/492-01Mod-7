package com.scaffolding.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.dto.PositionRequirementDTO;
import com.scaffolding.dto.ShiftDetailDTO;
import com.scaffolding.dto.ShiftMemberDTO;
import com.scaffolding.dto.ShiftRecommendResultDTO;
import com.scaffolding.entity.Shift;
import com.scaffolding.service.ShiftService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/shift")
@Api(tags = "排班管理")
public class ShiftController {

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/page")
    @ApiOperation("分页查询排班")
    public Result<PageResult<Shift>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String shiftName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            Page<Shift> page = shiftService.pageQuery(current, size, shiftName, status, startDate, endDate);
            PageResult<Shift> pageResult = new PageResult<>(
                    page.getTotal(),
                    page.getRecords(),
                    page.getCurrent(),
                    page.getSize()
            );
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("查询排班失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("获取排班详情")
    public Result<ShiftDetailDTO> getDetail(@PathVariable Long id) {
        try {
            ShiftDetailDTO detail = shiftService.getShiftDetail(id);
            if (detail == null) {
                return Result.error("排班不存在");
            }
            return Result.success(detail);
        } catch (Exception e) {
            log.error("获取排班详情失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @PostMapping
    @ApiOperation("创建排班")
    public Result<Shift> create(@RequestBody Map<String, Object> params) {
        try {
            Shift shift = new Shift();
            shift.setShiftName((String) params.get("shiftName"));
            if (params.get("shiftDate") != null) {
                shift.setShiftDate(LocalDate.parse((String) params.get("shiftDate")));
            }
            shift.setShiftType((String) params.get("shiftType"));
            if (params.get("startTime") != null) {
                shift.setStartTime(java.time.LocalTime.parse((String) params.get("startTime")));
            }
            if (params.get("endTime") != null) {
                shift.setEndTime(java.time.LocalTime.parse((String) params.get("endTime")));
            }
            if (params.get("supervisorId") != null) {
                shift.setSupervisorId(Long.valueOf(params.get("supervisorId").toString()));
            }
            shift.setRemark((String) params.get("remark"));

            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) params.get("userIds");

            Long supervisorId = params.get("supervisorId") != null ?
                    Long.valueOf(params.get("supervisorId").toString()) : null;

            boolean result = shiftService.createShift(shift, userIds, supervisorId);
            if (result) {
                return Result.success("创建成功", shift);
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建排班失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/members")
    @ApiOperation("更新排班成员")
    public Result<?> updateMembers(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) params.get("userIds");
            Long supervisorId = params.get("supervisorId") != null ?
                    Long.valueOf(params.get("supervisorId").toString()) : null;

            boolean result = shiftService.updateShiftMembers(id, userIds, supervisorId);
            if (result) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新排班成员失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/publish")
    @ApiOperation("发布排班")
    public Result<?> publish(
            @PathVariable Long id,
            @RequestParam(required = false) Long supervisorId) {
        try {
            boolean result = shiftService.publishShift(id, supervisorId);
            if (result) {
                return Result.success("发布成功");
            } else {
                return Result.error("发布失败");
            }
        } catch (Exception e) {
            log.error("发布排班失败", e);
            return Result.error("发布失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/complete")
    @ApiOperation("完成排班")
    public Result<?> complete(
            @PathVariable Long id,
            @RequestParam(required = false) Long supervisorId) {
        try {
            boolean result = shiftService.completeShift(id, supervisorId);
            if (result) {
                return Result.success("完成成功");
            } else {
                return Result.error("完成失败");
            }
        } catch (Exception e) {
            log.error("完成排班失败", e);
            return Result.error("完成失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}/recommend-members")
    @ApiOperation("推荐排班成员")
    public Result<List<ShiftMemberDTO>> recommendMembers(
            @PathVariable Long id,
            @RequestParam List<Long> skillIds,
            @RequestParam(required = false) Integer memberCount) {
        try {
            List<ShiftMemberDTO> members = shiftService.recommendShiftMembers(id, skillIds, memberCount);
            return Result.success(members);
        } catch (Exception e) {
            log.error("推荐排班成员失败", e);
            return Result.error("推荐失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除排班")
    public Result<?> delete(@PathVariable Long id) {
        try {
            boolean result = shiftService.removeById(id);
            if (result) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除排班失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/recommend-team")
    @ApiOperation("按岗位推荐排班组合")
    public Result<List<ShiftRecommendResultDTO>> recommendTeam(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        try {
            Object requirementsObj = params.get("requirements");
            List<PositionRequirementDTO> requirements = new ArrayList<>();
            if (requirementsObj != null) {
                requirements = objectMapper.convertValue(requirementsObj,
                    new TypeReference<List<PositionRequirementDTO>>() {});
            }

            List<ShiftRecommendResultDTO> result = shiftService.recommendShiftTeam(id, requirements);
            return Result.success(result);
        } catch (Exception e) {
            log.error("推荐排班组合失败", e);
            return Result.error("推荐失败：" + e.getMessage());
        }
    }
}
