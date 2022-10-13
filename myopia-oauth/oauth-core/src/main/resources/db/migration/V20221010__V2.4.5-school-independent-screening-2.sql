INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1030, '模糊查询指定省份下学校', 'schoolProvinceList', 'get:/management/school/province/list', 0, 0, 0, 750, 1);


-- 1000-1009
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES (1000, '创建筛查任务时-获取筛查学校列表', 'getSchoolList', 'get:/management/school/getSchoolList', 0, 0, 1, 25, 1),
       (1001, '处理学校端学生的学龄段数据', 'gradeTypeData', 'get:/management/test/gradeTypeData', 0, 0, 0, 384, 1),
       (1002, '学生列表', 'schoolStudentList', 'get:/management/student/schoolList', 0, 0, 0, 10, 1),
       (1003, '学生列表搜索下拉框', 'selectValue', 'get:/management/student/selectValue', 0, 0, 0, 10, 1),
       (1004, '学校学生的筛查记录', 'schoolStudentScreeningList', 'get:/management/student/screening/list/**', 0, 0, 0, 11, 1);
