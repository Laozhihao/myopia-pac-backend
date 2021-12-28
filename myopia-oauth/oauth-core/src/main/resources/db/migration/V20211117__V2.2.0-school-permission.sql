-- 学生管理相关
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code, create_time, update_time) VALUES
(376, '学生管理 (三级页面)', 'multiPortSchoolStudent', '', 1, 1, 0, 7, 1, '2021-11-16 21:45:01', '2021-11-17 10:25:00'),
(377, '学生列表', 'tManagementStudentList', 'get:/management/student/list', 0, 0, 0, 376, 1, '2021-11-17 10:22:56', '2021-11-17 11:09:58'),
(378, '学生档案', 'tMultiPortStudentFile', null, 1, 1, 1, 376, 1, '2021-11-17 11:09:58', '2021-11-17 11:09:58'),
(379, '学生管理增', 'tStudentCreate', 'post:/management/student/**', 0, 0, 1, 376, 1, '2021-11-17 11:09:58', '2021-11-17 11:09:58'),
(380, '学生管理删', 'tStudentDeleted', 'delete:/management/student/**', 0, 0, 1, 376, 1, '2021-11-17 11:09:58', '2021-11-17 11:09:58'),
(381, '学生管理改', 'tStudentUpdate', 'put:/management/student/**', 0, 0, 1, 376, 1, '2021-11-17 11:09:58', '2021-11-17 11:09:58'),
(382, '学生管理查', 'tStudentGet', 'get:/management/student/**', 0, 0, 1, 376, 1, '2021-11-17 11:09:59', '2021-11-17 11:09:59'),
(383, '学生导出', 'tStudentExport', 'get:/management/student/export', 0, 0, 1, 376, 1, '2021-11-17 11:09:59', '2021-11-17 11:09:59'),
(384, '学生导入模板', 'tStudentImportTemplate', 'get:/management/student/import/demo', 0, 0, 1, 376, 1, '2021-11-17 11:09:59', '2021-11-17 11:09:59'),
(385, '学生导入', 'tStudentImport', 'post:/management/student/import', 0, 0, 1, 376, 1, '2021-11-17 11:09:59', '2021-11-17 11:09:59'),
(386, '就诊档案列表', 'tMedicalReportList', 'get:/hospital/app/medicalReport/list/**', 0, 0, 1, 376, 1, '2021-11-17 11:09:59', '2021-11-17 11:09:59'),
(387, '就诊档案详情', 'tMedicalReportDetail', 'get:/hospital/app/medicalReport/**', 0, 0, 1, 376, 1, '2021-11-17 11:09:59', '2021-11-17 11:09:59'),
(388, '学校-学生管理按钮', 'tSchoolStudentManagementBtn', '', 0, 0, 1, 376, 1, '2021-11-17 11:10:00', '2021-11-17 11:10:00'),

-- 账号相关
(389, '学校-学生管理按钮', 'schoolStudentManagementBtn', 'get:/abc', 0, 0, 1, 7, 1, '2021-10-15 18:13:15', '2021-10-18 09:36:15'),
(390, '获取最新学校编号', 'getLatestSchoolNo', 'get:/management/school/getLatestSchoolNo', 0, 0, 1, 7, 1, '2021-10-15 13:11:21', '2021-10-15 13:11:21'),
(391, '医院-新增账号', 'addHospitalAdminUserAccount', 'post:/management/hospital/add/account/**', 0, 0, 1, 12, 1, '2021-10-13 13:41:04', '2021-10-13 13:41:04'),
(392, '医院-获取账号列表', 'getHospitalAdminUserAccountList', 'get:/management/hospital/accountList/**', 0, 0, 1, 12, 1, '2021-10-13 13:41:51', '2021-10-13 13:41:51'),
(393, '医院-停用启用账号', 'updateHospitalAdminUserAccountStatus', 'put:/management/hospital/admin/status', 0, 0, 1, 12, 1, '2021-10-13 13:43:21', '2021-10-13 13:43:21'),
(394, '学校-停用启用账号', 'updateSchoolAdminUserAccountStatus', 'put:/management/school/admin/status', 0, 0, 1, 7, 1, '2021-10-13 13:43:21', '2021-10-13 13:43:21'),
(395, '学校-获取账号列表', 'getSchoolAdminUserAccountList', 'get:/management/school/accountList/**', 0, 0, 1, 7, 1, '2021-10-13 13:41:51', '2021-10-13 13:41:51'),
(396, '学校-新增账号', 'addSchoolAdminUserAccount', 'post:/management/school/add/account/**', 0, 0, 1, 7, 1, '2021-10-13 13:41:04', '2021-10-13 13:41:04'),

-- 导出跟踪档案
(397, '导出指定计划下的单个学校的学生的预计跟踪档案', 'adminExportSchoolStudentWarningArchive', 'get:/export/student/warning/archive', 0, 0, 1, 2, 1, '2021-11-04 16:42:01', '2021-11-04 16:42:01'),
(398, '导出指定计划下的单个学校的学生的预计跟踪档案', 'orgExportSchoolStudentWarningArchive', 'get:/export/student/warning/archive', 0, 0, 1, 5, 1, '2021-11-04 16:41:21', '2021-11-04 16:41:21'),

-- 政府人员新增用户时获取角色信息
(399, '获取角色信息', 'getRoleInfo', 'get:/management/role/**', 0, 0, 1, 22, 1, '2021-11-04 16:41:21', '2021-11-04 16:41:21');

-- 更新“筛查机构账号列表”接口
UPDATE o_permission set api_url = 'post:/management/screeningOrganization/add/account/**' WHERE id = 373;