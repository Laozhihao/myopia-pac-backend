alter table m_school
    add vision_team_count int null comment '视力小分队人数';

alter table m_school
    add screening_config json null comment '筛查类型的配置';

alter table m_school
    add screening_type_config varchar(16) null comment '筛查类型配置, 英文逗号分隔, 0-视力筛查，1-常见病';

update m_school set screening_config = '{"medicalProjectList":["vision","computer_optometry","other_eye_diseases"]}' where screening_config is null