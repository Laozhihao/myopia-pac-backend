-- 问卷表
CREATE TABLE `q_questionnaire` (
    `id` INT NOT NULL auto_increment COMMENT '主键',
    `title` VARCHAR ( 255 ) NOT NULL COMMENT '问卷标题',
    `district_id` INT DEFAULT NULL COMMENT '区域ID',
    `year` INT NOT NULL COMMENT '年份，如：2022',
    `pid` INT NOT NULL COMMENT '父ID，没有上级为-1',
    `type` TINYINT NOT NULL COMMENT '问卷类型',
    `status` TINYINT DEFAULT NULL COMMENT '问卷状态 0-启用 1-禁用',
    `qes_url` VARCHAR ( 150 ) DEFAULT NULL COMMENT 'qes文件地址',
    `page_json` json DEFAULT NULL COMMENT '页面json数据',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY ( `id` )
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4  COMMENT '问卷表';

-- 题目表
CREATE TABLE `q_question` (
    `id` INT NOT NULL auto_increment COMMENT '主键',
    `type` VARCHAR ( 30 ) NOT NULL COMMENT '问题类型，如：radio（单选）、checkbox（多选）、input（填空）',
    `title` VARCHAR ( 255 ) NOT NULL COMMENT '问题题目',
    `attribute` json DEFAULT NULL COMMENT '问题属性',
    `options` json NOT NULL COMMENT '问题的答案选项',
    `same_question_group_id` VARCHAR ( 128 ) NULL COMMENT '相同问题uuid',
    `serial_number` VARCHAR ( 15 ) DEFAULT NULL COMMENT '问题的序号',
    `pid` INT NULL COMMENT '父ID，没有上级为-1',
    `icon_name` VARCHAR ( 16 ) NULL COMMENT '图标信息',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY ( `id` )
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4  COMMENT '题目表';

-- 问卷和题目关系表
CREATE TABLE q_questionnaire_question (
    id INT auto_increment PRIMARY KEY,
    questionnaire_id INT NOT NULL COMMENT '问卷ID',
    question_id INT NOT NULL COMMENT '题目ID',
    pid INT NOT NULL COMMENT '父题目Id，没有父题目的则为-1',
    serial_number VARCHAR ( 15 ) NULL COMMENT '自定义问题的序号',
    sort TINYINT NOT NULL COMMENT '排序',
    jump_ids json NULL COMMENT '跳转题目Ids',
    required boolean NOT NULL DEFAULT TRUE COMMENT '是否必填',
    CONSTRAINT questionnaire_question_unique_index UNIQUE ( questionnaire_id, question_id )
) COMMENT '问卷和题目关系表';

-- qes字段映射关系表
CREATE TABLE `q_qes_field_mapping` (
    `id` INT auto_increment PRIMARY KEY COMMENT '主键',
    `questionnaire_id` INT NOT NULL COMMENT '问卷ID',
    `qes_field` VARCHAR ( 60 ) NOT NULL COMMENT 'qes字段',
    `system_field` VARCHAR ( 60 ) NOT NULL COMMENT '系统字段',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT 'qes字段映射关系表';

-- 用户答案表
CREATE TABLE `q_user_answer` (
    `id` INT NOT NULL auto_increment COMMENT '主键',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `questionnaire_id` INT NOT NULL COMMENT '问卷ID',
    `question_id` INT NOT NULL COMMENT '问题ID',
    `question_title` VARCHAR ( 128 ) NOT NULL COMMENT '问题标题',
    `answer` json DEFAULT NULL COMMENT '用户答案',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY ( `id` ),
    CONSTRAINT q_user_answer_user_id_questionnaire_id_question_id_uindex UNIQUE ( user_id, questionnaire_id, question_id )
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT '用户答案表';

-- 用户答问卷记录表
CREATE TABLE q_user_question_record (
    id INT auto_increment COMMENT '主键' PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户端Id',
    questionnaire_id INT NOT NULL COMMENT '问卷Id',
    plan_id INT NULL COMMENT '计划Id',
    task_id INT NULL COMMENT '任务Id',
    notice_id INT NULL COMMENT '通知Id',
    gov_id INT NULL COMMENT '政府Id',
    school_id INT NULL COMMENT '学校Id',
    student_id INT NULL COMMENT '学生Id',
    questionnaire_type INT NULL COMMENT '问卷类型',
    STATUS TINYINT NULL COMMENT '状态 0-未开始 1-进行中 2-结束',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT '用户答问卷记录表';