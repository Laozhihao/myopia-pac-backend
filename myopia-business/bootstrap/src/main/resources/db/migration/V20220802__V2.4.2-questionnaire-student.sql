alter table q_user_answer
    add user_type int not null comment '用户类型 0-学生 1-学校' after question_id;

alter table q_question
    add sub_title varchar(128) null comment '副标题' after title;

alter table q_user_question_record
    add user_type int null comment '0-学生 1-学校' after user_id;

create unique index index_name
    on q_user_question_record (user_id, user_type, questionnaire_id);

alter table q_user_answer
    modify record_id int null comment '记录表Id';

drop index q_user_answer_user_id_questionnaire_id_question_id_uindex on q_user_answer;


CREATE TABLE `q_user_answer_progress`
(
    `id`               INT          NOT NULL auto_increment COMMENT '主键',
    `user_id`          int          not null comment '用户id',
    `user_type`        int          not null comment '用户类型',
    `current_step`     varchar(128) null comment 'currentStep',
    `current_side_bar` varchar(128) null comment 'currentSideBar',
    `create_time`      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4 COMMENT '用户答案进度表';

create index q_user_answer_progress_user_id_user_type_index
    on q_user_answer_progress (user_id, user_type);