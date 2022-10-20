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


-- 学校学生表新增字段
alter table m_school_student
    add particular_year int  DEFAULT NULL comment '年份' after sno;

alter table m_school_student
    add is_anisometropia tinyint(1) DEFAULT NULL COMMENT '是否屈光参差';

alter table m_school_student
    add is_refractive_error tinyint(1) DEFAULT NULL COMMENT '是否屈光不正';

alter table m_school_student
   add vision_correction tinyint(3) DEFAULT NULL COMMENT '视力矫正状态';

alter table m_school_student
   add is_normal tinyint(1) DEFAULT NULL COMMENT '是否正常(等效球镜判断)';


-- 管理端学生新增字段
alter table m_student
    add is_anisometropia tinyint(1) DEFAULT NULL COMMENT '是否屈光参差';

alter table m_student
    add is_refractive_error tinyint(1) DEFAULT NULL COMMENT '是否屈光不正';

alter table m_student
   add vision_correction tinyint(3) DEFAULT NULL COMMENT '视力矫正状态';


alter table m_stat_conclusion
   add is_normal tinyint(1) DEFAULT NULL COMMENT '是否正常(等效球镜判断)';




create table m_system_update_notice
(
    id             int auto_increment comment 'id' primary key,
    create_user_id int                                 null comment '创建人ID',
    comment        varchar(256)                        not null comment '内容',
    status         tinyint(0)                          not null default 0 comment '状态 0-上线 1-下线',
    system_code    json                                not null comment '系统编码',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '系统更新表';