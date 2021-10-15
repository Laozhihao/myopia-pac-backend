alter table m_school_vision_statistic
    add myopia_level_light int null comment '轻度近视人数' after focus_targets_numbers;

alter table m_school_vision_statistic
    add myopia_level_middle int null comment '中度近视人数' after myopia_level_light;

alter table m_school_vision_statistic
    add myopia_level_high int null comment '高度近视人数' after myopia_level_middle;

alter table m_school_vision_statistic
    add myopia_level_insufficient int null comment '远视储备不足人数' after myopia_level_high;

alter table m_stat_conclusion
    add myopia_level int null comment '近视预警等级';

-- 更新学校表
UPDATE m_school SET district_detail = '[]' WHERE school_no = '1234567890';
ALTER TABLE `m_school`
  MODIFY COLUMN `district_detail` json NOT NULL COMMENT '行政区域json',
  ADD COLUMN `district_area_code` bigint(20) DEFAULT NULL COMMENT '行政区域-区/县code（含省市）',
  ADD COLUMN `area_type` tinyint(1) COMMENT '片区：1好片、2中片、3差片',
  ADD COLUMN `monitor_type` tinyint(1) COMMENT '监测点：1城区、2郊县',
  ADD INDEX `m_school_district_area_code_area_type_monitor_type_index`(`district_area_code`, `area_type`, `monitor_type`) USING BTREE;

-- 更新学生表
ALTER TABLE .`m_student`
  ADD COLUMN `school_id` int(11) COMMENT '学校ID';
-- 处理学生表历史数据
UPDATE m_student a, m_school b SET a.school_id = b.id WHERE a.school_no = b.school_no;