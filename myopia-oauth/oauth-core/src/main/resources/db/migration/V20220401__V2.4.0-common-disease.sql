-- ID 从830开始
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES
(841, '获取学生复测卡', 'studentScreeningResult', 'get:/management/student/screeningResult', 0, 0, 11, 10, 1);

UPDATE o_permission SET pid = 25 WHERE pid = 24;

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,pid, system_code)
VALUES (846, '获取筛查学校详情', 'screeningSchoolDetails', 'get:/management/screeningTask/screeningSchoolDetails/**', 0, 0, 11, 26, 1);

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,pid, system_code)
VALUES (847, '增加筛查计划时间', 'updateScreeningEndTime', 'get:/management/screeningPlan/increased/screeningTime', 0, 0, 36, 27, 1);
