package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_skill")
public class UserSkill extends BaseEntity {

    private Long userId;

    private Long skillId;

    private Integer proficiency;

    private Integer certified;
}
