alter table q_user_answer
    add user_type int not null comment '用户类型 0-学生 1-学校' after question_id;