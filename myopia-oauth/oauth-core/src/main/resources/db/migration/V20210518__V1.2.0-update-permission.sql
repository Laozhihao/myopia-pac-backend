UPDATE `o_permission` SET `api_url` = 'put:/management/hospital', `name` = '医院编辑' WHERE `id` = '37';
INSERT INTO `o_permission` (`name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES ('停用/启用', 'hospitalStatusUpdate', 'put:/management/hospital/status', '0', '0', '1', '12', '1'),
('重置密码', 'hospitalPasswordReset', 'post:/management/hospital/reset', '0', '0', '1', '12', '1');

UPDATE `o_permission` SET `api_url` = 'put:/management/school', `name` = '学校编辑' WHERE `id` = '41';
INSERT INTO `o_permission` (`name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES ('停用/启用', 'schoolStatusUpdate', 'put:/management/school/status', '0', '0', '1', '7', '1'),
('重置密码', 'schoolPasswordReset', 'post:/management/school/reset', '0', '0', '1', '7', '1');

UPDATE `o_permission` SET `api_url` = 'put:/management/screeningOrganization', `name` = '筛查机构编辑' WHERE `id` = '61';
INSERT INTO `o_permission` (`name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES ('停用/启用', 'screeningOrganizationStatusUpdate', 'put:/management/screeningOrganization/status', '0', '0', '1', '2', '1'),
('重置密码', 'screeningOrganizationPasswordReset', 'post:/management/screeningOrganization/reset', '0', '0', '1', '2', '1');