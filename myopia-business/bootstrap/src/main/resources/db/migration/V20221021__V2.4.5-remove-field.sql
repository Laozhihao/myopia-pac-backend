alter table m_stat_conclusion
    drop column is_normal;

alter table m_school_student
    drop column is_normal;

alter table m_screening_plan_school_student
    drop column grade_name;

alter table m_screening_plan_school_student
    drop column class_name;