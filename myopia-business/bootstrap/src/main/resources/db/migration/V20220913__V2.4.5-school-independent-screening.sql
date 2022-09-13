alter table m_school_admin
    add role_type int default 0 null comment '0-管理员 1-校医' after gov_dept_id;

alter table m_school_admin
    add user_ids varchar(128) null comment '用户Ids，逗号隔开' after role_type;