package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shift_member")
public class ShiftMember extends BaseEntity {

    private Long shiftId;

    private Long userId;

    private String position;

    private Integer sortOrder;
}
