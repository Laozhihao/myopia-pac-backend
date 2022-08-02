DROP TABLE IF EXISTS `q_qes_field_mapping`;
create table q_qes_field_mapping
(
    id               int auto_increment comment '主键'
        primary key,
    questionnaire_id int                                 not null comment '问卷ID',
    qes_field        varchar(60)                         not null comment 'qes字段',
    system_field     varchar(60)                         not null comment '系统字段',
    create_time      timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment 'qes字段映射关系表' charset = utf8mb4;

DROP TABLE IF EXISTS `q_question`;
create table q_question
(
    id                     int auto_increment comment '主键'
        primary key,
    type                   varchar(30)                         not null comment '问题类型，如：radio（单选）、checkbox（多选）、input（填空）',
    title                  varchar(255)                        not null comment '问题题目',
    sub_title              varchar(128)                        null comment '副标题',
    attribute              json                                null comment '问题属性',
    options                json                                not null comment '问题的答案选项',
    same_question_group_id varchar(128)                        null comment '相同问题uuid',
    serial_number          varchar(15)                         null comment '问题的序号',
    pid                    int                                 null comment '父ID，没有上级为-1',
    icon_name              varchar(16)                         null comment '图标信息',
    create_time            timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time            timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '题目表' charset = utf8mb4;

DROP TABLE IF EXISTS `q_questionnaire`;
create table q_questionnaire
(
    id          int auto_increment comment '主键'
        primary key,
    title       varchar(255)                        not null comment '问卷标题',
    district_id int                                 null comment '区域ID',
    year        int                                 not null comment '年份，如：2022',
    pid         int                                 not null comment '父ID，没有上级为-1',
    type        tinyint                             not null comment '问卷类型',
    status      tinyint                             null comment '问卷状态 0-启用 1-禁用',
    qes_url     varchar(150)                        null comment 'qes文件地址',
    page_json   json                                null comment '页面json数据',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '问卷表' charset = utf8mb4;

DROP TABLE IF EXISTS `q_questionnaire_question`;
create table q_questionnaire_question
(
    id                 int auto_increment
        primary key,
    questionnaire_id   int                  not null comment '问卷ID',
    question_id        int                  not null comment '题目ID',
    pid                int                  not null comment '父题目Id，没有父题目的则为-1',
    serial_number      varchar(15)          null comment '自定义问题的序号',
    sort               tinyint              not null comment '排序',
    is_logic           tinyint(1)           null,
    jump_ids           json                 null comment '跳转题目Ids',
    is_not_show_number tinyint(1) default 0 not null comment '是否不展示题目序号',
    required           tinyint(1) default 1 not null comment '是否必填',
    constraint questionnaire_question_unique_index
        unique (questionnaire_id, question_id)
)
    comment '问卷和题目关系表';

DROP TABLE IF EXISTS `q_user_answer`;
create table q_user_answer
(
    id               int auto_increment comment '主键'
        primary key,
    user_id          int                                 not null comment '用户ID',
    questionnaire_id int                                 not null comment '问卷ID',
    question_id      int                                 not null comment '问题ID',
    record_id        int                                 null comment '记录表Id',
    user_type        int                                 not null comment '用户类型 0-学生 1-学校',
    question_title   varchar(128)                        null,
    answer           json                                null comment '用户答案',
    create_time      timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '用户答案表' charset = utf8mb4;

DROP TABLE IF EXISTS `q_user_answer_progress`;
create table q_user_answer_progress
(
    id               int auto_increment comment '主键'
        primary key,
    user_id          int                                 not null comment '用户id',
    user_type        int                                 not null comment '用户类型',
    current_step     varchar(128)                        null comment 'currentStep',
    current_side_bar varchar(128)                        null comment 'currentSideBar',
    create_time      timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint q_user_answer_progress_user_id_user_type_index
        unique (user_id, user_type)
)
    comment '用户答案进度表' charset = utf8mb4;

DROP TABLE IF EXISTS `q_user_question_record`;
create table q_user_question_record
(
    id                 int auto_increment comment '主键'
        primary key,
    user_id            int                                 not null comment '用户端Id',
    user_type          int                                 null comment '0-学生 1-学校',
    questionnaire_id   int                                 not null comment '问卷Id',
    plan_id            int                                 null comment '计划Id',
    task_id            int                                 null comment '任务Id',
    notice_id          int                                 null comment '通知Id',
    gov_id             int                                 null comment '政府Id',
    school_id          int                                 null comment '学校Id',
    student_id         int                                 null comment '学生Id',
    questionnaire_type int                                 null comment '问卷类型',
    status             tinyint                             null comment '状态 0-未开始 1-进行中 2-结束',
    create_time        timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time        timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint index_name
        unique (user_id, user_type, questionnaire_id)
)
    comment '用户答问卷记录表' charset = utf8mb4;