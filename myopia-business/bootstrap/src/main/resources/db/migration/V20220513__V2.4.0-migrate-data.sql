-- 新增字段，山西迁移学生的筛查数据ID
ALTER TABLE `m_screening_plan_school_student`
  ADD COLUMN `migrate_student_screening_id` varchar(50) DEFAULT NULL COMMENT '山西迁移学生的筛查数据ID';

-- 质控员和检测队长改为非必填
ALTER TABLE `m_screening_plan_school`
  MODIFY COLUMN `quality_controller_name` varchar(25) DEFAULT NULL COLLATE utf8mb4_general_ci COMMENT '机构质控员名字',
  MODIFY COLUMN `quality_controller_commander` varchar(25) DEFAULT NULL COLLATE utf8mb4_general_ci COMMENT '机构质控员队长';