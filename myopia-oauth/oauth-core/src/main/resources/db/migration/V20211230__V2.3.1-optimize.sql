
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES
(600, '获取医院信息', 'getHospitalInfo', 'get:/management/hospital/**', 0, 0, 1, 502, 1),
(601, '导出学生筛查数据Excel', 'exportScreeningData', 'get:/management/screeningResult/plan/export/schoolInfo', 0, 0, 1, 3, 1),
(602, '导出学校报告PDF', 'exportPlanSchoolPDF', 'get:/management/report/screeningOrg/export/school', 0, 0, 1, 3, 1),
(603, '导出学生筛查数据', 'exportPlanStudentScreeningData', 'get:/management/screeningPlan/plan/export/studentInfo', 0, 0, 1, 27, 1),
(604, '获取学生筛查数据', 'getStudentEyeByStudentId', 'get:/management/screeningPlan/getStudentEyeByStudentId', 0, 0, 1, 27, 1),
(605, '导出VS666设备数据', 'exportDeviceData', 'get:/management/device/report/excel', 0, 0, 1, 327, 1),
(606, '导出学生筛查数据Excel', 'exportScreeningDataSchool', 'get:/management/screeningResult/plan/export/schoolInfo', 0, 0, 1, 30, 1),
(607, '获取计划学校的年级信息（有计划）', 'getHaveResultGradesPlan', 'get:/management/screeningPlan/grades/haveResult/**/**', 0, 0, 1, 27, 1),
(608, '获取计划学校的年级信息（有计划）', 'getHaveResultGradesSchool', 'get:/management/screeningPlan/grades/haveResult/**/**', 0, 0, 1, 30, 1),
(609, '获取筛查学生（不分页）', 'getPlanStudentsList', 'get:/management/screeningPlan/students/list', 0, 0, 1, 30, 1),
(610, '导出学生筛查数据Excel', 'exportScreeningDataPlan', 'get:/management/screeningResult/plan/export/schoolInfo', 0, 0, 1, 27, 1),
(611, '更新结果通知配置', 'updateResultNoticeConfig', 'put:/management/screeningOrganization/update/resultNoticeConfig/**', 0, 0, 1, 27, 1),
(612, '异步导出学生报告', 'asyncGeneratorPDF', 'get:/management/screeningPlan/screeningNoticeResult/asyncGeneratorPDF', 0, 0, 0, 27, 1),
(613, '同步导出学生报告', 'syncGeneratorPDF', 'get:/management/screeningPlan/screeningNoticeResult/syncGeneratorPDF', 0, 0, 0, 27, 1);