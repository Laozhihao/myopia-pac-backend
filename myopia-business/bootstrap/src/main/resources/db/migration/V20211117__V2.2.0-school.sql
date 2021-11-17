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

alter table m_district_vision_statistic
    add myopia_level_early_num int null comment '近视前期人数' after valid_screening_numbers;

alter table m_school_vision_statistic alter column myopia_level_light set default 0;

alter table m_school_vision_statistic alter column myopia_level_early set default 0;

alter table m_school_vision_statistic alter column myopia_level_middle set default 0;

alter table m_school_vision_statistic alter column myopia_level_high set default 0;

alter table m_school_vision_statistic alter column myopia_level_insufficient set default 0;

alter table m_district_vision_statistic alter column myopia_level_early_num set default 0;

