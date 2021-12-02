-- 0-6岁系统
-- 医生信息表
ALTER TABLE `h_doctor`
DROP COLUMN `gender`,
DROP COLUMN `status`;

-- 筛查机构表
ALTER TABLE `m_screening_organization`
ADD COLUMN `cooperation_type` tinyint(4) NULL DEFAULT NULL COMMENT '合作类型 0-合作 1-试用' AFTER `status`,
ADD COLUMN `cooperation_time_type` tinyint(4) NULL DEFAULT NULL COMMENT '合作期限类型 -1-自定义 0-30天 1-60天 2-180天 3-1年 4-2年 5-3年' AFTER `cooperation_type`,
ADD COLUMN `cooperation_start_time` timestamp(0) NULL DEFAULT NULL COMMENT '合作开始时间' AFTER `cooperation_time_type`,
ADD COLUMN `cooperation_end_time` timestamp(0) NULL DEFAULT NULL COMMENT '合作结束时间' AFTER `cooperation_start_time`;

-- 学校表
ALTER TABLE `m_school`
ADD COLUMN `cooperation_type` tinyint(4) NULL DEFAULT NULL COMMENT '合作类型 0-合作 1-试用' AFTER `status`,
ADD COLUMN `cooperation_time_type` tinyint(4) NULL DEFAULT NULL COMMENT '合作期限类型 -1-自定义 0-30天 1-60天 2-180天 3-1年 4-2年 5-3年' AFTER `cooperation_type`,
ADD COLUMN `cooperation_start_time` timestamp(0) NULL DEFAULT NULL COMMENT '合作开始时间' AFTER `cooperation_time_type`,
ADD COLUMN `cooperation_end_time` timestamp(0) NULL DEFAULT NULL COMMENT '合作结束时间' AFTER `cooperation_start_time`;