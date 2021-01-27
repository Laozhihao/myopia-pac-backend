-- 初始化权限资源
INSERT INTO `o_permission` VALUES (1, '用户管理', NULL, NULL, 1, 1, 1, 0, 1, now(), now());
INSERT INTO `o_permission` VALUES (2, '部门管理', 'departmentManager', NULL, 1, 1, 1, 1, 1, now(), now());
INSERT INTO `o_permission` VALUES (3, '角色管理', 'roleManager', NULL, 1, 1, 2, 1, 1, now(), now());
INSERT INTO `o_permission` VALUES (4, '用户管理', 'usersManager', NULL, 1, 1, 3, 1, 1, now(), now());
INSERT INTO `o_permission` VALUES (5, '系统中心', NULL, NULL, 1, 1, 2, 0, 1, now(), now());
INSERT INTO `o_permission` VALUES (6, '页面资源设置', 'pageSourceSetting', NULL, 1, 1, 1, 5, 1, now(), now());
INSERT INTO `o_permission` VALUES (7, '重置密码', NULL, 'put:/management/user/password/**', 0, 0, 1, 4, 1, now(), now());
INSERT INTO `o_permission` VALUES (8, '编辑|停用|启动用户', NULL, 'put:/management/user', 0, 0, 2, 4, 1, now(), now());
INSERT INTO `o_permission` VALUES (9, '新增用户', NULL, 'post:/management/user', 0, 0, 3, 4, 1, now(), now());
INSERT INTO `o_permission` VALUES (10, '获取用户列表【分页】', NULL, 'get:/management/user/list', 0, 0, 4, 4, 1, now(), now());
INSERT INTO `o_permission` VALUES (11, '获取用户详情', NULL, 'get:/management/user/**', 0, 0, 5, 4, 1, now(), now());
INSERT INTO `o_permission` VALUES (12, '获取指定行政区的上级部门列表', NULL, 'get:/management/govDept/superior/**', 0, 0, 7, 2, 1, now(), now());
INSERT INTO `o_permission` VALUES (13, '获取部门详情', NULL, 'get:/management/govDept/**', 0, 0, 1, 2, 1, now(), now());
INSERT INTO `o_permission` VALUES (14, '获取部门架构树', NULL, 'get:/management/govDept/structure', 0, 0, 2, 2, 1, now(), now());
INSERT INTO `o_permission` VALUES (15, '编辑|停用|启动部门', NULL, 'put:/management/govDept', 0, 0, 3, 2, 1, now(), now());
INSERT INTO `o_permission` VALUES (16, '新增部门', NULL, 'post:/management/govDept', 0, 0, 4, 2, 1, now(), now());
INSERT INTO `o_permission` VALUES (17, '获取部门列表', NULL, 'get:/management/govDept/list', 0, 0, 5, 2, 1, now(), now());
INSERT INTO `o_permission` VALUES (18, '获取行政架构树', NULL, 'get:/management/district/structure', 0, 0, 6, 2, 1, now(), now());
INSERT INTO `o_permission` VALUES (19, '获取角色页面功能权限资源树', NULL, 'get:/management/role/permission/structure/**', 0, 0, 1, 3, 1, now(), now());
INSERT INTO `o_permission` VALUES (20, '给角色分配权限', NULL, 'post:/management/role/permission/**', 0, 0, 2, 3, 1, now(), now());
INSERT INTO `o_permission` VALUES (21, '编辑|停用|启动角色', NULL, 'put:/management/role', 0, 0, 3, 3, 1, now(), now());
INSERT INTO `o_permission` VALUES (22, '新增角色', NULL, 'post:/management/role', 0, 0, 4, 3, 1, now(), now());
INSERT INTO `o_permission` VALUES (23, '获取角色列表', NULL, 'get:/management/role/list', 0, 0, 5, 3, 1, now(), now());
INSERT INTO `o_permission` VALUES (24, '删除页面功能权限资源', NULL, 'delete:/management/permission/**', 0, 0, 1, 6, 1, now(), now());
INSERT INTO `o_permission` VALUES (25, '编辑|移动页面功能权限资源', NULL, 'put:/management/permission/**', 0, 0, 2, 6, 1, now(), now());
INSERT INTO `o_permission` VALUES (26, '新增页面功能权限资源', NULL, 'post:/management/permission', 0, 0, 3, 6, 1, now(), now());
INSERT INTO `o_permission` VALUES (27, '获取页面功能权限资源列表', NULL, 'get:/management/permission/list', 0, 0, 4, 6, 1, now(), now());
INSERT INTO `o_permission` VALUES (28, '退出登录', NULL, 'post:/auth/exit', 0, 0, 3, 5, 1, now(), now());
INSERT INTO `o_permission` VALUES (29, '权限集合包设置', 'permissionsGather', NULL, 1, 1, 2, 5, 1, now(), now());

