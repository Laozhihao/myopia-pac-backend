-- sql 加上注释说明描述
--- app筛查端原始数据
ALTER TABLE m_vision_screening_result ADD saprodontia_data json NULL COMMENT '龋齿检查';
ALTER TABLE m_vision_screening_result ADD spine_data json NULL COMMENT '脊柱检查';
ALTER TABLE m_vision_screening_result ADD blood_pressure_data json NULL COMMENT '血压检查';
ALTER TABLE m_vision_screening_result ADD diseases_history_data  json NULL COMMENT '疾病史';
ALTER TABLE m_vision_screening_result ADD privacy_data json NULL COMMENT '个人隐私';
ALTER TABLE m_vision_screening_result ADD deviation_data json NULL COMMENT '筛查不准确说明';
ALTER TABLE m_screening_plan_school_student ADD state tinyint(1) NOT NULL default 0 COMMENT '未做检查说明【0:无；1：请假；2：转学;3:其他】';
ALTER TABLE m_vision_screening_result ADD screening_type tinyint(1) NOT NULL default 0 COMMENT '筛查类型--来自筛查计划，筛查计划强一致 （0：视力筛查，1；常见病）';
ALTER TABLE m_screening_plan ADD screening_type tinyint(1) NOT NULL default 0 COMMENT '筛查类型（0：视力筛查，1；常见病）';