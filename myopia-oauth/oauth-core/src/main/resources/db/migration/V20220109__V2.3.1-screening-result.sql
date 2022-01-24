
-- 通用接口
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES
(600, '获取医院信息', 'getHospitalInfo', 'get:/management/hospital/**', 0, 0, 1, 502, 1),
(601, '导出学生筛查数据Excel', 'exportStudentExcel', 'get:/management/screeningResult/plan/export/schoolInfor', 0, 0, 105, 3, 1),
(602, '导出学校报告PDF', 'exportPlanSchoolPDF', 'get:/management/report/screeningOrg/export/school', 0, 0, 103, 3, 1),
(603, '导出学生眼睛数据EXCEL', 'exportPlanStudentEyeIfnorExcel', 'get:/management/screeningPlan/plan/export/studentInfor', 0, 0, 0, 27, 1),
(604, '查询学生眼镜信息', 'getStudentEyeByStudentId', 'get:/management/screeningPlan/getStudentEyeByStudentId', 0, 0, 107, 27, 1),
(606, '导出学生筛查数据Excel', 'exportStudentExcelSchool', 'get:/management/screeningResult/plan/export/schoolInfor', 0, 0, 60, 30, 1),
(608, '获取计划学校的年级信息（有计划）', 'getHaveResultGrades', 'get:/management/screeningPlan/grades/haveResult/**/**', 0, 0, 0, 27, 1),
(610, '获取计划学校的年级信息（有计划）', 'getHaveResultGrades', 'get:/management/screeningPlan/grades/haveResult/**/**', 0, 0, 0, 30, 1),
(611, '获取筛查学生（不分页）', 'getPlanStudentsList', 'get:/management/screeningPlan/students/list', 0, 0, 1, 27, 1);
