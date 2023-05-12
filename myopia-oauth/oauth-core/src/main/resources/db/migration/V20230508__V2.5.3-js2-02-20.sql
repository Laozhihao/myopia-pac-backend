INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1160, '获取学生类型', 'refactorSchoolStudentType', 'get:/management/report/refactor/school/studentType', 0, 0, 0, 9, 1),
       (1161, '获取档案卡', 'archiveExport', 'get:/management/archive/export', 0, 0, 0, 9, 1),
       (1162, '学校告知书配置', 'schoolResultNoticeConfig', 'put:/management/school/update/resultNoticeConfig/**', 0, 0, 0, 9, 1),
       (1163, '幼儿园报告', 'schoolRefactorSchoolKindergarten', 'get:/management/report/refactor/school/kindergarten', 0, 0, 0, 9, 1),
       (1164, '中小学报告', 'schoolRefactorSchoolPrimary', 'get:/management/report/refactor/school/primary', 0, 0, 0, 9, 1),
       (1165, '删除计划学校', 'deleteScreeningPlanSchoolBtn', 'delete:/management/screeningPlan/school/**/**', 0, 0, 1, 3, 1);