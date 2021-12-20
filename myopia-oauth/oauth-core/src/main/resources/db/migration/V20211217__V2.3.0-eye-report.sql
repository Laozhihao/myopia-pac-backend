INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (552, '更新筛查机构', 'updateOrg', 'put:/management/screeningOrganization/**', 0, 0, 0, 27, 1);
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (551, '获取角色信息', 'getRoleInfo', 'get:/management/role/**', 0, 0, 1, 22, 1);

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (553, '获取眼健康列表', 'getEyeHealthyList', 'get:/management/hospital/workbench/report/list', 0, 0, 1, 504, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (554, '眼健康就诊记录', 'patientReportList', 'get:/management/hospital/workbench/patient/report/list', 0, 0, 0, 505, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (555, '获取眼健康列表', 'tGetEyeHealthyList', 'get:/management/hospital/workbench/report/list', 0, 0, 1, 522, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (556, '眼健康就诊记录', 'tGatientReportList', 'get:/management/hospital/workbench/patient/report/list', 0, 0, 0, 523, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (557, '就诊列表', 'reportList', 'get:/management/student/report/list', 0, 0, 0, 505, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (558, '学生信息', 'studentInfo', 'get:/management/student/**', 0, 0, 0, 505, 1);
