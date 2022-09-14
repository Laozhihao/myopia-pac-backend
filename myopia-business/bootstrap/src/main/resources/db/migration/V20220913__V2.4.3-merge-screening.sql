ALTER TABLE m_screening_organization ADD screening_config json NULL COMMENT '筛查类型配置';
UPDATE m_screening_organization
SET screening_config = '{"screeningTypeList":[0],"channel":"Official","medicalProjectList":["vision","computer_optometry","other_eye_diseases"]}';