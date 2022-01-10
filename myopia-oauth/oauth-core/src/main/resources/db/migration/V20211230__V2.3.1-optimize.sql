INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code, create_time,
                          update_time)
VALUES (564, '更新结果通知配置', 'updateResultNoticeConfig',
        'put:/management/screeningOrganization/update/resultNoticeConfig/**', 0, 0, 1, 27, 1, '2021-12-29 17:39:35',
        '2021-12-29 17:39:35');

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid,
                          system_code)
VALUES (568, '异步导出学生报告', 'asyncGeneratorPDF', 'get:/management/screeningPlan/screeningNoticeResult/asyncGeneratorPDF',
        0, 0, 0, 27, 1);
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid,
                          system_code)
VALUES (569, '同步导出学生报告', 'syncGeneratorPDF', 'get:/management/screeningPlan/screeningNoticeResult/syncGeneratorPDF', 0,
        0, 0, 27, 1);