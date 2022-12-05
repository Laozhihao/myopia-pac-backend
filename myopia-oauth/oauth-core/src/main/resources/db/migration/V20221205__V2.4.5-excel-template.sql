INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES (1065, '导出学校筛查模板', 'schoolExportTemplate', 'get:/management/screeningResult/school/template/export', 0,
        0, 0, 27, 1),
       (1066, '导入学校筛查模板', 'schoolTemplateImport', 'post:/management/screeningResult/school/template/import', 0,
        0, 0, 27, 1);