ALTER TABLE m_hospital ADD account_num bigint(10) DEFAULT NULL COMMENT '账号数量';
update m_hospital set account_num = 7 where account_num is null
ALTER TABLE m_screening_organization ADD screening_num bigint(10) DEFAULT NULL COMMENT '筛查人员账号数量';
update m_screening_organization set screening_num = 5 where screening_num is null