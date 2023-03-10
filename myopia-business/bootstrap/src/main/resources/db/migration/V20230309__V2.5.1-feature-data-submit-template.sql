alter table m_school
    add data_submit_config json null comment '数据上报模版配置';

update m_school set data_submit_config = '[0]' where data_submit_config is null ;

alter table m_screening_organization
    add data_submit_config json null comment '数据上报模版配置';

update m_screening_organization set data_submit_config = '[0]' where data_submit_config is null;

create index m_plan_student_id_is_rescreen_index
    on m_stat_conclusion (screening_plan_school_student_id, is_rescreen);