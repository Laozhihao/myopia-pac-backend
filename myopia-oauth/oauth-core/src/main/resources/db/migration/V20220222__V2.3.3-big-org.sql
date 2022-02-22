INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (750, '数据总览机构管理', 'multiOverview', NULL, 1, 1, 7, 1, 1);
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (751, '总览机构列表', 'getOverviewList', 'get:/management/overview/list', 0, 0, 2, 750, 1);
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (752, '总览机构修改', 'updateOverview', 'put:/management/overview', 0, 0, 1, 750, 1);
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (753, '新增总览机构', 'addOverview', 'post:/management/overview', 0, 0, 1, 750, 1);
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (754, '查看总览机构账号', 'getOverviewAccountList', 'get:/management/overview/accountList/**', 0, 0, 1, 750, 1);
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (755, '增加总览机构账号', 'addOverviewAccount', 'post:/management/overview/add/account/**', 0, 0, 1, 750, 1);
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (756, '重置密码', 'resetOverviewAccountPwd', 'put:/management/overview/admin/status', 0, 0, 1, 750, 1);
INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES (757, '启用/停用', 'updateOverviewAccountStatus', '/management/overview/admin/reset', 0, 0, 1, 750, 1);
