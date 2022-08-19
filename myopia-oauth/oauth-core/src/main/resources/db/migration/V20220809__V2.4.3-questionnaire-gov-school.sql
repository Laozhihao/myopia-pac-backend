-- ID从960开始，新谋：960~964、治豪：965~969、袁杭：970~974

INSERT INTO `o_permission`(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES
(960, '通过key获取下拉值', 'getDropSelectKey', 'get:/management/questionnaire/select/list', 0, 0, 0, 911, 1),
(965, '作废筛查计划', 'abolishScreeningPlanBtn', 'put:/management/screeningPlan/abolish/**', 0, 0, 1, 27, 1),
(966, '删除计划学校', 'deleteScreeningPlanSchoolBtn', 'delete:/management/screeningPlan/school/**/**', 0, 0, 1, 27, 1);


INSERT INTO o_permission(`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`)
VALUES (970, '问卷模板设置', 'questionnaireQesSetting', NULL, 1, 1, 1, 910, 1),
(971, '创建问卷模板', 'createQuestionnaireQes', 'post:/questionnaire/qes/save', 0, 0, 1, 970, 1),
(972, '上传/更新QES问卷', 'uploadOrUpdateQes', 'post:/questionnaire/qes/upload/**', 0, 0, 2, 970, 1),
(973, '预览qes文件', 'qesPreview', 'get:/questionnaire/qes/preview/**', 0, 0, 3, 970, 1),
(974, '根据年份获取问卷模板qes列表', 'qesList', 'get:/questionnaire/qes/list', 0, 0, 4, 970, 1);
