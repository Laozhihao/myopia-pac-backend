INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (917, '题库搜索问题', 'questionSearch', 'get:/management/questionnaire/question/search', 0, 0, 1, 911, 1),
       (916, '问卷编辑', 'questionnaireEdit', 'post:/management/questionnaire/edit', 0, 0, 0, 912, 1),
       (915, '问卷详情', 'questionnaireDetail', 'get:/management/questionnaire/detail/**', 0, 0, 1, 912, 1),
       (914, '问卷列表', 'questionnaireList', 'get:/management/questionnaire/list', 0, 0, 1, 912, 1),
       (913, '题目录入', 'questionSave', 'post:/management/questionnaire/question/save', 0, 0, 1, 911, 1),
       (912, '问卷录入', 'questionnaireEnter', null, 1, 1, 1, 910, 1),
       (911, '题库录入', 'questionnaireQuestion', null, 1, 1, 1, 910, 1),
       (910, '问卷管理', 'questionnaire', null, 1, 1, 1, 14, 1);