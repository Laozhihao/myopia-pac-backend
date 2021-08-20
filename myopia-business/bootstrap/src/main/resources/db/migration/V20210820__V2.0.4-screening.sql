ALTER TABLE `m_device_source_data` ADD COLUMN `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `screening_time`;

ALTER TABLE `m_device_screening_data` MODIFY COLUMN `check_result` varchar(64) NOT NULL default '' COMMENT '筛查结果' AFTER `check_mode`;

-- 移除不需要的模板类型
DELETE FROM m_template WHERE id = 3;
DELETE FROM m_template WHERE id = 4;
UPDATE m_template t SET t.name = '学生档案卡-海南省学生眼疾病筛查单' WHERE t.id = 2;
UPDATE m_template t SET t.name = '学生档案卡-屈光档案' WHERE t.id = 1;
TRUNCATE TABLE m_template_district;

-- 更新 m_vision_screening_result 表，新增字段
ALTER TABLE `m_vision_screening_result`
    ADD COLUMN `ocular_inspection_data` json COMMENT '筛查结果--33cm眼位',
    ADD COLUMN `eye_pressure_data` json COMMENT '筛查结果--眼压',
    ADD COLUMN `fundus_data` json COMMENT '筛查结果--眼底',
    ADD COLUMN `slit_lamp_data` json COMMENT '筛查结果--裂隙灯检查',
    ADD COLUMN `pupil_optometry_data` json COMMENT '筛查结果--小瞳验光',
    ADD COLUMN `visual_loss_level_data` json COMMENT '筛查结果--盲及视力损害分类',
    ADD COLUMN `systemic_disease_symptom` varchar(500) COMMENT '筛查结果--全身疾病在眼部的表现';