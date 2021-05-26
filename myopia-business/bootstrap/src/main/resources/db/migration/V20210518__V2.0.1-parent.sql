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

alter table m_hospital
    add is_cooperation tinyint default 0 null comment '是否合作医院 0-否 1-是' after remark;