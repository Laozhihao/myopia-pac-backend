-- 初始化权限资源
INSERT INTO `o_permission` VALUES (1, '多端管理', 'multiPort', NULL, 1, 1, 2, 0, 1, '2021-02-01 03:36:47', '2021-02-01 13:26:33');
INSERT INTO `o_permission` VALUES (2, '筛查机构管理', 'multiPortOrganization', NULL, 1, 1, 1, 1, 1, '2021-02-01 03:36:47', '2021-02-01 13:24:12');
INSERT INTO `o_permission` VALUES (3, '筛查机构 筛查记录 (三级页面)', 'multiPortOrganizationScreeningRecord', NULL, 1, 1, 1, 1, 1, '2021-02-01 03:36:47', '2021-02-01 13:24:12');
INSERT INTO `o_permission` VALUES (4, '筛查人员管理 (三级页面)', 'multiPortOrganizationScreeningStaff', NULL, 1, 1, 1, 1, 1, '2021-02-01 03:36:47', '2021-02-01 13:24:12');
INSERT INTO `o_permission` VALUES (5, '筛查机构 筛查记录 (二级页面)', 'multiPortScreeningRecord', NULL, 1, 1, 1, 1, 1, '2021-02-01 03:36:47', '2021-02-01 13:24:12');
INSERT INTO `o_permission` VALUES (6, '筛查人员管理 (二级页面)', 'multiPortScreeningStaff', NULL, 1, 1, 1, 1, 1, '2021-02-01 03:36:47', '2021-02-01 13:24:12');
INSERT INTO `o_permission` VALUES (7, '学校管理', 'multiPortSchool', NULL, 1, 1, 1, 1, 1, '2021-02-01 03:36:47', '2021-02-01 13:24:12');
INSERT INTO `o_permission` VALUES (8, '年级管理', 'multiPortSchoolGrade', NULL, 1, 1, 1, 1, 1, '2021-02-01 03:36:47', '2021-02-01 13:24:12');
INSERT INTO `o_permission` VALUES (9, '学校 筛查记录', 'multiPortSchoolScreeningRecord', NULL, 1, 1, 1, 1, 1, '2021-02-01 03:36:47', '2021-02-01 13:24:12');
INSERT INTO `o_permission` VALUES (10, '学生管理', 'multiPortStudent', NULL, 1, 1, 1, 1, 1, '2021-02-01 03:36:47', '2021-02-01 13:24:12');
INSERT INTO `o_permission` VALUES (11, '学生档案', 'multiPortStudentFile', NULL, 1, 1, 1, 1, 1, '2021-02-01 03:36:47', '2021-02-01 13:24:12');
INSERT INTO `o_permission` VALUES (12, '医院管理', 'multiPortHospital', NULL, 1, 1, 1, 1, 1, '2021-02-01 03:36:47', '2021-02-01 13:24:12');
INSERT INTO `o_permission` VALUES (13, '个人中心', 'accountCenter', NULL, 1, 1, 6, 0, 1, '2021-02-01 03:40:39', '2021-02-01 13:27:09');
INSERT INTO `o_permission` VALUES (14, '系统中心', 'systemCenter', NULL, 1, 1, 5, 0, 1, '2021-02-01 03:40:39', '2021-02-01 13:27:12');
INSERT INTO `o_permission` VALUES (15, '页面资源设置', 'pageSourceSetting', NULL, 1, 1, 1, 14, 1, '2021-02-01 03:40:39', '2021-02-01 13:28:47');
INSERT INTO `o_permission` VALUES (16, '权限集合包设置', 'permissionsGather', NULL, 1, 1, 1, 14, 1, '2021-02-01 03:40:39', '2021-02-01 13:28:47');
INSERT INTO `o_permission` VALUES (17, '档案卡模板设置', 'fileCardTemplate', NULL, 1, 1, 1, 14, 1, '2021-02-01 03:40:39', '2021-02-01 13:28:47');
INSERT INTO `o_permission` VALUES (18, '筛查报告模板设置', 'screeningReportTemplate', NULL, 1, 1, 1, 14, 1, '2021-02-01 03:40:39', '2021-02-01 13:28:47');
INSERT INTO `o_permission` VALUES (19, '用户管理', 'user', NULL, 1, 1, 4, 0, 1, '2021-02-01 03:42:51', '2021-02-01 13:27:22');
INSERT INTO `o_permission` VALUES (20, '部门管理', 'departmentManager', NULL, 1, 1, 1, 19, 1, '2021-02-01 03:42:51', '2021-02-01 13:16:26');
INSERT INTO `o_permission` VALUES (21, '角色管理', 'roleManager', NULL, 1, 1, 1, 19, 1, '2021-02-01 03:42:51', '2021-02-01 13:16:26');
INSERT INTO `o_permission` VALUES (22, '用户管理2', 'usersManager', NULL, 1, 1, 1, 19, 1, '2021-02-01 03:42:51', '2021-02-01 13:16:26');
INSERT INTO `o_permission` VALUES (23, '筛查管理', 'screening', NULL, 1, 1, 1, 0, 1, '2021-02-01 03:42:51', '2021-02-01 03:42:51');
INSERT INTO `o_permission` VALUES (24, '发布筛查通知', 'screeningPublish', NULL, 1, 1, 1, 23, 1, '2021-02-01 03:42:51', '2021-02-01 13:22:05');
INSERT INTO `o_permission` VALUES (25, '筛查通知', 'screeningNotice', NULL, 1, 1, 1, 23, 1, '2021-02-01 03:42:51', '2021-02-01 13:22:05');
INSERT INTO `o_permission` VALUES (26, '筛查任务', 'screeningTask', NULL, 1, 1, 1, 23, 1, '2021-02-01 03:42:51', '2021-02-01 13:22:05');
INSERT INTO `o_permission` VALUES (27, '筛查计划', 'screeningPlan', NULL, 1, 1, 1, 23, 1, '2021-02-01 03:42:51', '2021-02-01 13:22:05');
INSERT INTO `o_permission` VALUES (28, '筛查学生列表', 'screeningStudent', NULL, 1, 1, 1, 23, 1, '2021-02-01 03:42:51', '2021-02-01 13:22:05');
INSERT INTO `o_permission` VALUES (29, '筛查结果-区域', 'screeningArea', NULL, 1, 1, 1, 23, 1, '2021-02-01 03:42:51', '2021-02-01 13:22:05');
INSERT INTO `o_permission` VALUES (30, '筛查结果-学校', 'screeningVision', NULL, 1, 1, 1, 23, 1, '2021-02-01 03:42:51', '2021-02-01 13:22:05');
INSERT INTO `o_permission` VALUES (31, '统计报表', 'statistics', NULL, 1, 1, 3, 0, 1, '2021-02-01 03:42:51', '2021-02-01 13:27:49');
INSERT INTO `o_permission` VALUES (32, '统计分析', 'statisticsAnalysis', NULL, 1, 1, 1, 31, 1, '2021-02-01 03:42:51', '2021-02-01 13:22:41');
INSERT INTO `o_permission` VALUES (33, '数据对比-时间', 'dataCompareTime', NULL, 1, 1, 1, 31, 1, '2021-02-01 03:42:51', '2021-02-01 13:22:41');
INSERT INTO `o_permission` VALUES (34, '消息中心', 'infoCenter', NULL, 1, 1, 7, 0, 1, '2021-02-01 03:42:51', '2021-02-01 13:27:40');
INSERT INTO `o_permission` VALUES (35, '医院管理增', 'hospitalCreate', 'post:/management/hospital/**', 0, 0, 1, 12, 1, '2021-02-01 03:53:54', '2021-02-01 16:09:17');
INSERT INTO `o_permission` VALUES (36, '医院管理删', NULL, 'delete:/management/hospital/**', 0, 0, 1, 12, 1, '2021-02-01 03:53:54', '2021-02-01 03:53:54');
INSERT INTO `o_permission` VALUES (37, '医院管理改', NULL, 'put:/management/hospital/**', 0, 0, 1, 12, 1, '2021-02-01 03:53:54', '2021-02-01 03:53:54');
INSERT INTO `o_permission` VALUES (38, '医院管理查', NULL, 'get:/management/hospital/**', 0, 0, 1, 12, 1, '2021-02-01 03:53:54', '2021-02-01 03:53:54');
INSERT INTO `o_permission` VALUES (39, '学校管理增', 'schoolCreate', 'post:/management/school/**', 0, 0, 1, 7, 1, '2021-02-01 03:56:50', '2021-02-01 16:08:07');
INSERT INTO `o_permission` VALUES (40, '学校管理删', NULL, 'delete:/management/school/**', 0, 0, 1, 7, 1, '2021-02-01 03:56:50', '2021-02-01 03:56:50');
INSERT INTO `o_permission` VALUES (41, '学校管理改', NULL, 'put:/management/school/**', 0, 0, 1, 7, 1, '2021-02-01 03:56:50', '2021-02-01 03:56:50');
INSERT INTO `o_permission` VALUES (42, '学校管理查', NULL, 'get:/management/school/**', 0, 0, 1, 7, 1, '2021-02-01 03:56:50', '2021-02-01 03:56:50');
INSERT INTO `o_permission` VALUES (43, '学生管理增', 'studentCreate', 'post:/management/student/**', 0, 0, 1, 10, 1, '2021-02-01 03:58:18', '2021-02-01 16:06:55');
INSERT INTO `o_permission` VALUES (44, '学生管理删', NULL, 'delete:/management/student/**', 0, 0, 1, 10, 1, '2021-02-01 03:58:18', '2021-02-01 03:58:18');
INSERT INTO `o_permission` VALUES (45, '学生管理改', NULL, 'put:/management/student/**', 0, 0, 1, 10, 1, '2021-02-01 03:58:18', '2021-02-01 03:58:18');
INSERT INTO `o_permission` VALUES (46, '学生管理查', NULL, 'get:/management/student/**', 0, 0, 1, 10, 1, '2021-02-01 03:58:18', '2021-02-01 03:58:18');
INSERT INTO `o_permission` VALUES (47, '班级管理增', NULL, 'post:/management/schoolClass/**', 0, 0, 1, 8, 1, '2021-02-01 04:00:49', '2021-02-01 04:00:49');
INSERT INTO `o_permission` VALUES (48, '班级管理删', NULL, 'delete:/management/schoolClass/**', 0, 0, 1, 8, 1, '2021-02-01 04:00:49', '2021-02-01 04:00:49');
INSERT INTO `o_permission` VALUES (49, '班级管理改', NULL, 'put:/management/schoolClass/**', 0, 0, 1, 8, 1, '2021-02-01 04:00:49', '2021-02-01 04:00:49');
INSERT INTO `o_permission` VALUES (50, '班级管理查', NULL, 'get:/management/schoolClass/**', 0, 0, 1, 8, 1, '2021-02-01 04:00:49', '2021-02-01 04:00:49');
INSERT INTO `o_permission` VALUES (51, '年级管理增', NULL, 'post:/management/schoolGrade/**', 0, 0, 1, 8, 1, '2021-02-01 04:00:49', '2021-02-01 04:00:49');
INSERT INTO `o_permission` VALUES (52, '年级管理删', NULL, 'delete:/management/schoolGrade/**', 0, 0, 1, 8, 1, '2021-02-01 04:00:49', '2021-02-01 04:00:49');
INSERT INTO `o_permission` VALUES (53, '年级管理改', NULL, 'put:/management/schoolGrade/**', 0, 0, 1, 8, 1, '2021-02-01 04:00:49', '2021-02-01 04:00:49');
INSERT INTO `o_permission` VALUES (54, '年级管理查', NULL, 'get:/management/schoolGrade/**', 0, 0, 1, 8, 1, '2021-02-01 04:00:49', '2021-02-01 04:00:49');
INSERT INTO `o_permission` VALUES (59, '筛查机构管理增', 'organizationCreate', 'post:/management/screeningOrganization/**', 0, 0, 1, 2, 1, '2021-02-01 04:02:10', '2021-02-01 16:45:28');
INSERT INTO `o_permission` VALUES (60, '筛查机构管理删', NULL, 'delete:/management/screeningOrganization/**', 0, 0, 1, 2, 1, '2021-02-01 04:02:10', '2021-02-01 04:03:57');
INSERT INTO `o_permission` VALUES (61, '筛查机构管理改', NULL, 'put:/management/screeningOrganization/**', 0, 0, 1, 2, 1, '2021-02-01 04:02:10', '2021-02-01 04:03:57');
INSERT INTO `o_permission` VALUES (62, '筛查机构管理查', NULL, 'get:/management/screeningOrganization/**', 0, 0, 1, 2, 1, '2021-02-01 04:02:10', '2021-02-01 04:03:57');
INSERT INTO `o_permission` VALUES (63, '筛查人员管理增', 'screeningStaffCreate', 'post:/management/screeningOrganizationStaff/**', 0, 0, 1, 4, 1, '2021-02-01 04:03:57', '2021-02-01 16:13:03');
INSERT INTO `o_permission` VALUES (64, '筛查人员管理删', NULL, 'delete:/management/screeningOrganizationStaff/**', 0, 0, 1, 4, 1, '2021-02-01 04:03:57', '2021-02-01 04:03:57');
INSERT INTO `o_permission` VALUES (65, '筛查人员管理改', NULL, 'put:/management/screeningOrganizationStaff/**', 0, 0, 1, 4, 1, '2021-02-01 04:03:57', '2021-02-01 04:03:57');
INSERT INTO `o_permission` VALUES (66, '筛查人员管理查', NULL, 'get:/management/screeningOrganizationStaff/**', 0, 0, 1, 4, 1, '2021-02-01 04:03:57', '2021-02-01 04:03:57');
INSERT INTO `o_permission` VALUES (67, '消息列表', NULL, 'get:/management/notice/**', 0, 0, 1, 34, 1, '2021-02-01 04:04:59', '2021-02-01 04:04:59');
INSERT INTO `o_permission` VALUES (68, '已读消息', NULL, 'post:/management/notice/**', 0, 0, 1, 34, 1, '2021-02-01 04:04:59', '2021-02-01 04:04:59');
INSERT INTO `o_permission` VALUES (69, '删除消息', NULL, 'delete:/management/notice/**', 0, 0, 1, 34, 1, '2021-02-01 04:04:59', '2021-02-01 04:04:59');
INSERT INTO `o_permission` VALUES (70, '模板学生查', NULL, 'get:/management/studentArchives/**', 0, 0, 1, 17, 1, '2021-02-01 04:08:48', '2021-02-01 04:08:48');
INSERT INTO `o_permission` VALUES (71, '模板学生改', NULL, 'put:/management/studentArchives/**', 0, 0, 1, 17, 1, '2021-02-01 04:08:48', '2021-02-01 04:08:48');
INSERT INTO `o_permission` VALUES (72, '模板筛查机构查', NULL, 'get:/management/screeningReport/**', 0, 0, 1, 18, 1, '2021-02-01 04:08:48', '2021-02-01 04:09:12');
INSERT INTO `o_permission` VALUES (73, '模板筛查机构改', NULL, 'put:/management/screeningReport/**', 0, 0, 1, 18, 1, '2021-02-01 04:08:48', '2021-02-01 04:09:12');
INSERT INTO `o_permission` VALUES (74, '删除页面功能权限资源', NULL, 'delete:/management/permission/**', 0, 0, 1, 15, 1, '2020-12-26 12:28:56', '2020-12-27 09:30:04');
INSERT INTO `o_permission` VALUES (75, '编辑|移动页面功能权限资源', NULL, 'put:/management/permission/**', 0, 0, 2, 15, 1, '2020-12-26 12:30:17', '2020-12-27 09:30:07');
INSERT INTO `o_permission` VALUES (76, '新增页面功能权限资源', NULL, 'post:/management/permission', 0, 0, 3, 15, 1, '2020-12-26 12:30:36', '2020-12-26 21:30:09');
INSERT INTO `o_permission` VALUES (77, '获取页面功能权限资源列表', NULL, 'get:/management/permission/list', 0, 0, 4, 15, 1, '2020-12-26 12:30:51', '2020-12-26 21:30:12');
INSERT INTO `o_permission` VALUES (78, '重置密码', NULL, 'put:/management/user/password/**', 0, 0, 1, 22, 1, '2020-12-26 12:15:45', '2021-01-18 10:49:07');
INSERT INTO `o_permission` VALUES (79, '编辑|停用|启动用户', NULL, 'put:/management/user', 0, 0, 2, 22, 1, '2020-12-26 12:17:19', '2021-01-18 10:49:08');
INSERT INTO `o_permission` VALUES (80, '新增用户', 'userCreate', 'post:/management/user', 0, 0, 3, 22, 1, '2020-12-26 12:17:37', '2021-02-01 16:03:09');
INSERT INTO `o_permission` VALUES (81, '获取用户列表【分页】', NULL, 'get:/management/user/list', 0, 0, 4, 22, 1, '2020-12-26 12:17:51', '2021-01-18 10:49:13');
INSERT INTO `o_permission` VALUES (82, '获取用户详情', NULL, 'get:/management/user/**', 0, 0, 5, 22, 1, '2020-12-26 12:17:51', '2021-01-18 10:49:20');
INSERT INTO `o_permission` VALUES (83, '获取指定行政区的上级部门列表', NULL, 'get:/management/govDept/superior/**', 0, 0, 7, 20, 1, '2020-12-30 22:21:31', '2021-01-18 10:53:04');
INSERT INTO `o_permission` VALUES (84, '获取部门详情', NULL, 'get:/management/govDept/**', 0, 0, 1, 20, 1, '2020-12-26 12:23:46', '2021-01-18 10:50:37');
INSERT INTO `o_permission` VALUES (85, '获取部门架构树', NULL, 'get:/management/govDept/structure', 0, 0, 2, 20, 1, '2020-12-26 12:23:59', '2021-01-18 10:50:37');
INSERT INTO `o_permission` VALUES (86, '编辑|停用|启动部门', NULL, 'put:/management/govDept', 0, 0, 3, 20, 1, '2020-12-26 12:24:26', '2021-01-18 10:50:37');
INSERT INTO `o_permission` VALUES (87, '新增部门', 'departmentCreate', 'post:/management/govDept', 0, 0, 4, 20, 1, '2020-12-26 12:24:43', '2021-02-01 16:02:45');
INSERT INTO `o_permission` VALUES (88, '获取部门列表', NULL, 'get:/management/govDept/list', 0, 0, 5, 20, 1, '2020-12-26 12:25:02', '2021-01-18 10:50:37');
INSERT INTO `o_permission` VALUES (89, '获取行政架构树', NULL, 'get:/management/district/structure', 0, 0, 6, 20, 1, '2020-12-26 12:25:36', '2021-01-18 10:51:56');
INSERT INTO `o_permission` VALUES (90, '获取角色页面功能权限资源树', NULL, 'get:/management/role/permission/structure/**', 0, 0, 1, 21, 1, '2020-12-26 12:26:20', '2021-01-18 10:52:41');
INSERT INTO `o_permission` VALUES (91, '给角色分配权限', NULL, 'post:/management/role/permission/**', 0, 0, 2, 21, 1, '2020-12-26 12:26:40', '2021-01-18 10:52:41');
INSERT INTO `o_permission` VALUES (92, '编辑|停用|启动角色', NULL, 'put:/management/role', 0, 0, 3, 21, 1, '2020-12-26 12:27:47', '2021-01-18 10:52:41');
INSERT INTO `o_permission` VALUES (93, '新增角色', 'roleCreate', 'post:/management/role', 0, 0, 4, 21, 1, '2020-12-26 12:28:05', '2021-01-31 22:53:07');
INSERT INTO `o_permission` VALUES (94, '获取角色列表', NULL, 'get:/management/role/list', 0, 0, 5, 21, 1, '2020-12-26 12:28:19', '2021-01-18 10:52:41');
INSERT INTO `o_permission` VALUES (95, '获取权限集合包', NULL, 'get:/management/permission/template/**', 0, 0, 1, 16, 1, '2021-01-20 17:47:10', '2021-01-20 17:47:10');
INSERT INTO `o_permission` VALUES (96, '更新权限集合包', NULL, 'put:/management/permission/template/**', 0, 0, 2, 16, 1, '2021-01-20 17:48:18', '2021-01-20 17:48:18');
INSERT INTO `o_permission` VALUES (97, '获取全国行政区域-树结构', NULL, 'get:/management/district/all', 0, 0, 3, 16, 1, '2021-01-22 18:08:28', '2021-01-22 18:08:28');
INSERT INTO `o_permission` VALUES (98, '获取当前登录用户所属层级 - 层级链(从省开始到所属层级)', NULL, 'get:/management/district/current/position', 0, 0, 4, 16, 1, '2021-01-22 18:08:47', '2021-01-22 18:08:47');
INSERT INTO `o_permission` VALUES (99, '获取指定行政区域的所有下级区域', NULL, 'get:/management/district/child/**', 0, 0, 5, 16, 1, '2021-01-22 18:09:31', '2021-01-22 18:09:31');
INSERT INTO `o_permission` VALUES (100, '停用|启用部门', NULL, 'put:/management/govDept/**/**', 0, 0, 1, 20, 1, '2021-01-25 17:24:32', '2021-01-25 17:24:32');
INSERT INTO `o_permission` VALUES (101, '停用|启用角色', NULL, 'put:/management/role/**/**', 0, 0, 1, 21, 1, '2021-01-25 17:25:57', '2021-01-25 17:25:57');
INSERT INTO `o_permission` VALUES (102, '停用|启用用户', NULL, 'put:/management/user/**/**', 0, 0, 1, 22, 1, '2021-01-25 17:25:57', '2021-01-25 17:25:57');
INSERT INTO `o_permission` VALUES (103, '退出登录', NULL, 'post:/auth/exit', 0, 0, 4, 13, 1, '2021-01-18 01:22:31', '2021-01-18 01:53:22');
INSERT INTO `o_permission` VALUES (104, '学生导出', 'studentExport', 'get:/management/student/export', 0, 0, 1, 10, 1, '2021-02-01 06:57:26', '2021-02-01 16:07:18');
INSERT INTO `o_permission` VALUES (105, '学生导入模板', NULL, 'get:/management/student/import/demo', 0, 0, 1, 10, 1, '2021-02-01 06:57:26', '2021-02-01 06:57:26');
INSERT INTO `o_permission` VALUES (106, '学生导入', NULL, 'post:/management/student/import', 0, 0, 1, 10, 1, '2021-02-01 06:57:26', '2021-02-01 06:57:26');
INSERT INTO `o_permission` VALUES (107, '筛查机构导出', 'organizationExport', 'get:/management/screeningOrganization/export', 0, 0, 1, 2, 1, '2021-02-01 06:57:26', '2021-02-01 16:55:46');
INSERT INTO `o_permission` VALUES (108, '筛查人员导出', 'screeningStaffExport', 'get:/management/screeningOrganizationStaff/export', 0, 0, 1, 4, 1, '2021-02-01 06:57:26', '2021-02-01 16:30:49');
INSERT INTO `o_permission` VALUES (109, '筛查人员导入模板', NULL, 'get:/management/screeningOrganizationStaff/import/demo', 0, 0, 1, 4, 1, '2021-02-01 06:57:26', '2021-02-01 06:57:26');
INSERT INTO `o_permission` VALUES (110, '筛查人员导入', 'screeningStaffImport', 'post:/management/screeningOrganizationStaff/import', 0, 0, 1, 4, 1, '2021-02-01 06:57:26', '2021-02-01 06:57:26');
INSERT INTO `o_permission` VALUES (111, '医院导出', 'hospitalExport', 'get:/management/hospital/export', 0, 0, 1, 12, 1, '2021-02-01 06:57:26', '2021-02-01 16:09:57');
INSERT INTO `o_permission` VALUES (112, '学校导出', 'schoolExport', 'get:/management/school/export', 0, 0, 1, 7, 1, '2021-02-01 06:57:26', '2021-02-01 16:08:50');
INSERT INTO `o_permission` VALUES (113, '学校编码规则', 'schoolNoRules', NULL, 0, 1, 1, 7, 1, '2021-02-02 11:51:41', '2021-02-02 20:33:55');
INSERT INTO `o_permission` VALUES (114, '用户详情', NULL, 'get:/management/user/**', 0, 0, 1, 13, 1, '2021-02-02 16:39:25', '2021-02-02 20:34:01');
INSERT INTO `o_permission` VALUES (173, '通用获取影像连接接口', 'fileUrl', 'get:/management/common/file/**', 0, 1, 1, 0, 1, '2021-02-09 23:07:41', '2021-02-10 10:42:09');
INSERT INTO `o_permission` VALUES (172, '通用上传富文本影像接口', 'riceTextUpload', 'post:/management/common/richTextFileUpload', 0, 1, 1, 0, 1, '2021-02-09 23:06:25', '2021-02-10 10:42:09');
INSERT INTO `o_permission` VALUES (171, '通用上传影像接口', 'upload', 'post:/management/common/fileUpload', 0, 1, 1, 0, 1, '2021-02-09 23:04:18', '2021-02-10 10:42:09');
INSERT INTO `o_permission` VALUES (170, '导出筛查学生二维码/告知书', 'demo', 'get:/management/screeningPlan/export/**', 0, 0, 1, 27, 1, '2021-02-09 18:25:49', '2021-02-09 18:25:49');
INSERT INTO `o_permission` VALUES (169, '下载筛查学生数据的上传模板', 'demo', 'get:/management/student/import/demo', 0, 0, 1, 27, 1, '2021-02-09 18:25:49', '2021-02-09 18:25:49');

