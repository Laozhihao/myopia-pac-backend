INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1040, '视力小队', 'visionTeam', null, 1, 1, 1, 7, 1);

-- 将筛查机构的筛查人员的user_type改为0
update o_user
set user_type = 0
where system_code = 3;