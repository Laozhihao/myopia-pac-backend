INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`, `create_time`, `update_time`) VALUES
(400, 'APP版本管理', 'appUpdateManagement', NULL, 1, 1, 5, 14, 1, '2021-11-23 15:57:09', '2021-11-25 15:48:19'),
(401, '获取最新版本', 'getLatestAppVersion', 'get:/management/common/app/version/latest', 0, 0, 5, 400, 1, '2021-11-25 10:44:01', '2021-11-25 12:32:40'),
(402, '停用|启用-APP版本', 'updateAppVersionStatus', 'put:/management/app/version/status', 0, 0, 4, 400, 1, '2021-11-25 10:42:33', '2021-11-25 10:42:33'),
(403, '编辑-APP版本', 'updateAppVersion', 'put:/management/app/version', 0, 0, 3, 400, 1, '2021-11-25 10:28:30', '2021-11-25 10:28:30'),
(404, '获取APP版本列表（分页）', 'getAppVersionListPage', 'get:/management/app/version/page', 0, 0, 2, 400, 1, '2021-11-25 10:27:35', '2021-11-25 10:28:42'),
(405, '新增-APP版本', 'addAppVersion', 'post:/management/app/version', 0, 0, 1, 400, 1, '2021-11-23 15:57:58', '2021-11-25 10:26:41');
;
