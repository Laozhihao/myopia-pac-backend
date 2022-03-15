
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES
       (810, '工单管理', 'workOrderManagement', NULL, 1, 1, 1, 14, 1),
       (811, '工单处理', 'workOrderProcessing', NULL, 0, 1, 1, 810, 1),
       (812, '查看工单', 'workOrderDetail', NULL, 0, 1, 1, 810, 1),
       (820, '查看工单列表', 'workOrderList', 'get:/management/workTable/list', 0, 0, 1, 810, 1),
       (821, '处理工单', 'workOrderDispose', 'get:/management/workTable/dispose', 0, 0, 2, 810, 1);
