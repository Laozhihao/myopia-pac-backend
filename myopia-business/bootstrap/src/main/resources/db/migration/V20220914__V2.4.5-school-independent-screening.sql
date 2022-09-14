alter table m_school_admin
    add role_type int default 0 null comment '0-管理员 1-校医' after gov_dept_id;

alter table m_school_admin
    add user_ids varchar(128) null comment '用户Ids，逗号隔开' after role_type;

alter table m_school
    add vision_team_count int null comment '视力小分队人数';

alter table m_school
    add screening_config json null comment '筛查类型的配置';

alter table m_school
    add screening_type_config varchar(16) null comment '筛查类型配置, 英文逗号分隔, 0-视力筛查，1-常见病';