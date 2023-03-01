-- ID从1080开始， 帅龙：1080~1085 佳亮：1086~1100
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code)
VALUES (1080, '获取设备对应模板信息（VS550）', 'templateBasicList', 'get:/management/device/report/template/basic/list', 0, 0, 0, 2, 1),
(1086, '数据上报按钮', 'planReportData', null, 0, 0, 7, 5, 1),
(1087, '数据上报数据提交', 'planReportDataAdd', 'post:/management/screeningPlan/data/submit/**', 0, 0, 1, 1086, 1),
(1088, '数据上报列表刷新按钮', 'planReportDataRefresh', 'get:/management/screeningPlan/data/submit/list', 0, 0, 2, 1086, 1),
(1089, '数据上报列表', 'planReportDataList', 'get:/management/screeningPlan/data/submit/list', 0, 0, 3, 1086, 1),
(1090, '数据上报文件下载', 'planDownloadFile', 'get:/management/screeningPlan/data/submit/file/**', 0, 0, 4, 1086, 1),
(1091, '数据上报按钮', 'orgRecordReportData', null, 0, 0, 38, 27, 1),
(1092, '数据上报数据提交', 'orgRecordReportDataAdd', 'post:/management/screeningPlan/data/submit/**', 0, 0, 1, 1091, 1),
(1093, '数据上报列表刷新按钮', 'orgRecordReportDataRefresh', 'get:/management/screeningPlan/data/submit/list', 0, 0, 2, 1091, 1),
(1094, '数据上报列表', 'orgRecordReportDataList', 'get:/management/screeningPlan/data/submit/list', 0, 0, 3, 1091, 1),
(1095, '数据上报文件下载', 'orgRecordDownloadFile', 'get:/management/screeningPlan/data/submit/file/**', 0, 0, 4, 1091, 1);
