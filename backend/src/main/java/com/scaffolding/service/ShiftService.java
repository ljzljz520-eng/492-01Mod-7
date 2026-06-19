package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.dto.PositionRequirementDTO;
import com.scaffolding.dto.ShiftDetailDTO;
import com.scaffolding.dto.ShiftMemberDTO;
import com.scaffolding.dto.ShiftRecommendResultDTO;
import com.scaffolding.entity.Shift;
import java.time.LocalDate;
import java.util.List;

public interface ShiftService extends IService<Shift> {
    Page<Shift> pageQuery(Long current, Long size, String shiftName, String status, LocalDate startDate, LocalDate endDate);

    ShiftDetailDTO getShiftDetail(Long id);

    boolean createShift(Shift shift, List<Long> userIds, Long supervisorId);

    boolean updateShiftMembers(Long shiftId, List<Long> userIds, Long supervisorId);

    boolean publishShift(Long id, Long supervisorId);

    boolean completeShift(Long id, Long supervisorId);

    List<ShiftMemberDTO> recommendShiftMembers(Long shiftId, List<Long> requiredSkillIds, Integer memberCount);

    List<ShiftRecommendResultDTO> recommendShiftTeam(Long shiftId, List<PositionRequirementDTO> requirements);
}
