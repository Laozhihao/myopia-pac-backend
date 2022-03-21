
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES
       (810, '工单管理', 'workOrderManagement', NULL, 1, 1, 1, 14, 5),
       (811, '工单处理', 'workOrderProcessing', NULL, 0, 1, 1, 810, 1),
       (812, '查看工单', 'workOrderDetail', NULL, 0, 1, 1, 810, 1),
       (813, '查看工单列表', 'workOrderList', 'get:/management/workOrder/list', 0, 0, 1, 810, 1),
       (814, '处理工单', 'workOrderDispose', 'put:/management/workOrder/dispose', 0, 0, 1, 811, 1),
       (815, '工单查询学生列表', 'workOrderStudentList', 'get:/management/student/list', 0, 0, 1, 811, 1),
       (816, '工单查询学生筛查记录', 'workOrderStudentScreeningList', 'get:/management/student/screening/**', 0, 0, 1, 811, 1);

       (817, '学生档案卡路径', 'managementArchivesUrl', 'get:/management/report/student/archivesUrl', 0, 0, 3, 11, 1);
