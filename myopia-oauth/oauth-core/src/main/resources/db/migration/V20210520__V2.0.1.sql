-- 家长端功能相关
INSERT INTO o_permission (`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES 
(298, '筛查机构管理-医院配置', 'multiPortOrganizationHospitalConfig', null, 1, 1, 1, 2, 1),
(299, '医院配置', 'multiPortHospitalConfig', null, 1, 1, 1, 1, 1),
(300, '获取合作医院列表（二级)', 'getOrgCooperationHospital', 'get:/management/screeningOrganization/getOrgCooperationHospital/**', 0, 0, 1, 299, 1),
(301, '新增合作医院（二级)', 'saveOrgCooperationHospital', 'post:/management/screeningOrganization/saveOrgCooperationHospital', 0, 0, 1, 299, 1),
(302, '删除合作医院（二级)', 'deletedCooperationHospital', 'delete:/management/screeningOrganization/deletedCooperationHospital/**', 0, 0, 1, 299, 1),
(303, '置顶医院（二级)', 'topCooperationHospital', 'put:/management/screeningOrganization/topCooperationHospital/**', 0, 0, 1, 299, 1),
(304, '获取医院（筛查机构只能看到全省）（二级)', 'getOrgCooperationHospitalList', 'get:/management/screeningOrganization/getOrgCooperationHospitalList', 0, 0, 1, 299, 1),
(305, '获取合作医院列表（三级)', 'GetOrgCooperationHospital', 'get:/management/screeningOrganization/getOrgCooperationHospital/**', 0, 0, 1, 298, 1),
(306, '新增合作医院（三级)', 'SaveOrgCooperationHospital', 'post:/management/screeningOrganization/saveOrgCooperationHospital', 0, 0, 1, 298, 1),
(307, '删除合作医院（三级)', 'DeletedCooperationHospital', 'delete:/management/screeningOrganization/deletedCooperationHospital/**', 0, 0, 1, 298, 1),
(308, '置顶医院（三级)', 'TopCooperationHospital', 'put:/management/screeningOrganization/topCooperationHospital/**', 0, 0, 1, 298, 1),
(309, '获取医院（筛查机构只能看到全省）（三级)', 'GetOrgCooperationHospitalList', 'get:/management/screeningOrganization/getOrgCooperationHospitalList', 0, 0, 1, 298, 1);

-- 筛查通知-创建筛查任务弹窗
INSERT INTO `o_permission` (`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES 
(310, '获取指定任务下的机构信息', 'screeningOrgOfTask', 'get:/management/screeningTask/orgs/**/**', '0', '0', '8', '25', '1');

-- 导出复测报告功能
INSERT INTO `o_permission` (`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES
(311, '导出复测报告功能', 'statResultSchoolRescreenExport', 'get:/management/stat/rescreen/export', '0', '0', '1', '30', '1'),
(312, '导出复测报告功能', 'orgScreenRecordRescreenExport', 'get:/management/stat/rescreen/export', '0', '0', '1', '3', '1'),
(313, '导出复测报告功能', 'schoolScreenRecordRescreenExport', 'get:/management/stat/rescreen/export', '0', '0', '1', '9', '1');

-- 筛查结果-学校 按计划筛选功能
INSERT INTO `o_permission` (`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES
(314, '获取计划年度', 'screenStatPlanYear', 'get:/management/screening-statistic/plan-year', '0', '0', '1', '30', '1'),
(315, '获取年度计划', 'screenStatPlan', 'get:/management/screening-statistic/plan', '0', '0', '1', '30', '1'),
(316, '获取计划地区信息', 'getPlanDistrict', 'get:/management/screening-statistic/plan-district', '0', '0', '1', '30', '1'),
(317, '计划下学校视力情况', 'planScreeningVisionResult', 'get:/management/screening-statistic/plan/school/screening-vision-result', '0', '0', '1', '30', '1'),
(318, '计划下学校监控统计情况', 'planScreeningMonitorResult', 'get:/management/screening-statistic/plan/school/screening-monitor-result', '0', '0', '1', '30', '1');

-- 医院配置-给筛查机构绑定医院的弹窗中
INSERT INTO `o_permission` (`id`, `name`, `menu_btn_name`, `api_url`, `is_menu`, `is_page`, `order`, `pid`, `system_code`) VALUES 
(319, '新增医院', 'addHospital', 'post:/management/hospital', '0', '0', '6', '299', '1'),
(320, '新增医院', 'addHospitalInBindingDialog', 'post:/management/hospital', '0', '0', '6', '298', '1');
