-- 通知书
alter table m_screening_organization
    add result_notice_config json null comment '结果通知配置';

alter table m_school
    add result_notice_config json null comment '结果通知配置';

alter table m_school_student
    drop column school_no;

alter table m_screening_plan_school_student
    drop column school_no;

-- 账号限制数量
ALTER TABLE m_hospital ADD account_num bigint(10) DEFAULT 7 COMMENT '账号数量';
ALTER TABLE m_screening_organization ADD account_num bigint(10) DEFAULT 5 COMMENT '筛查人员账号数量';