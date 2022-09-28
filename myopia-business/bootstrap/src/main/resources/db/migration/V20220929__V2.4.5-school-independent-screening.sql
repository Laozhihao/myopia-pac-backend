alter table m_school
    add vision_team_count int not null default 5 comment '视力小分队人数';

alter table m_school
    add screening_config json null comment '筛查类型的配置';

alter table m_school
    add screening_type_config varchar(16) null default '0' comment '筛查类型配置, 英文逗号分隔, 0-视力筛查，1-常见病';

update m_school set screening_config = '{"channel":"Official","screeningTypeList":[0],"medicalProjectList":["vision","computer_optometry","other_eye_diseases"]}';

create table m_school_staff
(
    id           int auto_increment primary key comment 'id',
    school_id    int                                  not null comment '学校Id',
    staff_name   varchar(8)                           not null comment '姓名',
    gender       tinyint(1)                           null comment '性别：0-男、1-女',
    phone        varchar(16)                          not null comment '手机号码',
    id_card      varchar(32)                          not null comment '身份证',
    staff_type   int                                  not null default 0 comment '角色 0-校医',
    account_info json                                 not null comment '用户表信息',
    status       tinyint(1) default 0                 not null comment '状态：0-启用 1-禁止 2-删除',
    remark       varchar(128)                         null comment '备注',
    create_time  timestamp  default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '学校员工表';