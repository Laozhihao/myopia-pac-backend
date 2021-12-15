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
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES
(412, '工作台', 'workbench', NULL, 1, 1, 1, 0, 1),
(413, '患者管理', 'patient', NULL, 1, 1, 1, 412, 1),
(414, '医生管理', 'doctor', NULL, 1, 1, 2, 412, 1),
(415, '就诊记录', 'diagnosisRecord', NULL, 1, 1, 1, 413, 1),

(416, '获取医生列表', 'getDoctorList', 'get:/management/doctor/list', 0, 0, 1, 414, 1),
(417, '获取医生详情', 'getDoctorDetails', 'get:/management/doctor/**', 0, 0, 2, 414, 1),
(418, '添加医生', 'addDoctor', 'post:/management/doctor', 0, 0, 3, 414, 1),
(419, '更新医生', 'updateDoctor', 'put:/management/doctor', 0, 0, 4, 414, 1),
(420, '更新医生状态', 'updateDoctorStatus', 'put:/management/doctor/status', 0, 0, 5, 414, 1),
(421, '重置医生密码', 'resetDoctorPassword', 'put:/management/doctor/reset', 0, 0, 6, 414, 1);


-- 初始化医生角色
INSERT INTO `o_role` ( `org_id`, `ch_name`, `role_type`, `create_user_id`, `system_code` ) VALUES
( -1, '居民健康医生类型角色', 5, 1, 4 ),
( -1, '0-6岁眼检查医生类型角色', 6, 1, 4 );

