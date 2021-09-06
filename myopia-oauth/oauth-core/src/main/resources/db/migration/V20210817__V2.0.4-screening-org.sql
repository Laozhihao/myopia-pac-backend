INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code, create_time,
                          update_time)
VALUES (341, '初始化筛查机构(临时)', 'managementScreeningOrganizationResetOrg', 'get:/management/screeningOrganization/resetOrg', 0,
        0, 1, 27, 1, '2021-08-19 10:39:13', '2021-08-19 10:39:13');

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code, create_time,
                          update_time)
VALUES (342, '打印告知书', 'printNotification', 'get:/management/screeningPlan/export/notice', 0, 0, 17, 27, 1,
        '2021-08-17 10:38:50', '2021-08-17 10:40:08');
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code, create_time,
                          update_time)
VALUES (343, '打印筛查二维码', 'printScreeningCode', 'get:/management/screeningPlan/export/QRCode', 0, 0, 18, 27, 1,
        '2021-08-17 10:42:09', '2021-08-17 10:42:09');
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code, create_time,
                          update_time)
VALUES (344, '打印VS666设备专属二维码', 'printEquipmentCode', 'get:/management/screeningPlan/export/QRCode', 0, 0, 19, 27, 1,
        '2021-08-17 10:44:38', '2021-08-17 10:44:38');
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code, create_time,
                          update_time)
VALUES (345, '获取指定任务下的机构信息', 'screeningOrgOfTaskInPlan', 'get:/management/screeningTask/orgs/**/**', 0, 0, 1, 27, 1,
        '2021-08-20 12:13:20', '2021-08-20 12:13:20');