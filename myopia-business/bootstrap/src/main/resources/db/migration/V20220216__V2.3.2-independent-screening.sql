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
