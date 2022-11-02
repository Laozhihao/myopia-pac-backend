alter table m_stat_conclusion
    drop column is_normal;

alter table m_school_student
    drop column is_normal;

alter table m_screening_plan_school_student
    drop column grade_name;

alter table m_screening_plan_school_student
    drop column class_name;

alter table m_school_student
    add is_myopia tinyint(1) null comment '是否近视';

alter table m_school_student
    add is_hyperopia tinyint(1) null comment '是否远视';

alter table m_school_student
    add is_astigmatism tinyint(1) null comment '是否散光';