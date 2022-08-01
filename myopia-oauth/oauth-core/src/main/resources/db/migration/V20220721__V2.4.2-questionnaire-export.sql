
-- ID从930开始


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

        (941, '多端学校管理筛查记录-问卷数据', 'exportMultiTerminalSchool', 'post:/management/questionnaire/export', 0, 0, 1, 9, 1);
