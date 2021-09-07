alter table m_screening_plan_school_student
    add artificial int default 0 null comment '0-非人造的、1-人造的';

alter table m_screening_plan_school_student
    add screening_code long null comment '筛查编号';