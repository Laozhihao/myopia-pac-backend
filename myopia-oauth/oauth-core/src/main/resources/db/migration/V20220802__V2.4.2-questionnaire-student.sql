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

-- 管理后台问卷管理权限

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES
        (930, '筛查机构管理-问卷数据', 'exportOrgQuestionnaire', 'post:/management/questionnaire/export', 0, 0, 2, 3, 1),
        (931, '获取有问卷数据的学校', 'questionnaireDataSchool', 'get:/management/questionnaire/dataSchool', 0, 0, 3, 3, 1),

        (932, '问卷管理-下载问卷数据', 'exportPageQuestionnaire', 'post:/management/questionnaire/export', 0, 0, 6, 882, 1),

        (933, '学校环境-下载问卷数据', 'exportSchoolEnvironment', 'post:/management/questionnaire/export', 0, 0, 1, 888, 1),

        (934, '中小学开展学校卫生工作-下载问卷数据', 'exportPrimarySecondarySchools', 'post:/management/questionnaire/export', 0, 0, 1, 887, 1),
        (935, '学生视力不良及脊柱弯曲异常-下载问卷数据', 'exportVisionSpine', 'post:/management/questionnaire/export', 0, 0, 2, 887, 1),
        (936, '学生健康状况-下载问卷数据', 'exportHealthStatus', 'post:/management/questionnaire/export', 0, 0, 3, 887, 1),

        (937, '按区域-问卷数据', 'exportDistrictQuestionnaire', 'post:/management/questionnaire/export', 0, 0, 1, 29, 1),
        (938, '按区域-获取问卷类型', 'districtQuestionnaireType', 'get:/management/questionnaire/type', 0, 0, 1, 29, 1),
        (939, '按学校-问卷数据', 'exportSchoolQuestionnaire', 'post:/management/questionnaire/export', 0, 0, 1, 30, 1),
        (940, '按学校-获取问卷类型', 'schoolQuestionnaireType', 'get:/management/questionnaire/type', 0, 0, 1, 30, 1),

        (941, '多端学校管理筛查记录-问卷数据', 'exportMultiTerminalSchool', 'post:/management/questionnaire/export', 0, 0, 1, 9, 1),

        (952, '问卷数据', 'exportPlatformOrgQuestionnaire', 'post:/management/questionnaire/export', 0, 0, 1, 5, 1);
