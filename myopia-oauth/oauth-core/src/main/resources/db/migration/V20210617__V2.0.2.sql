-- 统计对比
INSERT INTO `o_permission` (`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES 
(321, '统计对比获取对应类型年份', 'dataContrastYear', 'get:/management/stat/dataContrastYear', '0', '0', '1', '33', '1'),
(322, '统计对比获取过滤条件', 'dataContrastFilter', 'get:/management/stat/dataContrastFilter', '0', '0', '2', '33', '1'),
(323, '导出对比数据', 'exportContrast', 'post:/management/stat/exportContrast', '0', '0', '3', '33', '1');

-- 移除接口 get:/management/stat/dataContrastYear、get:/management/stat/dataContrast、get:/management/stat/exportContrast
DELETE FROM o_permission WHERE id IN ( 271, 272, 275, 228 );

-- 筛查结果-学校 —— 导出复测报告页面
INSERT INTO `o_permission` (`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES
(324, '导出复测报告页面', 'retestReport', null, '0', '1', '1', '30', '1');