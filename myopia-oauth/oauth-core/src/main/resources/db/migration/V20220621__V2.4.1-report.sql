
-- 筛查结果数据转化筛查结论数据
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,pid, system_code)
VALUES(880, '筛查结果数据转化筛查结论数据', '', 'get:/management/screening-statistic/afreshScreeningToConclusion', 0, 0, 1, 30, 1);

-- 移除接口 get:/management/report/screeningOrg/export 、get:/management/report/screeningOrg/export/school , get:/management/test/triggerAll
DELETE FROM o_permission WHERE id IN ( 183, 276,602,385);

-- 测试和管理处理接口
UPDATE o_permission set api_url = 'get:/management/test/big' where id = 293;
UPDATE o_permission set api_url = 'get:/management/test/screeningToConclusion' where id = 857;
UPDATE o_permission set api_url = 'get:/management/test/afreshScreeningToConclusion' where id = 880;
UPDATE o_permission set api_url = 'get:/management/test/afreshStatistic' where id = 860;
UPDATE o_permission set api_url = 'get:/management/test/triggerAll' where id = 295;

-- 更新父级 (运维接口目录)
UPDATE o_permission set pid = 294 where id in (293,857,880,860,295);

