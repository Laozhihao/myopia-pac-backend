DROP TABLE IF EXISTS `m_stat_conclusion`;

CREATE TABLE `m_stat_conclusion` (
    `id` int NOT NULL AUTO_INCREMENT,
    `result_id` int NOT NULL COMMENT '源筛查数据id',
    `src_screening_notice_id` int NOT NULL COMMENT '通知id',
    `task_id` int NOT NULL COMMENT '任务id',
    `plan_id` int NOT NULL COMMENT '计划ID',
    `district_id` int NOT NULL COMMENT '所属地区id',
    `school_age` int NOT NULL COMMENT '学龄',
    `gender` int NOT NULL COMMENT '性别',
    `warning_level` int DEFAULT NULL COMMENT '预警级别',
    `vision_l` int DEFAULT NULL COMMENT '左眼视力',
    `vision_r` int DEFAULT NULL COMMENT '右眼视力',
    `is_low_vision` int DEFAULT NULL COMMENT '是否视力低下',
    `is_refractive_error` tinyint(1) DEFAULT NULL COMMENT '是否屈光不正',
    `is_myopia` tinyint(1) DEFAULT NULL COMMENT '是否近视',
    `is_hyperopia` tinyint(1) DEFAULT NULL COMMENT '是否远视',
    `is_astigmatism` tinyint(1) DEFAULT NULL COMMENT '是否散光',
    `is_wearing_glasses` tinyint(1) DEFAULT NULL COMMENT '是否戴镜',
    `is_recommend_visit` tinyint(1) DEFAULT NULL COMMENT '是否建议就诊',
    `is_rescreen` tinyint(1) NOT NULL COMMENT '是否复测',
    `rescreen_error_num` int NOT NULL COMMENT '复测错误项次',
    `is_valid` tinyint(1) NOT NULL COMMENT '是否有效数据',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT = '筛查数据结论';