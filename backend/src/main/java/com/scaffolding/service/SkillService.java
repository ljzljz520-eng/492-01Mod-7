package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.Skill;
import java.util.List;

public interface SkillService extends IService<Skill> {
    List<Skill> listAll();
}
