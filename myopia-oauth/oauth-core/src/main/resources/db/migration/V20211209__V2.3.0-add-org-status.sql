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

-- 数据迁移，生成机构数据
INSERT INTO o_organization(org_id, system_code, user_type)
SELECT org_id, system_code, user_type
FROM o_user
GROUP BY org_id, system_code, user_type
HAVING system_code in (1,2);