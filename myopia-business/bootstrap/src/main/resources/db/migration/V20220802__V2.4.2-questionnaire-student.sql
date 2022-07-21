alter table q_user_answer
    add user_type int not null comment '用户类型 0-学生 1-学校' after question_id;

alter table q_question
    add sub_title varchar(128) null comment '副标题' after title;