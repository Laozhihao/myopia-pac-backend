-- 医院、学校、筛查机构 账号停用\启用，重置密码
UPDATE `o_permission` SET `api_url` = 'put:/management/hospital', `name` = '医院编辑' WHERE `id` = '37';
INSERT INTO `o_permission` (`name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES ('停用/启用', 'hospitalStatusUpdate', 'put:/management/hospital/status', '0', '0', '1', '12', '1'),
('重置密码', 'hospitalPasswordReset', 'post:/management/hospital/reset', '0', '0', '1', '12', '1');

UPDATE `o_permission` SET `api_url` = 'put:/management/school', `name` = '学校编辑' WHERE `id` = '41';
INSERT INTO `o_permission` (`name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES ('停用/启用', 'schoolStatusUpdate', 'put:/management/school/status', '0', '0', '1', '7', '1'),
('重置密码', 'schoolPasswordReset', 'post:/management/school/reset', '0', '0', '1', '7', '1');

UPDATE `o_permission` SET `api_url` = 'put:/management/screeningOrganization', `name` = '筛查机构编辑' WHERE `id` = '61';
INSERT INTO `o_permission` (`name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES ('停用/启用', 'screeningOrganizationStatusUpdate', 'put:/management/screeningOrganization/status', '0', '0', '1', '2', '1'),
('重置密码', 'screeningOrganizationPasswordReset', 'post:/management/screeningOrganization/reset', '0', '0', '1', '2', '1');


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


