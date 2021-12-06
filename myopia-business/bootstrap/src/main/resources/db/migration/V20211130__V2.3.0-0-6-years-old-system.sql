-- 表m_hospital新增字段
ALTER TABLE `m_hospital`
  ADD COLUMN `service_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '服务类型（配置），0：居民健康系统(默认)、1：0-6岁眼保健、2：0-6岁眼保健+居民健康系统',
  ADD COLUMN `cooperation_type` tinyint(1) COMMENT '合作类型，0-合作 1-试用',
  ADD COLUMN `cooperation_time_type` tinyint(1) COMMENT '合作期限类型，-1-自定义 0-30天 1-60天 2-180天 3-1年 4-2年 5-3年',
  ADD COLUMN `cooperation_start_time` timestamp(0) NULL DEFAULT NULL COMMENT '合作开始时间',
  ADD COLUMN `cooperation_end_time` timestamp(0) NULL DEFAULT NULL COMMENT '合作结束时间',
  ADD COLUMN `associate_screening_org_id` int(11) COMMENT '关联筛查机构的ID';