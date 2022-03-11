alter table m_screening_organization_staff
    add type int(10) DEFAULT '0' COMMENT '筛查人员类型（0普通筛查人员，1自动生成的筛查人员）';

update m_screening_organization_staff set type = 0 WHERE ISNULL(type);