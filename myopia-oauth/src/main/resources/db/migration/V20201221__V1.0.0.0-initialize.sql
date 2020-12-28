DROP TABLE IF EXISTS `o_district_permission`;
CREATE TABLE `o_district_permission` (
  `district_level` tinyint(1) NOT NULL COMMENT '行政区级别：0-省、1-市、2-区/县、3-镇',
  `permission_id` int(11) NOT NULL COMMENT '权限资源ID',
  PRIMARY KEY (`district_level`, `permission_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '行政区权限表' ROW_FORMAT = Dynamic;
DROP TABLE IF EXISTS `o_permission`;
CREATE TABLE `o_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '权限资源ID',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '权限资源名称',
  `menu_btn_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '对应页面或按钮的name（权限资源为页面时，该值不能为空）',
  `api_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '功能接口地址（权限资源为功能时，该值不能为空）',
  `is_menu` tinyint(1) NOT NULL COMMENT '是否为菜单：0-否、1-是',
  `is_page` tinyint(1) NOT NULL COMMENT '是否为页面：0-功能、1-页面',
  `order` tinyint(3) NOT NULL COMMENT '顺序',
  `pid` int(11) NOT NULL COMMENT '上级权限资源ID',
  `system_code` tinyint(1) NOT NULL COMMENT '系统编号',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '权限资源表' ROW_FORMAT = Dynamic;
DROP TABLE IF EXISTS `o_role`;
CREATE TABLE `o_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `org_id` int(11) NOT NULL COMMENT '机构组织ID（如政府部门ID、学校ID、医院ID）',
  `en_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '英文名',
  `ch_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '中文名',
  `role_type` tinyint(1) DEFAULT 0 COMMENT '角色类型：0-admin、1-机构管理员、2-普通用户',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建人',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态：0-启用 1-禁止 2-删除',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '备注',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;
DROP TABLE IF EXISTS `o_role_permission`;
CREATE TABLE `o_role_permission` (
  `role_id` int(11) NOT NULL COMMENT '角色ID',
  `permission_id` int(11) NOT NULL COMMENT '权限资源ID',
  PRIMARY KEY (`role_id`, `permission_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色权限表' ROW_FORMAT = Dynamic;
DROP TABLE IF EXISTS `o_user`;
CREATE TABLE `o_user` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `org_id` int(11) NOT NULL COMMENT '机构组织ID（如政府部门ID、学校ID、医院ID）',
  `real_name` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '真实姓名',
  `gender` tinyint(1) NOT NULL COMMENT '性别：0-男、1-女',
  `phone` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '手机号码',
  `id_card` varchar(18) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '身份证号码',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名（账号）',
  `password` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `is_leader` tinyint(1) DEFAULT 0 COMMENT '是否领导：0-否、1-是',
  `system_code` tinyint(1) NOT NULL COMMENT '系统编号',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建人',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态：0-启用 1-禁止 2-删除',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '备注',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;
DROP TABLE IF EXISTS `o_user_role`;
CREATE TABLE `o_user_role` (
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `role_id` int(11) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户角色表' ROW_FORMAT = Dynamic;
DROP TABLE IF EXISTS `oauth_client_details`;
CREATE TABLE `oauth_client_details` (
  `client_id` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `resource_ids` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `client_secret` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `scope` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `authorized_grant_types` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `web_server_redirect_uri` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `authorities` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `access_token_validity` int(0) DEFAULT NULL,
  `refresh_token_validity` int(0) DEFAULT NULL,
  `additional_information` varchar(4096) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `autoapprove` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  PRIMARY KEY (`client_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '客户端信息配置' ROW_FORMAT = Dynamic;
-- 初始化client表
INSERT INTO
  `oauth_client_details`
VALUES
  (
    '1',
    NULL,
    '123456',
    'all',
    'password,refresh_token',
    NULL,
    NULL,
    3600,
    7200,
    NULL,
    NULL
  );
INSERT INTO
  `oauth_client_details`
VALUES
  (
    '2',
    NULL,
    '123456',
    'all',
    'password,refresh_token',
    NULL,
    NULL,
    3600,
    7200,
    NULL,
    NULL
  );
-- 初始化用户表（密码加密算法Bcrypt）
INSERT INTO
  `o_user`(
    `org_id`,
    `real_name`,
    `gender`,
    `phone`,
    `id_card`,
    `username`,
    `password`,
    `is_leader`,
    `system_code`,
    `create_user_id`
  )
VALUES
  (
    1,
    '管理员',
    1,
    '18814182374',
    '56363',
    'admin',
    '$2a$10$XiLyxJhjcGEkMKG7KVY/XuGu2cHxPp5S89AuyKh50.Gyl1OHUWQlq',
    0,
    1,
    1
  );