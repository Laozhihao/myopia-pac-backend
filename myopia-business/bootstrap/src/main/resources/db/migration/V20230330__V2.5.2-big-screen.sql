-- 更新m_district_big_screen_statistic表的更新时间字段值为跟随数据的变动而更新
ALTER TABLE `m_district_big_screen_statistic` MODIFY COLUMN `update_time` TIMESTAMP ( 0 ) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ( 0 ) COMMENT '大屏展示--更新时间';

-- 增加表m_school_student的class_name字段长大
ALTER TABLE `m_school_student` MODIFY COLUMN `class_name` varchar(32) NULL DEFAULT NULL COMMENT '班级名称';