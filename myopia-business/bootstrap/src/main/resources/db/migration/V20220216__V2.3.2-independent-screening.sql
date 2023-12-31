alter table m_student
    add passport varchar(32) null comment '护照';

alter table m_school_student
    add passport varchar(32) null comment '护照' after astigmatism_level;

alter table h_hospital_student
    add passport varchar(32) null comment '护照';

alter table m_screening_plan_school_student
    add passport varchar(32) null comment '护照';

alter table m_screening_organization
    add qr_code_config varchar(16) default '' not null comment '二维码配置, 英文逗号分隔, 1-普通二维码, 2-vs666, 3-虚拟二维码';

create unique index m_student_passport_uindex
    on m_student (passport);

alter table m_vision_screening_result
    add height_and_weight_data json DEFAULT NULL COMMENT '筛查结果--身高体重';

alter table m_school_student
    add source_client tinyint default 0 null comment '源客户端 0-多端 1-学校端 2-筛查计划';

alter table h_hospital_student
    modify id_card varchar(32) null comment '身份证号码';

alter table m_student
    add source_client tinyint default 0 null comment '源客户端 0-多端 1-学校端 2-筛查计划';

DROP TABLE IF EXISTS `m_deleted_archive`;
create table m_deleted_archive
(
    id          int auto_increment
        primary key,
    type        int                                 not null comment '数据类型 1-多端学生 2-筛查学生 3-学校端学生',
    content     json                                null comment '内容',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间'
) comment '删除信息归档表';
