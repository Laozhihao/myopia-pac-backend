
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES
       (818, '获取计划学校（计划中没有导入学生不显示）', 'querySchoolsInfoWithPlan', 'get:/management/screeningPlan/schools/haveStudents/**', 0, 0, 7, 27, 1);
