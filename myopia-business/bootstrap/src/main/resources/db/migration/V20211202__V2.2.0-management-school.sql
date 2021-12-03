-- auto-generated definition
DROP TABLE IF EXISTS `m_school_student`;
create table m_school_student
(
    id                  int auto_increment comment 'id'
        primary key,
    student_id          int                                 null comment '学生表Id',
    school_id           int                                 null comment '学校ID',
    school_no           varchar(64)                         null comment '学校编号',
    create_user_id      int                                 null comment '创建人ID',
    sno                 varchar(64)                         null comment '学号',
    grade_id            int                                 null comment '年级ID',
    grade_name          varchar(8)                          null comment '年级名称',
    grade_type          tinyint                             null comment '学龄段',
    class_id            int                                 null comment '班级ID',
    class_name          varchar(8)                          null comment '班级名称',
    name                varchar(32)                         not null comment '学生姓名',
    gender              tinyint(1)                          not null comment '性别 0-男 1-女',
    birthday            timestamp                           null comment '出生日期',
    nation              tinyint                             null comment '民族 0-汉族',
    id_card             varchar(32)                         null comment '身份证号码',
    parent_phone        varchar(16)                         null comment '家长手机号码',
    mp_parent_phone     varchar(128)                        null comment '家长公众号手机号码',
    province_code       bigint                              null comment '省代码',
    city_code           bigint                              null comment '市代码',
    area_code           bigint                              null comment '区代码',
    town_code           bigint                              null comment '镇/乡代码',
    address             varchar(128)                        null comment '详细地址',
    status              tinyint   default 0                 not null comment '状态 0-启用 1-禁止 2-删除',
    glasses_type        int                                 null comment '戴镜类型',
    last_screening_time timestamp                           null comment '最近筛选时间',
    vision_label        tinyint                             null comment '视力标签 0-零级、1-一级、2-二级、3-三级',
    myopia_level        tinyint(1)                          null comment '近视等级：0-正常、1-筛查性近视、2-近视前期、3-低度近视、4-中度近视、5-重度近视',
    hyperopia_level     tinyint(1)                          null comment '远视等级：0-正常、1-远视、2-低度远视、3-中度远视、4-重度远视',
    astigmatism_level   tinyint(1)                          null comment '散光等级：0-正常、1-低度散光、2-中度散光、3-重度散光',
    create_time         timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time         timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '学校端-学生表';

alter table m_stat_conclusion
    add is_bind_mp tinyint null comment '是否绑定公众号';

alter table m_stat_conclusion
    add report_id int null comment '报告Id';

alter table m_school_vision_statistic
    add bind_mp_numbers int default 0 null comment '绑定公众号人数' after myopia_level_insufficient;

alter table m_school_vision_statistic
    add review_numbers int default 0 null comment '去医院就诊数' after bind_mp_numbers;

drop index m_student_school_no_status_class_id_grade_id_index on m_student;

alter table m_student drop column school_no;

create index m_student_status_class_id_grade_id_index
    on m_student (status, class_id, grade_id);

alter table m_stat_conclusion
    alter column is_bind_mp set default false;

alter table m_school_vision_statistic alter column myopia_level_light set default 0;

alter table m_school_vision_statistic alter column myopia_level_early set default 0;

alter table m_school_vision_statistic alter column myopia_level_middle set default 0;

alter table m_school_vision_statistic alter column myopia_level_high set default 0;

alter table m_school_vision_statistic alter column myopia_level_insufficient set default 0;

alter table m_district_vision_statistic alter column myopia_level_early_num set default 0;

alter table m_school
    add notification_config json null comment '告知书配置';