-- 初始化用户数据 - 超级管理员（密码：123456）
-- 角色和用户的orgId值要与business服务中的运营中心部门ID（见对应服务的初始化SQL）一致
INSERT INTO `o_role` VALUES (1, 1, 'admin', '超级管理员', 0, -1, 1, 0, NULL, now(), now());
INSERT INTO `o_user` VALUES (1, 1, '超级管理员', 1, NULL, NULL, 'admin', '$2a$10$XiLyxJhjcGEkMKG7KVY/XuGu2cHxPp5S89AuyKh50.Gyl1OHUWQlq', 0, 1, -1, 0, NULL, now(), now(), NULL);
INSERT INTO `o_user_role`(`user_id`, `role_id`) VALUES (1, 1);

INSERT INTO `o_role_permission` VALUES (1, 1);
INSERT INTO `o_role_permission` VALUES (1, 2);
INSERT INTO `o_role_permission` VALUES (1, 3);
INSERT INTO `o_role_permission` VALUES (1, 4);
INSERT INTO `o_role_permission` VALUES (1, 5);
INSERT INTO `o_role_permission` VALUES (1, 6);
INSERT INTO `o_role_permission` VALUES (1, 7);
INSERT INTO `o_role_permission` VALUES (1, 8);
INSERT INTO `o_role_permission` VALUES (1, 9);
INSERT INTO `o_role_permission` VALUES (1, 10);
INSERT INTO `o_role_permission` VALUES (1, 11);
INSERT INTO `o_role_permission` VALUES (1, 12);
INSERT INTO `o_role_permission` VALUES (1, 13);
INSERT INTO `o_role_permission` VALUES (1, 14);
INSERT INTO `o_role_permission` VALUES (1, 15);
INSERT INTO `o_role_permission` VALUES (1, 16);
INSERT INTO `o_role_permission` VALUES (1, 17);
INSERT INTO `o_role_permission` VALUES (1, 18);
INSERT INTO `o_role_permission` VALUES (1, 19);
INSERT INTO `o_role_permission` VALUES (1, 20);
INSERT INTO `o_role_permission` VALUES (1, 21);
INSERT INTO `o_role_permission` VALUES (1, 22);
INSERT INTO `o_role_permission` VALUES (1, 23);
INSERT INTO `o_role_permission` VALUES (1, 24);
INSERT INTO `o_role_permission` VALUES (1, 25);
INSERT INTO `o_role_permission` VALUES (1, 26);
INSERT INTO `o_role_permission` VALUES (1, 27);
INSERT INTO `o_role_permission` VALUES (1, 28);
INSERT INTO `o_role_permission` VALUES (1, 29);

-- 初始化client表（默认token有效期为2小时，refreshToken有效期为4小时）
INSERT INTO `oauth_client_details` VALUES ('1', NULL, '123456', 'all', 'password,refresh_token', NULL, NULL, 7200, 14400, '管理端', NULL);
INSERT INTO `oauth_client_details` VALUES ('2', NULL, '123456', 'all', 'password,refresh_token', NULL, NULL, 7200, 14400, '学校端', NULL);
INSERT INTO `oauth_client_details` VALUES ('3', NULL, '123456', 'all', 'password,refresh_token', NULL, NULL, 7200, 14400, '筛查端', NULL);
INSERT INTO `oauth_client_details` VALUES ('4', NULL, '123456', 'all', 'password,refresh_token', NULL, NULL, 7200, 14400, '医院端', NULL);
INSERT INTO `oauth_client_details` VALUES ('5', NULL, '123456', 'all', 'password,refresh_token', NULL, NULL, 7200, 14400, '家长端', NULL);
INSERT INTO `oauth_client_details` VALUES ('6', NULL, '123456', 'all', 'password,refresh_token', NULL, NULL, 7200, 14400, '筛查管理端', NULL);
