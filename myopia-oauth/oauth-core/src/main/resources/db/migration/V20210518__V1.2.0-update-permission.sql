-- 导出复测报告功能
INSERT INTO `o_permission` (`name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES ('导出复测报告功能', 'statResultSchoolRescreenExport', 'get:/management/stat/rescreen/export', '0', '0', '1', '30', '1'),
 ('导出复测报告功能', 'orgScreenRecordRescreenExport', 'get:/management/stat/rescreen/export', '0', '0', '1', '3', '1'),
 ('导出复测报告功能', 'schoolScreenRecordRescreenExport', 'get:/management/stat/rescreen/export', '0', '0', '1', '9', '1');


-- 筛查结果-学校 按计划筛选功能
INSERT INTO `o_permission` (`name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES ('获取计划年度', 'screenStatPlanYear', 'get:/management/screening-statistic/plan-year', '0', '0', '1', '30', '1'),
 ('获取年度计划', 'screenStatPlan', 'get:/management/screening-statistic/plan', '0', '0', '1', '30', '1'),
 ('获取计划地区信息', 'getPlanDistrict', 'get:/management/screening-statistic/plan-district', '0', '0', '1', '30', '1'),
 ('计划下学校视力情况', 'planScreeningVisionResult', 'get:/management/screening-statistic/plan/school/screening-vision-result', '0', '0', '1', '30', '1'),
 ('计划下学校监控统计情况', 'planScreeningMonitorResult', 'get:/management/screening-statistic/plan/school/screening-monitor-result', '0', '0', '1', '30', '1');


