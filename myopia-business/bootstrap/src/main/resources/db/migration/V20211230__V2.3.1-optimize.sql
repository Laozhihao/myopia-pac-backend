alter table m_screening_organization
    add result_notice_config json null comment '结果通知配置';

alter table m_school
    add result_notice_config json null comment '结果通知配置';

alter table m_school_student
    drop column school_no;

alter table m_screening_plan_school_student
    drop column student_no;