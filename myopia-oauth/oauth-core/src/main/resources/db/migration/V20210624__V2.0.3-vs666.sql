INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (339, '报告打印', 'ManagementDeviceReportPrint', 'get:/management/device/report/print', 0, 0, 1, 332, 1),
       (337, '配置机构', 'managementDeviceConfiguration', 'post:/management/device/report/template/configuration', 0, 0, 1,333, 1),
       (336, '通过模板Id获取筛查机构', 'managementDeviceGetOrgList', 'get:/management/device/report/template/getOrgList/**', 0, 0, 1, 333, 1),
       (335, '获取设备报告模板列表', 'managementDeviceList', 'get:/management/device/report/template/list', 0, 0, 1, 333, 1),
       (341, '通过筛查名称获取机构', 'managementScreeningOrganizationGetByName','get:/management/screeningOrganization/getByName', 0, 0, 1, 333, 1);