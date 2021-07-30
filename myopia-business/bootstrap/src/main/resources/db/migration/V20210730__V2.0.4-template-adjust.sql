DELETE FROM m_template WHERE id = 3;
DELETE FROM m_template WHERE id = 4;
UPDATE m_template t SET t.name = '学生档案卡-海南省学生眼疾病筛查单' WHERE t.id = 2;
UPDATE m_template t SET t.name = '学生档案卡-屈光档案' WHERE t.id = 1;
TRUNCATE TABLE m_template_district;