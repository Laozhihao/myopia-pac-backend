INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1050, '系统更新提醒', 'systemUpdateNotice', null, 1, 1, 0, 14, 1),
       (1051, '系统更新通知下线通知', 'systemNoticeStatus', 'post:/management/systemNotice/status/**', 0, 0, 0, 1050,1),
       (1052, '系统更新通知保存通知', 'systemNoticeSave', 'post:/management/systemNotice/save', 0, 0, 0, 1050, 1),
       (1053, '系统更新通知获取列表', 'systemNoticeList', 'get:/management/systemNotice/list', 0, 0, 0, 1050, 1);