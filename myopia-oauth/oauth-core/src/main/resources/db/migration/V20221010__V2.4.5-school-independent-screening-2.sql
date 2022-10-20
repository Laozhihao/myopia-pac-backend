INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1030, '模糊查询指定省份下学校', 'schoolProvinceList', 'get:/management/school/province/list', 0, 0, 0, 750, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1050, '系统更新提醒', 'systemUpdateNotice', null, 1, 1, 0, 14, 1),
       (1051, '系统更新通知下线通知', 'systemNoticeStatus', 'post:/management/systemNotice/status/**', 0, 0, 0, 1050,1),
       (1052, '系统更新通知保存通知', 'systemNoticeSave', 'post:/management/systemNotice/save', 0, 0, 0, 1050, 1),
       (1053, '系统更新通知获取列表', 'systemNoticeList', 'get:/management/systemNotice/list', 0, 0, 0, 1050, 1);


UPDATE `o_permission` SET `api_url` = 'get:/management/screeningOrganization/getOrgList'  WHERE `menu_btn_name` = 'getScreeningOrganizationList';

-- 1000-1009
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES (1000, '创建筛查任务时-获取筛查学校列表', 'getSchoolList', 'get:/management/school/getSchoolList', 0, 0, 1, 25, 1),
       (1001, '处理学校端学生的学龄段数据', 'gradeTypeData', 'get:/management/test/gradeTypeData', 0, 0, 0, 384, 1),
       (1002, '学生列表', 'schoolStudentList', 'get:/management/student/schoolList', 0, 0, 0, 10, 1),
       (1003, '学生列表搜索下拉框', 'selectValue', 'get:/management/student/selectValue', 0, 0, 0, 10, 1),
       (1004, '学校学生的筛查记录', 'schoolStudentScreeningList', 'get:/management/student/screening/list/**', 0, 0, 0, 11, 1),
       (1005, '学校学生详情', 'getSchoolStudentDetail', 'get:/management/student/school/**', 0, 0, 0, 11, 1),
       (1006, '更新学校学生信息', 'saveSchoolStudent', 'post:/management/student/school/save', 0, 0, 0, 11, 1),
       (1007, '处理学校学生年份', 'schoolStudentYearData', 'get:/management/test/schoolStudentYearData', 0, 0, 0, 384, 1);

