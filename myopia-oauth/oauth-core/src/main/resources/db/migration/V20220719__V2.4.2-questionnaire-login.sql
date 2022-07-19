-- ID从900开始

INSERT INTO `oauth_client_details`(`client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove`)
VALUES ('8', NULL, '123456', 'all', 'password,refresh_token', NULL, NULL, 86400, 100800, '问卷系统端', NULL);


INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES ('906', '获取学校待办列表', 'getQuestionBacklogList', 'get:/management/questionnaire/backlog/list', '0', '0', '6', '900','1'),
       ('905', '获得学校问卷列表', 'getQuestionSchoolList', 'get:/management/questionnaire/schools/list', '0', '0', '5', '900','1'),
       ('904', '获得待办情况', 'getQuestionBacklog', 'get:/management/questionnaire/backlog', '0', '0', '4', '900', '1'),
       ('903', '获得问卷情况', 'getQuestionSchool', 'get:/management/questionnaire/school', '0', '0', '3', '900', '1'),
       ('902', '根据筛查任务Id获得下级区域', 'getQuestionTaskAreas', 'get:/management/questionnaire/areas', '0', '0', '2', '900','1'),
       ('901', '获得当前账号的筛查任务', 'getQuestionTask', 'get:/management/questionnaire/task', '0', '0', '1', '900', '1'),
       ('900', '问卷管理', 'questionnaireManagement', NULL, '1', '1', '5', '500', '1');
