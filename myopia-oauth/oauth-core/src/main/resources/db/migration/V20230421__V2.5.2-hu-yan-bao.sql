INSERT INTO oauth_client_details (client_id, client_secret, scope, authorized_grant_types, access_token_validity,
                                  refresh_token_validity, additional_information)
VALUES ('9', '123456', 'all', 'password,refresh_token', 86400, 100800, '第三方平台');

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1150, '第三方平台', 'thirdPartyPlatform', null, 0, 1, 0, 0, 1),
       (1151, '护眼宝', 'huyan', null, 0, 1, 0, 1150, 1),
       (1152, '家长端推送学生数据', 'huyanPush', 'post:/parent/hyb/push', 0, 0, 0, 1151, 1),
       (1153, '护眼宝绑定回调', 'hybBindCallback', 'post:/parent/hyb/bind/callback', 0, 0, 0, 1151, 1);