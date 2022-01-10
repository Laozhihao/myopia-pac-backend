
-- 通用接口
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (600, '获取医院信息', NULL, 'get:/management/hospital/select/**', 0, 0, 1, 502, 1);

INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (601, '导出学生筛查数据Excel', NULL, 'get:/management/screeningResult/plan/export/schoolInfor', 0, 0, 1, 3, 1);
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (602, '导出学校报告PDF', NULL, 'get:/management/report/screeningOrg/export/school', 0, 0, 105, 3, 1);
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (603, '导出学生眼睛数据EXCEL', NULL, 'get:/management/screeningPlan/plan/export/studentInfor', 0, 0, 1, 502, 1);
