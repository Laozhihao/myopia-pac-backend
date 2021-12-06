-- 增加org信息表
CREATE TABLE `o_organization`  (
  `org_id` int(11) NOT NULL COMMENT '机构组织ID（如政府部门ID、学校ID、医院ID）',
  `system_code` tinyint(1) NOT NULL COMMENT '系统编号',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '组织状态：0-启用 1-禁止 2-删除',
  `bind_org_id` int(11) NULL DEFAULT NULL COMMENT '关联机构组织ID（如政府部门ID、学校ID、医院ID）',
  `bind_system_code` tinyint(1) NULL DEFAULT NULL COMMENT '关联的组织系统编号',
  PRIMARY KEY (`system_code`, `org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'org基本信息表' ROW_FORMAT = Dynamic;

-- 旧数据迁移
INSERT INTO o_organization(org_id, system_code)
SELECT org_id, system_code
FROM o_user
GROUP BY org_id, system_code
HAVING org_id > 0;