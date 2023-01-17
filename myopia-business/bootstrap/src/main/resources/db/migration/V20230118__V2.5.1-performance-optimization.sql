-- 表 m_school_student 加索引
ALTER TABLE `m_school_student`
    ADD INDEX `m_school_id_student_id_index`(`school_id`, `student_id`) USING BTREE;