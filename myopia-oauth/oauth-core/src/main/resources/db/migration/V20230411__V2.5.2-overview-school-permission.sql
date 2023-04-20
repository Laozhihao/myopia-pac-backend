-- “多端管理-学校管理-筛查记录-学生管理”下新增权限
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES
(1125, '导出学校筛查数据模板（带学生信息）', 'schoolExportTemplate', 'get:/management/screeningResult/school/template/export', 0, 0, 0, 211, 1),
(1126, '导入学校筛查数据', 'schoolTemplateImport', 'post:/management/screeningResult/school/template/import', 0, 0, 0, 211, 1),
(1127, '导入筛查数据（按钮）', 'importScreeningData', null, 0, 0, 0, 211, 1),
(1128, '筛查学生列表（按钮）', 'screeningStudentListBtn', null, 0, 0, 1, 9, 1),
(1129, '按学校-查看筛查结果详情', 'schoolScreeningDetail', 'get:/management/screening-statistic/school/schoolStatisticDetail', 0, 0, 1, 9, 1),
(1130, '多端获取学校年级', 'manageSchoolGrade', 'get:/management/screeningPlan/grades/**/**', 0, 0, 1, 211, 1),
(1131, '多端筛查学生列表', 'manageScreeningStudentList', 'get:/management/screeningPlan/students/page', 0, 0, 1, 211, 1),
(1132, '多端更新筛查学生', 'manageScreeningPlanUpdatePlanStudent', 'post:/management/screeningPlan/update/planStudent', 0, 0, 1, 211, 1),
(1133, '获取筛查学校数据', 'osrSchoolList', 'get:/management/screeningPlan/schools/**', 0, 0, 1, 3, 1),
(1134, '多端获取学生筛查数据', 'manageGetStudentEyeByStudentId', 'get:/management/screeningPlan/getStudentEyeByStudentId', 0, 0, 1, 211, 1),
(1135, '多端删除筛查计划学生', 'manageDeletedPlanStudent', 'delete:/management/screeningPlan/deleted/planStudent/**', 0, 0, 1, 211, 1),
(1136, '多端获取班级', 'manageGetGradeInfo', 'get:/management/schoolGrade/all', 0, 0, 1, 211, 1),
(1137, '导入筛查学生数据(按钮)', 'manageScreeningPlanAddStudent', 'post:/management/screeningPlan/upload/**/**', 0, 0, 1, 211, 1),
(1138, '导出筛查学生', 'manageScreeningPlanExportStudent', 'get:/management/screeningPlan/export/planStudent/**/**', 0, 0, 1, 211, 1),
(1139, '导出筛查计划筛查学生', 'manageScreeningPlanExportStudentList', 'get:/management/screeningPlan/plan/export/studentInfo', 0, 0, 1, 211, 1);

UPDATE `o_permission` SET `name` =  '筛查学生列表页面', `api_url` = null, `is_menu` = 0, `is_page` = 1 WHERE `id` = 211;