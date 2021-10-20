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

alter table m_school_vision_statistic
    add myopia_level_early int null comment '近视前期' after myopia_level_light;

alter table m_stat_conclusion
    add hyperopia_level int null comment '远视等级';

alter table m_stat_conclusion
    add astigmatism_level int null comment '散光等级';
-- m_student表新增视力情况等级相关字段
ALTER TABLE `m_student`
  ADD COLUMN `myopia_level` tinyint(1) COMMENT '近视等级：0-正常、1-筛查性近视、2-近视前期、3-低度近视、4-中度近视、5-重度近视',
  ADD COLUMN `hyperopia_level` tinyint(1) COMMENT '远视等级：0-正常、1-远视、2-低度远视、3-中度远视、4-重度远视',
  ADD COLUMN `astigmatism_level` tinyint(1) COMMENT '散光等级：0-正常、1-低度散光、2-中度散光、3-重度散光';

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

alter table m_district_vision_statistic
    add myopia_level_early_num int null comment '近视前期人数' after valid_screening_numbers;