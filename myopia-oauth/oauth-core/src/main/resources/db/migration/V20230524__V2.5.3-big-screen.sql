INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1180, '获取大屏通知', 'bigScreeningGetNotice', 'get:/management/screening-statistic/big-screen-notice-year', 0,0, 0, 226, 1),
       (1181, '获取大屏通知所在年度的筛查任务', 'bigScreeningGetNoticeDetailByYearAndUser','get:/management/screening-statistic/big-screen-notice', 0, 0, 0, 226, 1);