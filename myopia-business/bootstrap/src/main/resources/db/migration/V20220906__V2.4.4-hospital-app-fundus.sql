alter table m_device
    add org_type int default 0 not null comment '机构类型 0-筛查机构 1-医院 2-学校' after binding_screening_org_id;
alter table m_device
    add mac_address varchar(32) null comment 'MAC地址' after type;