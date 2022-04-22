-- ID 从830开始
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (830, '复查告知书-导出复查告知书', 'reviewExport', 'get:/management/screeningPlan/review/export', 0, 0, 1, 27, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (831, '复查告知书-获取班级年级信息', 'reviewGetGrades', 'get:/management/screeningPlan/review/getGrades/**/**/**', 0, 0, 1, 27, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (832, '复查告知书-获取学校列表', 'reviewGetSchool', 'get:/management/screeningPlan/review/getSchool/**/**', 0, 0, 1, 27, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (833, '复测报告（二级）获取学校日期', 'sRescreenSchoolDate', 'get:/management/stat/rescreen/schoolDate', 0, 0, 0, 2, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (834, '复查报告（三级）获取学校日期', 'rescreenSchoolDate', 'get:/management/stat/rescreen/schoolDate', 0, 0, 0, 3, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (835, '学生档案卡路径', 'workOrderStudentScreeningList', 'get:/management/student/screeningResult/**', 0, 0, 1, 811, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES
    (841, '学生档案卡路径', 'studentScreeningResult', 'get:/management/student/screeningResult/**', 0, 0, 1, 811, 1);
UPDATE o_permission SET pid = 25 WHERE pid = 24;

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,pid, system_code)
VALUES (846, '获取筛查学校详情', 'screeningSchoolDetails', 'get:/management/screeningTask/screeningSchoolDetails/**', 0, 0, 11, 26, 1);

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,pid, system_code)
VALUES (847, '增加筛查计划时间', 'updateScreeningEndTime', 'get:/management/screeningPlan/increased/screeningTime', 0, 0, 36, 27, 1);

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,pid, system_code)
VALUES (848, '获取根据天数增加之后的时间', 'getTncreaseDate', 'get:/management/screeningPlan/getTncreaseDate', 0, 0, 37, 27, 1);


--- 按区域和按学校
UPDATE `o_permission` SET `name` = '按区域统计', `pid` = 31,  `update_time` = NOW() WHERE `id` = 29;
UPDATE `o_permission` SET `name` = '按学校统计', `pid` = 31, `update_time` = NOW() WHERE `id` = 30;


INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,pid, system_code)
VALUES
       (851, '按区域-幼儿园', 'getDistrictKindergartenStatistic', 'get:/management/screening-statistic/district/kindergartenResult', 0, 0, 1, 29, 1),
       (852, '按区域-小学及以上', 'getDistrictPrimaryAndAboveStatistic', 'get:/management/screening-statistic/district/primarySchoolAndAboveResult', 0, 0, 1, 29, 1),
       (853, '按区域-查看详情', 'districtDetail', 'get:/management/screening-statistic/district/screeningResultTotalDetail', 0, 0, 1, 29, 1),
       (854, '按学校-幼儿园', 'getSchoolKindergartenStatistic', 'get:/management/screening-statistic/school/kindergartenResult', 0, 0, 1, 30, 1),
       (855, '按学校-小学及以上', 'getSchoolPrimaryAndAboveStatistic', 'get:/management/screening-statistic/school/primarySchoolAndAboveResult', 0, 0, 1, 30, 1),
       (856, '按学校-查看详情', 'schoolDetail', 'get:/management/screening-statistic/school/schoolStatisticDetail', 0, 0, 1, 30, 1);
