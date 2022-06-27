
-- 筛查结果数据转化筛查结论数据
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,pid, system_code)
VALUES(880, '筛查结果数据转化筛查结论数据', '', 'get:/management/screening-statistic/afreshScreeningToConclusion', 0, 0, 1, 30, 1);

-- 移除接口 get:/management/report/screeningOrg/export 、get:/management/report/screeningOrg/export/school
DELETE FROM o_permission WHERE id IN ( 183, 276,602 );
