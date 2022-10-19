create table m_system_update_notice
(
    id             int auto_increment comment 'id' primary key,
    create_user_id int                                 null comment '创建人ID',
    comment        varchar(256)                        not null comment '内容',
    status         tinyint(0)                          not null default 0 comment '状态 0-上线 1-下线',
    system_code    json                                not null comment '系统编码',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '系统更新表';