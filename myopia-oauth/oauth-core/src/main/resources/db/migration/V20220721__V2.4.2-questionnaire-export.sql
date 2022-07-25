
-- ID从930开始


-- 管理后台问卷管理权限

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES ('930', '导出问卷数据', 'exportQuestionnaire', 'post:/management/questionnaire/export', '0', '0', '30', '910', '1'),
       ('931', '获取有问卷数据的学校', 'questionnaireDataSchool', 'get:/management/questionnaire/dataSchool', '0', '0', '31', '910','1'),
       ('932', '获取问卷类型', 'questionnaireType', 'get:/management/questionnaire/type', '0', '0', '32', '910', '1'),
       ('933', '下载问卷数据(页面)', 'questionnairePageData', NULL, '1', '1', '33', '910', '1'),
       ('934', '下载问卷数据(学校列表)', 'questionnaireSchoolData', NULL, '1', '1', '34', '910', '1'),
       ('935', '问卷数据(机构筛查记录)', 'organizationScreeningRecord', NULL, '1', '1', '35', '910', '1'),
       ('936', '问卷数据(按区域统计)', 'districtStatistics', NULL, '1', '1', '36', '910', '1'),
       ('937', '问卷数据(按学校统计)', 'schoolStatistics', NULL, '1', '1', '37', '910', '1'),
       ('938', '问卷数据(学校筛查记录)', 'schoolScreeningRecord', NULL, '1', '1', '38', '910', '1');
