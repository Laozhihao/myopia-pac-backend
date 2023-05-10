INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1160, '获取学生类型', 'refactorSchoolStudentType', 'get:/management/report/refactor/school/studentType', 0, 0, 0, 9, 1),
       (1161, '获取档案卡', 'archiveExport', 'get:/management/archive/export', 0, 0, 0, 9, 1),
       (1162, '学校告知书配置', 'schoolResultNoticeConfig', 'put:/management/school/update/resultNoticeConfig/**', 0, 0, 0, 9, 1);