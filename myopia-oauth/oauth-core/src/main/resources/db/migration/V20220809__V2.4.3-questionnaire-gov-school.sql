-- ID从960开始，新谋：960~964、治豪：965~969、袁杭：970~974

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (960, '通过key获取下拉值', 'getDropSelectKey', 'get:/management/questionnaire/select/list', 0, 0, 0, 911, 1),
       (961, '问卷系统', 'questionnaireSystem', null, 1, 1, 0, 0, 1),
       (962, '问卷系统get请求', 'questionnaireGet', 'get:/questionnaire/**', 0, 0, 0, 961, 1),
       (963, '问卷系统post请求', 'questionnairePost', 'post:/questionnaire/**', 0, 0, 0, 961, 1),
       (965, '作废筛查计划', 'abolishScreeningPlanBtn', 'put:/management/screeningPlan/abolish/**', 0, 0, 1, 27, 1),
       (966, '删除计划学校', 'deleteScreeningPlanSchoolBtn', 'delete:/management/screeningPlan/school/**/**', 0, 0, 1, 27, 1);


-- 问卷模板设置
INSERT INTO o_permission(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES (970, '问卷模板设置', 'questionnaireQesSetting', NULL, 1, 1, 1, 910, 1),
(971, '创建问卷模板', 'createQuestionnaireQes', 'post:/questionnaire/qes/save', 0, 0, 1, 970, 1),
(972, '上传/更新QES问卷', 'uploadOrUpdateQes', 'post:/questionnaire/qes/upload/**', 0, 0, 2, 970, 1),
(973, '预览qes文件', 'qesPreview', 'get:/questionnaire/qes/preview/**', 0, 0, 3, 970, 1),
(974, '根据年份获取问卷模板qes列表', 'qesList', 'get:/questionnaire/qes/list', 0, 0, 4, 970, 1);

-- rec文件导出
INSERT INTO o_permission(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES (975, '筛查机构管理-rec文件', 'exportOrgQuestionnaireRec', 'post:/management/questionnaire/rec/export', 0, 0, 2, 3, 1),
(976, 'rec文件', 'exportPlatformOrgQuestionnaireRec', 'post:/management/questionnaire/rec/export', 0, 0, 1, 5, 1),
(977, '问卷关联qes字段映射', 'addQesFieldMapping', 'get:/questionnaire/addQesFieldMapping', 0, 0, 1, 970, 1);

-- 问卷管理rec文件权限sql
INSERT INTO o_permission(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES (978, '按区域-rec文件', 'exportDistrictQuestionnaireRec', 'post:/management/questionnaire/rec/export', 0, 0, 1, 882, 1),
(979, '按学校-rec文件', 'exportSchoolQuestionnaireRec', 'post:/management/questionnaire/rec/export', 0, 0, 1, 882, 1);
