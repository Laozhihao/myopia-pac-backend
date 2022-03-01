INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code, create_time, update_time)
VALUES (690, '筛查二维码', 'screeningQrcodePage', null, 1, 1, 0, 1, 1, '2022-03-01 15:29:48', '2022-03-01 15:29:48'),
       (691, '虚拟二维码', 'virtualScreeningQrcode', null, 1, 1, 0, 690, 1, '2022-03-01 14:54:53', '2022-03-01 15:30:04'),
       (692, 'VS666筛查二维码', 'vsScreeningQrcode', null, 1, 1, 1, 690, 1, '2022-03-01 14:53:50', '2022-03-01 15:30:04'),
       (693, '普通筛查二维码', 'screeningQrcode', null, 1, 1, 0, 690, 1, '2022-03-01 14:52:54', '2022-03-01 15:30:04'),
       (694, '通过机构类型获取权限', 'OrganizationGetPermission', 'get:/management/screeningOrganization/getPermission/**', 0, 0, 1, 690, 1, '2022-03-01 15:41:23', '2022-03-01 15:41:23');
