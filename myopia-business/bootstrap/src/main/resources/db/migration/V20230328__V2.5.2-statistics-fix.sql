-- 调整m_screening_result_statistic唯一索引字段
ALTER TABLE `m_screening_result_statistic` DROP INDEX `screening_result_statistic_unique`,
ADD UNIQUE INDEX `screening_result_statistic_unique` ( `screening_notice_id`, `screening_plan_id`, `school_id`, `school_type`, `district_id`, `is_total` ) USING BTREE;