INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1140, '市区创建任务按钮', 'areaCreateTaskBtn', '', 0, 0, 0, 26, 1),
       (1141, '市区创建任务', 'areaCreateTask', 'post:/management/screeningTask/urbanArea/createTask', 0, 0, 0, 26, 1),
       (1142, '复查告知书-获取学校列表', 'reviewSchoolList', 'get:/management/screeningPlan/review/getSchool/**/**', 0, 0, 1, 9, 1);