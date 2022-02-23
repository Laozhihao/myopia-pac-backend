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