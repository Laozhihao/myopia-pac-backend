-- ID从1070开始，新谋：1070 ~ 1074，立周：1075~1079
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1070, '幼儿园报告', 'refactorSchoolKindergarten', 'get:/management/report/refactor/school/kindergarten', 0, 0,0, 570, 1),
       (1071, '获取学生类型', 'refactorSchoolStudentType', 'get:/management/report/refactor/school/studentType', 0, 0,0, 570, 1),
       (1072, '中小学报告', 'refactorSchoolPrimary', 'get:/management/report/refactor/school/primary', 0, 0, 0, 570, 1);