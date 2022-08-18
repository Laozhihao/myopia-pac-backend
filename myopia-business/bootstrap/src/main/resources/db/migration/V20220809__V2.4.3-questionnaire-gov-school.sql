alter table q_questionnaire_question
    add is_hidden boolean null comment '是否隐藏';

alter table q_questionnaire_question
    add qes_data json null comment 'qes字段序号';

alter table q_user_question_record
    add record_type int null comment '汇总类型 1-汇总' after status;

alter table q_question
    add mapping_key varchar(32) null comment '前端映射key' after icon_name;

alter table q_user_answer
    add table_json json null comment '表格JSON' after answer;

alter table q_user_answer
    add type varchar(32) null comment '类型' after table_json;

alter table q_user_answer
    modify question_title varchar(1024) null;

alter table q_user_answer
    add mapping_key varchar(32) null comment '映射Key' after type;

-- 筛查机构表，新增筛查类型配置字段
ALTER TABLE `m_screening_organization`
    ADD COLUMN `screening_type_config` varchar(10) COMMENT '筛查类型配置, 英文逗号分隔, 0-视力筛查，1-常见病';
-- 处理历史数据，默认为视力筛查配置
UPDATE `m_screening_organization` SET screening_type_config = '0';