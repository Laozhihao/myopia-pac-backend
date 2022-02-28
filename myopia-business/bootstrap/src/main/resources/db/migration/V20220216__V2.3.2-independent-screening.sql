alter table m_student
    add passport varchar(32) null comment '护照';

alter table m_school_student
    add passport varchar(32) null comment '护照' after astigmatism_level;

alter table h_hospital_student
    add passport varchar(32) null comment '护照';

alter table m_screening_plan_school_student
    add passport varchar(32) null comment '护照';

create unique index m_student_passport_uindex
    on m_student (passport);

alter table m_vision_screening_result
    add height_and_weight_data json DEFAULT NULL COMMENT '筛查结果--身高体重';

alter table m_school_student
    add source_client tinyint default 0 null comment '源客户端 0-多端 1-学校端 2-筛查计划';

alter table h_hospital_student
    modify id_card varchar(32) null comment '身份证号码';
