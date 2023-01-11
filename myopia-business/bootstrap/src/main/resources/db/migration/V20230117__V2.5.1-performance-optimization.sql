-- 调整组合索引字段顺序
ALTER TABLE `m_vision_screening_result`
DROP INDEX `m_school_id_screening_org_id_plan_id_is_double_screen_index`,
DROP INDEX `m_vision_screening_result_is_double_screen_student_id_index`,
ADD INDEX `m_school_id_screening_org_id_plan_id_is_double_screen_index`(`plan_id`, `school_id`, `is_double_screen`) USING BTREE,
ADD INDEX `m_vision_screening_result_is_double_screen_student_id_index`(`student_id`, `is_double_screen`) USING BTREE;
-- 增加新组合索引
ALTER TABLE `m_vision_screening_result`
    ADD INDEX `m_plan_student_id_is_doubule_screen_index`(`screening_plan_school_student_id`, `is_double_screen`) USING BTREE;