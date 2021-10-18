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