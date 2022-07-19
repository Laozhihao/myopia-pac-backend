CREATE TABLE `q_questionnaire`
(
    `id`          int          not null auto_increment comment '主键',
    `title`       varchar(255) not null comment '问卷标题',
    `district_id` int          default null comment '区域ID',
    `year`        int          not null comment '年份，如：2022',
    `pid`         int          not null comment '父ID，没有上级为-1',
    `type`        tinyint      not null comment '问卷类型',
    `status`      tinyint      default null comment '问卷状态 0-启用 1-禁用',
    `qes_url`     varchar(150) default null comment 'qes文件地址',
    `page_json`   json         default null comment '页面json数据',
    `create_time` timestamp    not null default CURRENT_TIMESTAMP comment '创建时间',
    `update_time` timestamp    not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  default CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
    comment '问卷表';

CREATE TABLE `q_question`
(
    `id`                     int          not null auto_increment comment '主键',
    `type`                   varchar(30)  not null comment '问题类型，如：radio（单选）、checkbox（多选）、input（填空）',
    `title`                  varchar(255) not null comment '问题题目',
    `attribute`              json                  default null comment '问题属性',
    `options`                json         not null comment '问题的答案选项',
    `same_question_group_id` varchar(128) null comment '相同问题uuid',
    `serial_number`          varchar(15)           default null comment '问题的序号',
    `pid`                    int          null comment '父ID，没有上级为-1',
    `icon_name`              varchar(16)  null comment '图标信息',
    `create_time`            timestamp    not null default CURRENT_TIMESTAMP comment '创建时间',
    `update_time`            timestamp    not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  default CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
    comment '题目表';

create table q_questionnaire_question
(
    id               int auto_increment
        primary key,
    questionnaire_id int         not null comment '问卷ID',
    question_id      int         not null comment '题目ID',
    pid              int         not null comment '父题目Id，没有父题目的则为-1',
    serial_number    varchar(15) null comment '自定义问题的序号',
    sort             tinyint     not null comment '排序',
    jump_ids         json        null comment '跳转题目Ids',
    required         boolean     not null default true comment '是否必填',
    constraint questionnaire_question_unique_index
        unique (questionnaire_id, question_id)
)
    comment '问卷和题目关系表';

CREATE TABLE `q_qes_field_mapping`
(
    `id`               int auto_increment primary key comment '主键',
    `questionnaire_id` int         not null comment '问卷ID',
    `qes_field`        varchar(60) not null comment 'qes字段',
    `system_field`     varchar(60) not null comment '系统字段',
    `create_time`      timestamp   not null default CURRENT_TIMESTAMP comment '创建时间',
    `update_time`      timestamp   not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间'
) ENGINE = InnoDB
  default CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
    comment 'qes字段映射关系表';

CREATE TABLE `q_user_answer`
(
    `id`               int          not null auto_increment comment '主键',
    `user_id`          int          not null comment '用户ID',
    `questionnaire_id` int          not null comment '问卷ID',
    `question_id`      int          not null comment '问题ID',
    `question_title`   varchar(128) not null comment '问题标题',
    `answer`           json                  default null comment '用户答案',
    `create_time`      timestamp    not null default CURRENT_TIMESTAMP comment '创建时间',
    `update_time`      timestamp    not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
    PRIMARY KEY (`id`),
    constraint q_user_answer_user_id_questionnaire_id_question_id_uindex
        unique (user_id, questionnaire_id, question_id)
) ENGINE = InnoDB
  default CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
    comment '用户答案表';

create table q_user_question_record
(
    id                 int auto_increment comment '主键'
        primary key,
    user_id            int                                 not null comment '用户端Id',
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
    update_time        timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '用户答问卷记录表';