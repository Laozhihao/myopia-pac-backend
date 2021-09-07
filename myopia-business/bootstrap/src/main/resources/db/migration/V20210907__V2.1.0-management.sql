alter table m_screening_plan_school_student
    add artificial int default 0 null comment '0-非人造的、1-人造的';

alter table m_screening_plan_school_student
    add screening_code long null comment '筛查编号';

alter table m_student
    modify id_card varchar(32) null comment '身份证号码';

alter table m_screening_plan_school_student
    modify id_card varchar(32) null comment '筛查计划--参与筛查的学生身份证号码';

alter table m_screening_plan_school_student
    modify student_no varchar(64) null comment '筛查计划--参与筛查的学生编号';

alter table m_screening_plan_school_student
    modify student_name varchar(32) not null comment '筛查计划--参与筛查的学生名字';
