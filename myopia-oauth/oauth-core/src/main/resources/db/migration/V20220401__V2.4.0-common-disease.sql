-- ID 从830开始
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`,
                          pid, system_code)
VALUES
    (841, '获取学生复测卡', 'studentScreeningResult', 'get:/management/student/screeningResult/**', 0, 0, 1, 811, 1);