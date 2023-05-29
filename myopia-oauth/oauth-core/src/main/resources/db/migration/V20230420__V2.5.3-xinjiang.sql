INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1148, '判断是否为新疆地区机构', 'isXinJiangDistrictOrg', 'get:/management/screeningPlan/isXinJiangDistrict', 0, 0, 1, 27, 1),
(1149, '手动同步筛查数据到新疆', 'syncDataToXinJiang', 'get:/management/test/syncDataToXinJiang', 0, 0, 1, 294, 1),
(1166, '更新筛查计划信息', 'updateScreeningPlanInfo', 'put:/management/test/updatePlan', 0, 0, 1, 294, 1);