-- ID从1000开始，袁杭：1000~1009，新谋：1010~1019
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1010, '视力小队', 'visionTeam', null, 1, 1, 1, 7, 1),
       (1011, '获取学校员工列表', 'schoolStaffList', 'get:/management/school/staff/list/**', 0, 0, 0, 1010, 1),
       (1012, '保存员工', 'schoolStaffSave', 'post:/management/school/staff/save/**', 0, 0, 0, 1010, 1),
       (1013, '启用/停用', 'schoolStaffEditStatus', 'post:/management/school/staff/editStatus/**/**', 0, 0, 0, 1010, 1),
       (1014, '重置密码', 'schoolStaffReset', 'post:/management/school/staff/resetPassword/**', 0, 0, 0, 1010, 1),
       (1015, '是否超过人数配置', 'isMoreThanConfig', 'get:/management/school/staff/checkTeamCount/**', 0, 0, 0, 1010,1),
       (1016, '视力小队按钮', 'visionTeamBtn', null, 0, 0, 0, 7, 1);

-- 将筛查机构的筛查人员的user_type改为0
update o_user
set user_type = 0
where system_code = 3;
