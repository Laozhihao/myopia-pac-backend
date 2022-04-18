-- ID 从830开始
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (830, '复查告知书-导出复查告知书', 'reviewExport', 'get:/management/screeningPlan/review/export', 0, 0, 1, 27, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (831, '复查告知书-获取班级年级信息', 'reviewGetGrades', 'get:/management/screeningPlan/review/getGrades/**/**/**', 0, 0, 1, 27, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (832, '复查告知书-获取学校列表', 'reviewGetSchool', 'get:/management/screeningPlan/review/getSchool/**/**', 0, 0, 1, 27, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (823, '复测报告（二级）获取学校日期', 'sRescreenSchoolDate', 'get:/management/stat/rescreen/schoolDate', 0, 0, 0, 2, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (822, '复查报告（三级）获取学校日期', 'rescreenSchoolDate', 'get:/management/stat/rescreen/schoolDate', 0, 0, 0, 3, 1);
