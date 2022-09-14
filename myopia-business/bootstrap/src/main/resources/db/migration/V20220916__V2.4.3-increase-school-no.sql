
-- 处理学校历史数据，把编码增加到13位，其中学校自增部分补3个零
UPDATE m_school set school_no = CONCAT(SUBSTRING(school_no FROM 1 FOR 8), '000', SUBSTRING(school_no FROM 9 FOR 2));