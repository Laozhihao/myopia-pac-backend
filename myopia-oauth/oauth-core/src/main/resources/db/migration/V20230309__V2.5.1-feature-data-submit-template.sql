INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1100, '数据上报模版', 'dataSubmissionTemplate', null, 1, 1, 0, 14, 1),
       (1101, '获取数据上报模版', 'dataSubmitTemplate', 'get:/management/dataSubmit/template', 0, 0, 0, 1100, 1);