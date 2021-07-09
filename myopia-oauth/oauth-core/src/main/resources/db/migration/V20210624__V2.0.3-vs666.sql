-- 打印设备报告 & 设备报告模板
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (339, '报告打印', 'ManagementDeviceReportPrint', 'get:/management/device/report/print', 0, 0, 1, 332, 1),
       (337, '配置机构', 'managementDeviceConfiguration', 'post:/management/device/report/template/configuration', 0, 0, 1,333, 1),
       (336, '通过模板Id获取筛查机构', 'managementDeviceGetOrgList', 'get:/management/device/report/template/getOrgList/**', 0, 0, 1, 333, 1),
       (335, '获取设备报告模板列表', 'managementDeviceList', 'get:/management/device/report/template/list', 0, 0, 1, 333, 1),
       (341, '通过筛查名称获取机构', 'managementScreeningOrganizationGetByName','get:/management/screeningOrganization/getByName', 0, 0, 1, 333, 1);


-- 设备管理
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES
(342, '获取绑定的筛查机构列表（模糊查询）', 'screeningOrgListByLike', 'get:/management/screeningOrganization/getByName', 0, 0, 5, 331, 1),
(343, '停用|启用设备', 'updateDeviceStatus', 'put:/management/device/**/**', 0, 0, 4, 331, 1),
(344, '更新设备', 'updateDevice', 'put:/management/device', 0, 0, 3, 331, 1),
(345, '新增设备', 'addDevice', 'post:/management/device', 0, 0, 2, 331, 1),
(346, '获取设备列表（分页）', 'deviceList', 'get:/management/device/list', 0, 0, 1, 331, 1);

-- 设备数据
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES (347, '获取设备数据列表（分页）', 'deviceList', 'get:/management/device/data/list', '0', '0', '1', '332', '1');