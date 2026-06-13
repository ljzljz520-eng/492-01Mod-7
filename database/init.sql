SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `scaffolding_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `scaffolding_db`;

-- 文件信息表
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT '0' COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `file_extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `upload_user_id` bigint(20) DEFAULT NULL COMMENT '上传人ID',
  `upload_user_name` varchar(50) DEFAULT NULL COMMENT '上传人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

-- 工作管理表
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_name` varchar(100) NOT NULL COMMENT '工作名称',
  `work_content` text COMMENT '工作内容',
  `work_status` varchar(20) DEFAULT 'pending' COMMENT '工作状态（pending-待处理，in_progress-进行中，completed-已完成，cancelled-已取消）',
  `work_time` datetime DEFAULT NULL COMMENT '工作时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `priority` varchar(20) DEFAULT 'normal' COMMENT '优先级（low-低，normal-普通，high-高，urgent-紧急）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_work_status` (`work_status`),
  KEY `idx_work_time` (`work_time`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作管理表';

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（账号）',
  `password` varchar(100) NOT NULL COMMENT '密码（不加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `role` varchar(20) DEFAULT 'worker' COMMENT '角色（supervisor-主管，worker-工人）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认账号
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES ('admin', '123456', '管理员', 'supervisor');
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES ('worker1', '123456', '张三', 'worker');
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES ('worker2', '123456', '李四', 'worker');
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES ('worker3', '123456', '王五', 'worker');
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES ('worker4', '123456', '赵六', 'worker');
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES ('worker5', '123456', '钱七', 'worker');

-- 技能定义表
DROP TABLE IF EXISTS `skill`;
CREATE TABLE `skill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `skill_code` varchar(50) NOT NULL COMMENT '技能编码',
  `skill_name` varchar(50) NOT NULL COMMENT '技能名称',
  `skill_type` varchar(20) DEFAULT 'normal' COMMENT '技能类型（key-关键技能，normal-普通技能）',
  `description` varchar(255) DEFAULT NULL COMMENT '技能描述',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_skill_code` (`skill_code`),
  KEY `idx_skill_type` (`skill_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能定义表';

-- 插入初始技能数据
INSERT INTO `skill` (`skill_code`, `skill_name`, `skill_type`, `description`, `sort_order`) VALUES 
('forklift', '叉车', 'key', '叉车操作技能', 1),
('packing', '打包', 'normal', '产品打包技能', 2),
('qc', '质检', 'key', '质量检验技能', 3),
('welding', '焊接', 'key', '焊接作业技能', 4),
('night_lead', '夜班带队', 'key', '夜班带队管理能力', 5);

-- 用户技能关联表
DROP TABLE IF EXISTS `user_skill`;
CREATE TABLE `user_skill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `skill_id` bigint(20) NOT NULL COMMENT '技能ID',
  `proficiency` int(11) DEFAULT '1' COMMENT '熟练度（1-入门，2-初级，3-中级，4-高级，5-精通）',
  `certified` tinyint(1) DEFAULT '0' COMMENT '是否持证（0-否，1-是）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_skill` (`user_id`, `skill_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_skill_id` (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户技能关联表';

-- 插入示例用户技能数据
INSERT INTO `user_skill` (`user_id`, `skill_id`, `proficiency`, `certified`) VALUES 
(2, 1, 4, 1),
(2, 2, 3, 0),
(3, 2, 5, 0),
(3, 3, 2, 0),
(4, 3, 4, 1),
(4, 4, 3, 1),
(5, 4, 5, 1),
(5, 5, 4, 0),
(6, 1, 2, 0),
(6, 5, 3, 0);

-- 排班表
DROP TABLE IF EXISTS `shift`;
CREATE TABLE `shift` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shift_name` varchar(100) NOT NULL COMMENT '班次名称',
  `shift_date` date NOT NULL COMMENT '排班日期',
  `shift_type` varchar(20) DEFAULT 'day' COMMENT '班次类型（day-白班，night-夜班）',
  `start_time` time DEFAULT NULL COMMENT '开始时间',
  `end_time` time DEFAULT NULL COMMENT '结束时间',
  `supervisor_id` bigint(20) DEFAULT NULL COMMENT '主管ID',
  `status` varchar(20) DEFAULT 'draft' COMMENT '状态（draft-草稿，published-已发布，completed-已完成）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_shift_date` (`shift_date`),
  KEY `idx_status` (`status`),
  KEY `idx_supervisor_id` (`supervisor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班表';

-- 排班成员表
DROP TABLE IF EXISTS `shift_member`;
CREATE TABLE `shift_member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shift_id` bigint(20) NOT NULL COMMENT '排班ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `position` varchar(50) DEFAULT NULL COMMENT '岗位',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shift_user` (`shift_id`, `user_id`),
  KEY `idx_shift_id` (`shift_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班成员表';

-- 班后评价表
DROP TABLE IF EXISTS `shift_evaluation`;
CREATE TABLE `shift_evaluation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shift_id` bigint(20) NOT NULL COMMENT '排班ID',
  `user_id` bigint(20) NOT NULL COMMENT '被评价人ID',
  `evaluator_id` bigint(20) NOT NULL COMMENT '评价人ID（主管）',
  `skill_id` bigint(20) NOT NULL COMMENT '技能ID',
  `proficiency_change` int(11) DEFAULT '0' COMMENT '熟练度变化（-1, 0, +1）',
  `score` int(11) DEFAULT NULL COMMENT '评分（1-5）',
  `comment` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_shift_id` (`shift_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_evaluator_id` (`evaluator_id`),
  KEY `idx_skill_id` (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班后评价表';

SET FOREIGN_KEY_CHECKS = 1;
