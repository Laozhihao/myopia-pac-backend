create table m_overview_school
(
    overview_id int                                 not null comment '总览机构id',
    school_id   int                                 not null comment '学校id',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    primary key (overview_id, school_id)
)
    comment '总览机构学校关联表';

alter table m_overview
    add school_config_type int default 0 not null comment '学校配置 0-默认配置' after cooperation_end_time;

alter table m_overview
    add school_limit_num int default 5 not null comment '学校限制数量' after school_config_type;


-- 学校学生表新增年份字段
alter table m_school_student
    add particular_year int  DEFAULT NULL comment '年份' after sno;



