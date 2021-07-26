-- 菜单
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES
(323, 'VS666管理', 'equipment', NULL, 1, 1, 10, 0, 1),
(324, '设备管理', 'equipmentManage', NULL, 1, 1, 1, 323, 1),
(325, '数据管理', 'informationManage', NULL, 1, 1, 2, 323, 1),
(326, '报告管理', 'reportManage', NULL, 1, 1, 3, 323, 1);

-- 打印设备报告 & 设备报告模板
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES
(327, '报告打印', 'ManagementDeviceReportPrint', 'get:/management/device/report/print', 0, 0, 1, 325, 1),
(328, '配置机构', 'managementDeviceConfiguration', 'post:/management/device/report/template/configuration', 0, 0, 1,326, 1),
(329, '通过模板Id获取筛查机构', 'managementDeviceGetOrgList', 'get:/management/device/report/template/getOrgList/**', 0, 0, 2, 326, 1),
(330, '获取设备报告模板列表', 'managementDeviceList', 'get:/management/device/report/template/list', 0, 0, 3, 326, 1),
(331, '通过筛查名称获取机构', 'managementScreeningOrganizationGetByName','get:/management/screeningOrganization/getByName', 0, 0, 4, 326, 1);

-- 设备管理
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES
(332, '获取绑定的筛查机构列表（模糊查询）', 'screeningOrgListByLike', 'get:/management/screeningOrganization/getByName', 0, 0, 5, 324, 1),
(333, '停用|启用设备', 'updateDeviceStatus', 'put:/management/device/**/**', 0, 0, 4, 324, 1),
(334, '更新设备', 'updateDevice', 'put:/management/device', 0, 0, 3, 324, 1),
(335, '新增设备', 'addDevice', 'post:/management/device', 0, 0, 2, 324, 1),
(336, '获取设备列表（分页）', 'deviceList', 'get:/management/device/list', 0, 0, 1, 324, 1);

-- 设备数据
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES
(337, '视力筛查报告', 'visionReport', NULL, 0, 1, 2, 325, 1),
(338, '获取设备数据列表（分页）', 'deviceList', 'get:/management/device/data/list', 0, 0, 1, 325, 1);