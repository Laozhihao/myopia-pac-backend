-- auto-generated definition
create table m_national_data_download_record
(
    id            int auto_increment comment 'id'
        primary key,
    school_id     int                                 not null comment '学校Id',
    remark        varchar(128)                        null comment '说明',
    success_match int                                 null comment '成功匹配',
    fail_match    int                                 null comment '失败匹配',
    file_id       int                                 null comment '文件Id',
    status        int                                 not null default 0 comment '状态 0-创建 1-进行中 2-成功 3-失败',
    create_time   timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '数据报送';

