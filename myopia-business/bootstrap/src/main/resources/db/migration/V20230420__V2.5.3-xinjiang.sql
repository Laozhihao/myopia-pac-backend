ALTER TABLE `m_screening_plan`
    ADD COLUMN `year` int(4) NULL COMMENT '筛查年份',
    ADD COLUMN `time` tinyint(1) NULL COMMENT '筛查次数';