-- sql 加上注释说明描述
-- app筛查端原始数据
ALTER TABLE m_vision_screening_result ADD saprodontia_data json NULL COMMENT '龋齿检查';
ALTER TABLE m_vision_screening_result ADD spine_data json NULL COMMENT '脊柱检查';
ALTER TABLE m_vision_screening_result ADD blood_pressure_data json NULL COMMENT '血压检查';
ALTER TABLE m_vision_screening_result ADD diseases_history_data  json NULL COMMENT '疾病史';
ALTER TABLE m_vision_screening_result ADD privacy_data json NULL COMMENT '个人隐私';
ALTER TABLE m_screening_plan_school_student ADD state tinyint(1) NOT NULL default 0 COMMENT '未做检查说明【0:无；1：请假；2：转学;3:其他】';
ALTER TABLE m_vision_screening_result ADD screening_type tinyint(1) NOT NULL default 0 COMMENT '筛查类型--来自筛查计划，筛查计划强一致 （0：视力筛查，1；常见病）';
ALTER TABLE m_screening_plan ADD screening_type tinyint(1) NOT NULL default 0 COMMENT '筛查类型（0：视力筛查，1；常见病）';
-- 筛查计划表添加筛查类型字段update_screening_end_time_status
ALTER TABLE m_screening_plan ADD update_screening_end_time_status tinyint(1) NOT NULL default 0 COMMENT '修改筛查结束时间状态（0：未修改，1；已修改）';
-- 筛查通知表添加筛查类型字段screening_type
ALTER TABLE m_screening_notice ADD screening_type tinyint(1) NOT NULL default 0 COMMENT '筛查类型（0：视力筛查，1；常见病）';
-- 筛查任务表添加筛查类型字段screening_type
ALTER TABLE m_screening_task ADD screening_type tinyint(1) NOT NULL default 0 COMMENT '筛查类型（0：视力筛查，1；常见病）';
-- 筛查计划关联的学校表字段quality_controller_name必填改为非必填
alter table m_screening_plan_school modify quality_controller_name varchar(25) DEFAULT NULL COMMENT '机构质控员名字';
-- 筛查计划关联的学校表字段quality_controller_commander必填改为非必填
alter table m_screening_plan_school modify quality_controller_commander varchar(25) DEFAULT NULL COMMENT '机构质控员队长';