alter table m_screening_plan_school_student
    add mock_status tinyint default 0 not null comment 'mock状态, 0mock, -1 非mock' after parent_phone;