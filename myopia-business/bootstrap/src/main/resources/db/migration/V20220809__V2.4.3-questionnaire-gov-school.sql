alter table q_questionnaire_question
    add is_hidden boolean null comment '是否隐藏';

alter table q_questionnaire_question
    add qes_serial_number json null comment 'qes字段序号';

alter table q_user_question_record
    add record_type int null comment '汇总类型 1-汇总' after status;