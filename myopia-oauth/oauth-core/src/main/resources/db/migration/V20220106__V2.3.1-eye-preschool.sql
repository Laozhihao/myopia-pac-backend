INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (564, '0-6岁眼保健检查（按钮）', 'bChildEyeHealthy', 'get:/management/preschool/check/list', 0, 0, 0, 12, 1);

INSERT INTO `oauth_client_details`(`client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove`)
VALUES ('7', NULL, '123456', 'all', 'password,refresh_token', NULL, NULL, 7200, 14400, '0-6岁', NULL);
