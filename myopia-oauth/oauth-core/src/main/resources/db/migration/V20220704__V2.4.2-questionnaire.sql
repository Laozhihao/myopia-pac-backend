-- ID从900开始
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (906, '问卷编辑', 'questionnaireEdit', 'post:/management/questionnaire/edit', 0, 0, 0, 902, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (905, '问卷详情', 'questionnaireDetail', 'get:/management/questionnaire/detail/**', 0, 0, 1, 902, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (904, '问卷列表', 'questionnaireList', 'get:/management/questionnaire/list', 0, 0, 1, 902, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (903, '题目录入', 'questionSave', 'post:/management/questionnaire/question/save', 0, 0, 1, 901, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (902, '问卷录入', 'questionnaireEnter', null, 1, 1, 1, 900, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (901, '题库录入', 'questionnaireQuestion', null, 1, 1, 1, 900, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code) VALUES (900, '问卷管理', 'questionnaire', null, 1, 1, 1, 14, 1);