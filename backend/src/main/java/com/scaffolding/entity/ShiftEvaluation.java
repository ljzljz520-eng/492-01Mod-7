package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shift_evaluation")
public class ShiftEvaluation extends BaseEntity {

    private Long shiftId;

    private Long userId;

    private Long evaluatorId;

    private Long skillId;

    private Integer proficiencyChange;

    private Integer score;

    private String comment;
}
