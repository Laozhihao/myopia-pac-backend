-- ID 从830开始
UPDATE `o_permission` SET `name` = '按区域统计', `pid` = 31,  `update_time` = NOW() WHERE `id` = 29;
UPDATE `o_permission` SET `name` = '按学校统计', `pid` = 31, `update_time` = NOW() WHERE `id` = 30;


INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,pid, system_code)
VALUES
       (851, '按区域-幼儿园', 'getDistrictKindergartenStatistic', 'get:/management/screening-statistic/district/kindergartenResult', 0, 0, 1, 29, 1),
       (852, '按区域-小学及以上', 'getDistrictPrimaryAndAboveStatistic', 'get:/management/screening-statistic/district/primarySchoolAndAboveResult', 0, 0, 1, 29, 1),
       (853, '按区域-查看详情', 'districtDetail', 'get:/management/screening-statistic/district/screeningResultTotalDetail', 0, 0, 1, 29, 1),
       (854, '按学校-幼儿园', 'getSchoolKindergartenStatistic', 'get:/management/screening-statistic/school/kindergartenResult', 0, 0, 1, 30, 1),
       (855, '按学校-小学及以上', 'getSchoolPrimaryAndAboveStatistic', 'get:/management/screening-statistic/school/primarySchoolAndAboveResult', 0, 0, 1, 30, 1),
       (856, '按学校-查看详情', 'schoolDetail', 'get:/management/screening-statistic/school/schoolStatisticDetail', 0, 0, 1, 30, 1),
       (857, '筛查数据结论', '', 'get:/management/screening-statistic/screeningToConclusion', 0, 0, 1, 30, 1),
       (858, '筛查结果统计', '', 'get:/management/screening-statistic/trigger', 0, 0, 1, 30, 1);
