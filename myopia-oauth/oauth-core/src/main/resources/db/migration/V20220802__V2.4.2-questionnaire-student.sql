INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (922, '获取逻辑题目列表', 'logicList', 'get:/management/questionnaire/logic/list', 0, 0, 0, 911, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (921, '逻辑题编辑', 'logicEdit', 'post:/management/questionnaire/logic/edit', 0, 0, 0, 911, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (920, '查找题目', 'logicFindQuestion', 'get:/management/questionnaire/logic/findQuestion', 0, 0, 0, 911, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (919, '逻辑题删除', 'logicDeleted', 'post:/management/questionnaire/logic/deleted', 0, 0, 0, 911, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (918, '题库搜索问题', 'questionSearch', 'get:/management/questionnaire/question/search', 0, 0, 1, 912, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (917, '问卷编辑', 'questionnaireEdit', 'post:/management/questionnaire/edit', 0, 0, 0, 913, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (916, '问卷详情', 'questionnaireDetail', 'get:/management/questionnaire/detail/**', 0, 0, 1, 913, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (915, '问卷列表', 'questionnaireList', 'get:/management/questionnaire/list', 0, 0, 1, 913, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (914, '题目录入', 'questionSave', 'post:/management/questionnaire/question/save', 0, 0, 1, 912, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (913, '问卷录入', 'questionnaireEnter', null, 1, 1, 1, 911, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (912, '题库录入', 'questionnaireQuestion', null, 1, 1, 1, 911, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (911, '题库管理', 'questionnaireBank', null, 1, 1, 1, 910, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (910, '问卷中心', 'questionnaire', null, 1, 1, 1, 0, 1);