-- 初始化用户数据 - 超级管理员（密码：123456）
-- 用户（org_id需要与business服务中的运营中心部门的ID保持一致，默认为1）
INSERT INTO `o_user`(`id`, `org_id`, `real_name`, `username`, `password`, `system_code`, `status`, `user_type`) VALUES (1, 1, '超级管理员', 'admin', '$2a$10$XiLyxJhjcGEkMKG7KVY/XuGu2cHxPp5S89AuyKh50.Gyl1OHUWQlq', 1, 0, 0);

-- 角色（org_id需要与运营中心部门的ID保持一致，默认为1）
INSERT INTO `o_role`(`id`, `org_id`, `en_name`, `ch_name`, `role_type`, `system_code`, `status`) VALUES (1, 1, 'admin', '超级管理员', 0, 1, 0);

-- 用户角色
INSERT INTO `o_user_role`(`user_id`, `role_id`) VALUES (1, 1);

-- 角色权限
INSERT INTO `o_role_permission`(`role_id`, `permission_id`) SELECT 1, id FROM `o_permission`;

-- 客户端（默认token有效期为2小时，refreshToken有效期为4小时）
INSERT INTO `oauth_client_details` ( `client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove` )
VALUES
       ( '1', NULL, '123456', 'all', 'password,refresh_token', NULL, NULL, 7200, 14400, '管理端', NULL ),
       ( '2', NULL, '123456', 'all', 'password,refresh_token', NULL, NULL, 7200, 14400, '学校端', NULL ),
       ( '3', NULL, '123456', 'all', 'password,refresh_token', NULL, NULL, 7200, 14400, '筛查端', NULL ),
       ( '4', NULL, '123456', 'all', 'password,refresh_token', NULL, NULL, 7200, 14400, '医院端', NULL ),
       ( '5', NULL, '123456', 'all', 'password,refresh_token', NULL, NULL, 7200, 14400, '家长端', NULL ),
       ( '6', NULL, '123456', 'all', 'password,refresh_token', NULL, NULL, 7200, 14400, '筛查管理端', NULL );
