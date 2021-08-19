-- 更新 m_vision_screening_result 表，新增字段
ALTER TABLE `m_vision_screening_result`
    ADD COLUMN `ocular_inspection_data` json COMMENT '筛查结果--33cm眼位',
    ADD COLUMN `eye_pressure_data` json COMMENT '筛查结果--眼压',
    ADD COLUMN `fundus_data` json COMMENT '筛查结果--眼底',
    ADD COLUMN `slit_lamp_data` json COMMENT '筛查结果--裂隙灯检查',
    ADD COLUMN `pupil_optometry_data` json COMMENT '筛查结果--小瞳验光',
    ADD COLUMN `visual_loss_level_data` json COMMENT '筛查结果--盲及视力损害分类',
    ADD COLUMN `systemic_disease_symptom` varchar(500) COMMENT '筛查结果--全身疾病在眼部的表现';