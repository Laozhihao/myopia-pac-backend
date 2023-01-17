-- 表 m_vision_screening_result 调整组合索引字段顺序
ALTER TABLE `m_vision_screening_result`
DROP INDEX `m_school_id_screening_org_id_plan_id_is_double_screen_index`,
DROP INDEX `m_vision_screening_result_is_double_screen_student_id_index`,
ADD INDEX `m_school_id_screening_org_id_plan_id_is_double_screen_index`(`plan_id`, `school_id`, `is_double_screen`) USING BTREE,
ADD INDEX `m_vision_screening_result_is_double_screen_student_id_index`(`student_id`, `is_double_screen`) USING BTREE;
-- 表 m_vision_screening_result 增加新组合索引
ALTER TABLE `m_vision_screening_result`
    ADD INDEX `m_plan_student_id_is_doubule_screen_index`(`screening_plan_school_student_id`, `is_double_screen`) USING BTREE;

-- 表 m_stat_conclusion 加索引
ALTER TABLE `m_stat_conclusion`
    ADD INDEX `m_vsr_plan_id_school_id_index`(`plan_id`, `school_id`) USING BTREE;

-- 表 m_student 加索引
ALTER TABLE `m_student`
    ADD INDEX `m_student_school_id_status_index`(`school_id`, `status`) USING BTREE;

-- 表 m_vision_screening_result 加索引
ALTER TABLE `m_vision_screening_result`
    ADD INDEX `m_vsr_student_id_school_id_index`(`student_id`, `school_id`);

-- 表 m_screening_plan_school_student 加索引、移除多余索引
ALTER TABLE `m_screening_plan_school_student`
    DROP INDEX `m_screening_plan_school_student_screening_plan_id_index`,
    ADD INDEX `m_spss_student_id_index`(`student_id`) USING BTREE;

-- 表 m_screening_plan_school_student 加索引
ALTER TABLE `m_screening_plan_school_student`
    ADD INDEX `m_spss_plan_id_class_id`(`screening_plan_id`, `class_id`) USING BTREE;

-- 表 m_stat_conclusion 优化索引顺序
ALTER TABLE `m_stat_conclusion`
    DROP INDEX `district_INDEX`,
    ADD INDEX `m_sc_create_time_rescreen_valid_cooperative_district_index`(`create_time`, `is_valid`, `is_rescreen`, `is_cooperative`, `warning_level`, `district_id`) USING BTREE;

-- 表 m_school_student 加索引
ALTER TABLE `m_school_student`
    ADD INDEX `m_school_id_student_id_index`(`school_id`, `student_id`) USING BTREE;