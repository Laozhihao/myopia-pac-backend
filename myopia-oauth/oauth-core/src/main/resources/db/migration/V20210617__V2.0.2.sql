-- 统计对比
INSERT INTO `o_permission` (`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES 
(311, '统计对比获取对应类型年份', 'dataContrastYear', 'get:/management/stat/dataContrastYear', '0', '0', '1', '33', '1'),
(312, '统计对比获取过滤条件', 'dataContrastFilter', 'get:/management/stat/dataContrastFilter', '0', '0', '1', '33', '1');

-- 移除接口 get:/management/stat/dataContrastYear、get:/management/stat/dataContrast
DELETE FROM o_permission WHERE id IN ( 271, 272, 275 );

