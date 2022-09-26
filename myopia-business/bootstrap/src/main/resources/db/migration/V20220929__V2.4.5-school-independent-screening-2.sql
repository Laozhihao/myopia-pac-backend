create table m_overview_school
(
    overview_id int                                 not null comment '总览机构id',
    school_id   int                                 not null comment '学校id',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    primary key (overview_id, school_id)
)
    comment '总览机构学校关联表';