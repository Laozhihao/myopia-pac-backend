INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code, create_time,
                          update_time)
VALUES (682, '删除筛查计划学生', 'deletedPlanStudent', 'post:/management/screeningPlan/deleted/planStudent/**', 0, 0, 1, 27, 1,
        '2022-02-28 18:37:33', '2022-02-28 18:37:33'),
       (681, '虚拟学生列表', 'fictitiousStudentList', 'get:/management/mockPlanStudent/list', 0, 0, 1, 680, 1,
        '2022-02-24 14:20:14', '2022-02-24 14:20:14'),
       (680, '虚拟学生管理', 'fictitiousStudent', null, 1, 1, 7, 1, 1, '2022-02-15 11:57:49', '2022-02-15 11:58:51');