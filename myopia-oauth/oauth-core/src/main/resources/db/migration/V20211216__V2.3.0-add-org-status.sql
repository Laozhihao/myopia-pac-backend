-- 增加o_organization信息表
CREATE TABLE `o_organization`  (
  `org_id` int(11) NOT NULL COMMENT '机构组织ID（如政府部门ID、学校ID、医院ID）',
  `system_code` tinyint(1) NOT NULL COMMENT '系统编号',
  `user_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '用户类型：0-平台管理员、1-政府人员、2-筛查机构、3-医院管理员',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '组织状态：0-启用 1-禁止 2-删除',
  PRIMARY KEY (`system_code`, `org_id`, `user_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'org基本信息表' ROW_FORMAT = Dynamic;

-- 修改user_type默认值
ALTER TABLE `o_user`
MODIFY COLUMN `user_type` tinyint(1) NOT NULL DEFAULT -1 COMMENT '用户类型：0-平台管理员、1-非平台管理员' AFTER `last_login_time`;

-- 数据迁移，修正原数据
update o_user set user_type = -1 where system_code = 2;
update o_user set user_type = -1 where system_code = 3;
update o_user set system_code = 1, user_type = 3 where system_code = 4;
update o_user set user_type = -1 where system_code = 5;
update o_user set system_code = 1, user_type = 2 where system_code = 6;

update o_role set system_code = 1 where system_code = 6;
update o_permission set system_code = 1 where system_code = 6;

-- 数据迁移，生成机构数据
INSERT INTO o_organization(org_id, system_code, user_type, `status`)
SELECT org_id, system_code, user_type, 1
FROM o_user
GROUP BY org_id, system_code, user_type
HAVING system_code = 2;

INSERT INTO o_organization(org_id, system_code, user_type, `status`)
SELECT org_id, system_code, user_type, 0
FROM o_user
GROUP BY org_id, system_code, user_type
HAVING system_code = 1
AND user_type in (0,1,2,3);

-- 增加相关权限信息
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (500, '工作台（一级）', 'workbench', null, 1, 1, 1, 0, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (501, '患者管理', 'patient', null, 1, 1, 1, 500, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (502, '医生管理', 'doctor', null, 1, 1, 2, 500, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (503, '0-6眼保健检查记录', 'eyeInspect', null, 1, 1, 3, 500, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (504, '眼健康就诊记录', 'eyeHealthy', null, 1, 1, 4, 500, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (505, '就诊记录', 'diagnosisRecord', null, 1, 1, 1, 501, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (506, '获取医院学生列表', 'HospitalStudentList', 'get:/management/hospital/workbench/patient/list', 0, 0, 0, 501, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (507, '删除医院学生', 'deletedHospitalStudentId', 'delete:/management/hospital/workbench/patient/**', 0, 0, 0, 501, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (508, '通过Id获取医院学生', 'getByHospitalStudentId', 'get:/management/hospital/workbench/patient/**', 0, 0, 0, 505, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (509, '根据指定code，获取其下级行政区域集', 'getDistrictCode', 'get:/management/hospital/workbench/patient/child/district/**', 0, 0, 0, 505, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (510, '通过名字获取学校列表', 'getBySchoolName', 'get:/management/school/getSchools/**', 0, 0, 0, 505, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (511, '获取班级', 'getGradeInfo', 'get:/management/schoolGrade/all', 0, 0, 0, 505, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (512, '更新资料', 'update', 'put:/management/hospital/workbench/patient', 0, 0, 0, 505, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (513, '获取医生列表', 'getDoctorList', 'get:/management/doctor/list', 0, 0, 1, 502, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (514, '获取医生详情', 'getDoctorDetails', 'get:/management/doctor/**', 0, 0, 2, 502, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (515, '添加医生', 'addDoctor', 'post:/management/doctor', 0, 0, 3, 502, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (516, '更新医生', 'updateDoctor', 'put:/management/doctor', 0, 0, 4, 502, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (517, '更新医生状态', 'updateDoctorStatus', 'put:/management/doctor/status', 0, 0, 5, 502, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (518, '重置医生密码', 'resetDoctorPassword', 'put:/management/doctor/reset', 0, 0, 6, 502, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (519, '患者管理（三级）', 'tPatient', null, 1, 1, 0, 12, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (520, '医生管理（三级）', 'tDoctor', null, 1, 1, 0, 12, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (521, '0-6眼保健检查记录', 'tEyeInspect', null, 1, 1, 3, 12, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (522, '眼健康就诊记录', 'tEyeHealthy', null, 1, 1, 4, 12, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (523, '就诊记录', 'tDiagnosisRecord', null, 1, 1, 1, 519, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (524, '获取医院学生列表', 'tHospitalStudentList', 'get:/management/hospital/workbench/patient/list', 0, 0, 0, 519, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (525, '删除医院学生', 'tDeletedHospitalStudentId', 'delete:/management/hospital/workbench/patient/**', 0, 0, 0, 519, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (526, '通过Id获取医院学生', 'tGetByHospitalStudentId', 'get:/management/hospital/workbench/patient/**', 0, 0, 0, 523, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (527, '根据指定code，获取其下级行政区域集', 'tGetDistrictCode', 'get:/management/hospital/workbench/patient/child/district/**', 0, 0, 0, 523, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (528, '通过名字获取学校列表', 'tGetBySchoolName', 'get:/management/school/getSchools/**', 0, 0, 0, 523, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (529, '获取班级', 'tGetGradeInfo', 'get:/management/schoolGrade/all', 0, 0, 0, 523, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (530, '更新资料', 'tUpdate', 'put:/management/hospital/workbench/patient', 0, 0, 0, 523, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (531, '获取医生列表', 'tGetDoctorList', 'get:/management/doctor/list', 0, 0, 1, 520, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (532, '获取医生详情', 'tGetDoctorDetails', 'get:/management/doctor/**', 0, 0, 2, 520, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (533, '添加医生', 'tAddDoctor', 'post:/management/doctor', 0, 0, 3, 520, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (534, '更新医生', 'tUpdateDoctor', 'put:/management/doctor', 0, 0, 4, 520, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (535, '更新医生状态', 'tUpdateDoctorStatus', 'put:/management/doctor/status', 0, 0, 5, 520, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (536, '重置医生密码', 'tResetDoctorPassword', 'put:/management/doctor/reset', 0, 0, 6, 520, 1);

-- 0-6系统APP权限
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (537, '0-6岁系统（APP）', 'appPreschool', null, 1, 1, 0, 0, 4);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (538, '获取', 'appGetPreschool', 'get:/preschool/app/**', 0, 0, 1, 537, 4);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (539, '增加', 'appPostPreschool', 'post:/preschool/app/**', 0, 0, 2, 537, 4);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (540, '更新', 'appPutPreschool', 'put:/preschool/app/**', 0, 0, 3, 537, 4);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (541, '删除', 'appDeletePreschool', 'delete:/preschool/app/**', 0, 0, 4, 537, 4);

-- 0-6系统APP权限
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (542, '居民健康系统（APP）', 'appHospital', null, 1, 1, 0, 0, 4);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (543, '获取', 'appGetHospital', 'get:/hospital/app/**', 0, 0, 1, 542, 4);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (544, '增加', 'appPostHospital', 'post:/hospital/app/**', 0, 0, 2, 542, 4);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (545, '更新', 'appPutHospital', 'put:/hospital/app/**', 0, 0, 3, 542, 4);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (546, '删除', 'appDeleteHospital', 'delete:/hospital/app/**', 0, 0, 4, 542, 4);

INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (547, '医院账号历史问题处理', 'dealHistoryData', 'post:/management/hospital/dealHistoryData', 0, 0, 2, 294, 1);
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (548, '医生账号历史问题处理', 'doctorRepair', 'post:/management/doctor/repair', 0, 0, 3, 294, 1);

INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (549, '医生管理（按钮）', 'bDoctor', 'get:/management/doctor/list', 0, 0, 2, 12, 1);
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (550, '患者管理（按钮）', 'bPatient', 'get:/management/hospital/workbench/patient/list', 0, 0, 1, 12, 1);


-- 初始化医生角色
INSERT INTO `o_role` ( `org_id`, `ch_name`, `role_type`, `create_user_id`, `system_code` ) VALUES
( -1, '居民健康医生类型角色', 5, 1, 4 ),
( -1, '0-6岁眼检查医生类型角色', 6, 1, 4 );

-- 菜单栏调整
UPDATE o_permission set pid = 500 where id = 5;
UPDATE o_permission set pid = 500 where id = 6;

