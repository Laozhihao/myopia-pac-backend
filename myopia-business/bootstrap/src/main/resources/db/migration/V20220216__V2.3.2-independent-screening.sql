alter table m_student
    add passport varchar(32) null comment '护照';

alter table m_school_student
    add passport varchar(32) null comment '护照' after astigmatism_level;

alter table h_hospital_student
    add passport varchar(32) null comment '护照';

alter table m_screening_plan_school_student
    add passport varchar(32) null comment '护照';

alter table m_vision_screening_result
    add height_and_weight_data json DEFAULT NULL COMMENT '筛查结果--身高体重';
