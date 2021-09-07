alter table m_screening_plan_school_student
    add artificial int default 0 null comment '0-非人造的、1-人造的';

alter table m_screening_plan_school_student
    add screening_code bigint null comment '筛查编号';

alter table m_student
    modify id_card varchar(32) null comment '身份证号码';

alter table m_screening_plan_school_student
    modify id_card varchar(32) null comment '筛查计划--参与筛查的学生身份证号码';

alter table m_screening_plan_school_student
    modify student_no varchar(64) null comment '筛查计划--参与筛查的学生编号';

alter table m_screening_plan_school_student
    modify student_name varchar(32) not null comment '筛查计划--参与筛查的学生名字';

create unique index m_screening_plan_school_student_screening_code_uindex
    on m_screening_plan_school_student (screening_code);

create index plan_id_school_id_screening_code_index
    on m_screening_plan_school_student (screening_plan_id, school_id, screening_code);
