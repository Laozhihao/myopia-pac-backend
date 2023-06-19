delete from o_permission where id in (703,705,706,708,763);
delete from o_role_permission where permission_id in (703,705,706,708,763);
delete from o_district_permission where permission_id in (703,705,706,708,763);

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1170, '筛查二维码', 'vsScreeningQrcode', 'get:/management/report/screeningOrg/qrcode', 0, 0, 1, 560, 1),
       (1171, '虚拟二维码', 'virtualScreeningQrcode', 'get:/management/report/screeningOrg/qrcode', 0, 0, 0, 560, 1),
       (1172, '导出问卷账号密码', 'questionnaireAccount', 'get:/management/screeningPlan/export/planStudent/questionnaire/account/**/**', 0, 0, 0, 27, 1),
       (1173, '复查报告-获取学校日期', 'getReScreeningReportSchoolDate', 'get:/management/stat/rescreen/schoolDate', 0, 0, 0, 30, 1);