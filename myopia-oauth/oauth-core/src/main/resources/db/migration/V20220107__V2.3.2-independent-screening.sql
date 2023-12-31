INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code, create_time, update_time)
VALUES
       (700, '虚拟学生管理', 'fictitiousStudent', null, 1, 1, 7, 1, 1, '2022-02-15 11:57:49', '2022-02-15 11:58:51'),
       (701, '虚拟学生列表', 'fictitiousStudentList', 'get:/management/mockPlanStudent/list', 0, 0, 1, 700, 1,'2022-02-24 14:20:14', '2022-02-24 14:20:14'),
       (702, '删除筛查计划学生', 'deletedPlanStudent', 'post:/management/screeningPlan/deleted/planStudent/**', 0, 0, 1, 27, 1,'2022-02-28 18:37:33', '2022-02-28 18:37:33'),
       (703, '筛查二维码', 'screeningQrcodePage', null, 1, 1, 0, 1, 1, '2022-03-01 15:29:48', '2022-03-01 15:29:48'),
       (704, '虚拟二维码', 'virtualScreeningQrcode', null, 1, 1, 0, 703, 1, '2022-03-01 14:54:53', '2022-03-01 15:30:04'),
       (705, 'VS666筛查二维码', 'vsScreeningQrcode', null, 1, 1, 1, 703, 1, '2022-03-01 14:53:50', '2022-03-01 15:30:04'),
       (706, '普通筛查二维码', 'screeningQrcode', null, 1, 1, 0, 703, 1, '2022-03-01 14:52:54', '2022-03-01 15:30:04'),
       (707, '通过机构类型获取权限', 'OrganizationGetPermission', 'get:/management/screeningOrganization/getPermission/**', 0, 0, 1, 703, 1, '2022-03-01 15:41:23', '2022-03-01 15:41:23');
