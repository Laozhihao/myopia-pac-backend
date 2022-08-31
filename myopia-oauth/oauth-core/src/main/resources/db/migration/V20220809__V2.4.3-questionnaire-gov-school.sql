-- ID从960开始，新谋：960~964、治豪：965~969、袁杭：970~974

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (960, '通过key获取下拉值', 'getDropSelectKey', 'get:/management/questionnaire/select/list', 0, 0, 0, 911, 1),
       (961, '问卷系统', 'questionnaireSystem', null, 1, 1, 0, 0, 1),
       (962, '问卷系统get请求', 'questionnaireGet', 'get:/questionnaire/**', 0, 0, 0, 961, 1),
       (963, '问卷系统post请求', 'questionnairePost', 'post:/questionnaire/**', 0, 0, 0, 961, 1);