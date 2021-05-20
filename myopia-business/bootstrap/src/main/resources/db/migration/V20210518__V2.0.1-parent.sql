create table m_org_cooperation_hospital
(
    id               int auto_increment comment 'id'
        primary key,
    screening_org_id int                                 not null comment '筛查机构Id',
    hospital_id      int                                 not null comment '医院Id',
    is_top           tinyint                             not null default 0 comment '是否置顶',
    create_time      timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '筛查机构合作医院表' charset = utf8mb4;

alter table m_hospital
    add district_province_code tinyint null comment '行政区域-省Code（保留两位）' after district_id;

alter table m_hospital
    add avatar_file_id int null comment '头像资源Id' after address;

INSERT INTO o_permission (name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code,
                          create_time, update_time)
VALUES ('获取合作医院列表（二级)', 'getOrgCooperationHospital',
        'get:/management/screeningOrganization/getOrgCooperationHospital/**', 0, 0, 1, 291, 1, '2021-05-20 17:20:11',
        '2021-05-20 17:24:56');
INSERT INTO o_permission (name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code,
                          create_time, update_time)
VALUES ('新增合作医院（二级)', 'saveOrgCooperationHospital', 'post:/management/screeningOrganization/saveOrgCooperationHospital',
        0, 0, 1, 291, 1, '2021-05-20 17:21:00', '2021-05-20 17:24:56');
INSERT INTO o_permission (name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code,
                          create_time, update_time)
VALUES ('删除合作医院（二级)', 'deletedCooperationHospital',
        'delete:/management/screeningOrganization/deletedCooperationHospital/**', 0, 0, 1, 291, 1,
        '2021-05-20 17:22:22', '2021-05-20 17:24:56');
INSERT INTO o_permission (name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code,
                          create_time, update_time)
VALUES ('置顶医院（二级)', 'topCooperationHospital', 'put:/management/screeningOrganization/topCooperationHospital/**', 0, 0,
        1, 291, 1, '2021-05-20 17:23:12', '2021-05-20 17:24:56');
INSERT INTO o_permission (name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code,
                          create_time, update_time)
VALUES ('获取医院（筛查机构只能看到全省）（二级)', 'getOrgCooperationHospitalList',
        'get:/management/screeningOrganization/getOrgCooperationHospitalList', 0, 0, 1, 291, 1, '2021-05-20 17:23:48',
        '2021-05-20 17:24:56');
INSERT INTO o_permission (name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code,
                          create_time, update_time)
VALUES ('获取合作医院列表（三级)', 'GetOrgCooperationHospital',
        'get:/management/screeningOrganization/getOrgCooperationHospital/**', 0, 0, 1, 2, 1, '2021-05-20 17:27:10',
        '2021-05-20 17:27:30');
INSERT INTO o_permission (name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code,
                          create_time, update_time)
VALUES ('新增合作医院（三级)', 'SaveOrgCooperationHospital', 'post:/management/screeningOrganization/saveOrgCooperationHospital',
        0, 0, 1, 2, 1, '2021-05-20 17:27:10', '2021-05-20 17:27:30');
INSERT INTO o_permission (name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code,
                          create_time, update_time)
VALUES ('删除合作医院（三级)', 'DeletedCooperationHospital',
        'delete:/management/screeningOrganization/deletedCooperationHospital/**', 0, 0, 1, 2, 1, '2021-05-20 17:27:10',
        '2021-05-20 17:27:30');
INSERT INTO o_permission (name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code,
                          create_time, update_time)
VALUES ('置顶医院（三级)', 'TopCooperationHospital', 'put:/management/screeningOrganization/topCooperationHospital/**', 0, 0,
        1, 2, 1, '2021-05-20 17:27:10', '2021-05-20 17:27:30');
INSERT INTO o_permission (name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code,
                          create_time, update_time)
VALUES ('获取医院（筛查机构只能看到全省）（三级)', 'GetOrgCooperationHospitalList',
        'get:/management/screeningOrganization/getOrgCooperationHospitalList', 0, 0, 1, 2, 1, '2021-05-20 17:27:10',
        '2021-05-20 17:27:30');