alter table m_parent
    add hyb_bind_status tinyint default 0 not null comment '护眼宝绑定状态 0-未绑定 1-已绑定 2-解除绑定';

create table m_hyb_data
(
    id          int unsigned auto_increment comment '唯一主键'
        primary key,
    parent_id   int                                 not null comment '家长Id',
    student_id  int                                 not null comment '学生Id',
    eye_report  json                                null comment '用眼报告',
    check_time  timestamp                           not null comment '筛查时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间'
)