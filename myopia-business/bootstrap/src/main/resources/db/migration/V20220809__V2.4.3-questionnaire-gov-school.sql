alter table q_questionnaire_question
    add is_hidden boolean null comment '是否隐藏';

alter table q_questionnaire_question
    add qes_data json null comment 'qes字段序号';

alter table q_user_question_record
    add record_type int null comment '汇总类型 1-汇总' after status;

alter table q_question
    add mapping_key varchar(32) null comment '前端映射key' after icon_name;

alter table myopia_business.q_user_answer
    add table_json json null comment '表格JSON' after answer;


-- 问卷表修改字段
ALTER TABLE q_questionnaire CHANGE qes_url qes_id INT NULL COMMENT 'qes管理ID';

-- qes字段映射表修改
ALTER TABLE q_qes_field_mapping MODIFY COLUMN system_field varchar(60) NULL COMMENT '系统字段';
ALTER TABLE q_qes_field_mapping ADD option_id varchar(255) NULL COMMENT '选项ID' AFTER system_field;
ALTER TABLE q_qes_field_mapping DROP COLUMN questionnaire_id;
ALTER TABLE q_qes_field_mapping ADD `qes_id` INT NULL COMMENT '区域ID' AFTER `id`;
ALTER TABLE q_qes_field_mapping ADD `year` INT NOT NULL COMMENT '年份' AFTER `qes_id`;


-- qes管理表
CREATE TABLE `q_questionnaire_qes` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `district_id` int(11) DEFAULT NULL COMMENT '区域ID',
  `year` int(11) NOT NULL COMMENT '年份',
  `name` varchar(255) NOT NULL COMMENT '问卷名称',
  `description` varchar(255) DEFAULT NULL COMMENT '问卷描述',
  `qes_file_id` int(11) DEFAULT NULL COMMENT 'qes文件的资源文件ID（m_resource_file表ID）',
  `preview_file_id` int(11) DEFAULT NULL COMMENT '预览文件的资源文件ID (m_resource_file表ID）',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷qes管理表';