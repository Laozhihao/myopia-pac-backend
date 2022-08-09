-- qes字段映射表 新增选项ID，系统字段可以为null
ALTER TABLE q_qes_field_mapping MODIFY COLUMN system_field varchar(60) NULL COMMENT '系统字段';
ALTER TABLE q_qes_field_mapping ADD option_id varchar(20) NULL COMMENT '选项ID' AFTER system_field;
ALTER TABLE q_qes_field_mapping DROP COLUMN questionnaire_id;
ALTER TABLE q_qes_field_mapping ADD `year` INT NOT NULL COMMENT '年份' AFTER `id`;