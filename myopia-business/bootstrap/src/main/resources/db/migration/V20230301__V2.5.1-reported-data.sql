alter table m_national_data_download_record add screening_plan_id int(10) DEFAULT NULL COMMENT  '筛查计划id';
UPDATE m_device_report_template SET `name` = 'VS550报告-标准模板' WHERE `name` = 'VS666报告-标准模板';
INSERT INTO m_device_report_template ( id, `name`, device_type, template_type )
VALUES
    (2,'VS550报告-0.25D分辨率', 1,2),
    (3,'VS550报告-0.01D分辨率', 1,3);