INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1114, '获取可关联的通知', 'planLinkNoticeList', 'get:/management/screeningPlan/planLinkNotice/list', 0, 0, 0, 27, 1),
       (1115, '计划关联通知', 'planLinkNotice', 'post:/management/screeningPlan/linkNotice/link', 0, 0, 0, 27, 1),
       (1116, '手动调用触发关联通知', 'triggerNoticeLink', 'get:/management/test/triggerNoticeLink', 0, 0, 0, 294, 1),
       (1117, '计划关联按钮', 'planAssociateTask', null, 0, 0, 0, 27, 1);