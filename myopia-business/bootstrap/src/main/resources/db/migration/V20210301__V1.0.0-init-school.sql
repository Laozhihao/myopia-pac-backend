INSERT INTO myopia_business.m_school (id, school_no, create_user_id, gov_dept_id, district_id, district_detail, name,
                                      kind, kind_desc, lodge_status, type, province_code, city_code, area_code,
                                      town_code, address, remark, status, create_time, update_time)
VALUES (1, '1234567890', 1, 1, 1919,
        '[{"id":1918,"name":"广东省","code":440000000,"parentCode":100000000,"areaCode":null,"monitorCode":null,"child":null},{"id":1919,"name":"广州市","code":440100000,"parentCode":440000000,"areaCode":null,"monitorCode":null,"child":null}]',
        '其他', 2, '其他', null, 7, null, null, null, null, '', '', 0, '2021-03-01 14:19:06', '2021-03-01 14:19:06');

INSERT INTO myopia_oauth.o_user (org_id, real_name, gender, phone, id_card, username, password, is_leader,
                                 system_code, create_user_id, status, remark, create_time, update_time, last_login_time,
                                 user_type)
VALUES (1, '其他', null, null, null, '其他', '$2a$10$gz/7Axb/vWVwgU.2QtdQSeI3eb7fCXY3EkKiOxSlRrz8jdJ4gnoMm', 0, 2,
        1, 0, null, '2021-03-01 14:19:07', '2021-03-01 14:19:07', null, 1);

INSERT INTO myopia_business.m_school_grade (id, create_user_id, school_id, grade_code, name, status, create_time,
                                            update_time)
VALUES (1, 1, 1, '90', '其他', 0, '2021-03-01 14:29:32', '2021-03-01 14:29:32');


INSERT INTO myopia_business.m_school_class (grade_id, create_user_id, school_id, name, seat_count, status,
                                            create_time, update_time)
VALUES (1, 1, 1, '其他', 30, 0, '2021-03-01 14:29:39', '2021-03-01 14:29:39');