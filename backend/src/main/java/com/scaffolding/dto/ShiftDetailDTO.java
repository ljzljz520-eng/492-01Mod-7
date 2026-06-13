package com.scaffolding.dto;

import lombok.Data;
import java.util.List;

@Data
public class ShiftDetailDTO {
    private Long shiftId;
    private String shiftName;
    private String shiftDate;
    private String shiftType;
    private String startTime;
    private String endTime;
    private Long supervisorId;
    private String supervisorName;
    private String status;
    private String remark;
    private List<ShiftMemberDTO> members;
}
