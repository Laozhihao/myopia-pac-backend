create table m_device_report_template
(
    id            int auto_increment comment 'id'
        primary key,
    name          varchar(32)                         not null comment '模板名称',
    device_type   tinyint                             not null comment '设备类型 1-VS666',
    template_type tinyint                             not null comment '模板类型 1-VS666模板1',
    create_time   timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '设备报告模板表';

create table m_screening_org_bind_device_report
(
    id                 int auto_increment comment 'id'
        primary key,
    template_id        int                                 not null comment '模板表id',
    screening_org_id   int                                 not null comment '筛查机构Id',
    screening_org_name varchar(32)                         not null comment '筛查机构名称',
    create_time        timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time        timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '筛查机构绑定设备报告模板表';