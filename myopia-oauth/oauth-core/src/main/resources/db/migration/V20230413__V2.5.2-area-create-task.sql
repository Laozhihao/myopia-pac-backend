INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1140, '市区创建任务按钮', 'areaCreateTaskBtn', '', 0, 0, 0, 26, 1),
       (1141, '市区创建任务', 'areaCreateTask', 'post:/management/screeningTask/urbanArea/createTask', 0, 0, 0, 26, 1),
       (1142, '复查告知书-获取学校列表', 'reviewSchoolList', 'get:/management/screeningPlan/review/getSchool/**/**', 0, 0, 1, 9, 1),
       (1143, '复查告知书-获取班级年级信息', 'reviewSchoolGrades', 'get:/management/screeningPlan/review/getGrades/**/**/**', 0, 0, 1, 9, 1),
       (1144, '复查告知书-导出复查告知书', 'reviewExport', 'get:/management/screeningPlan/review/export', 0, 0, 1, 9, 1),
       (1145, '获取学生民族列表', 'studentNation', 'get:/management/student/nation', 0, 0, 1, 3, 1),
       (372, '获取学生民族列表', 'getStudentNation', 'get:/management/student/nation', 0, 0, 1, 27, 1);