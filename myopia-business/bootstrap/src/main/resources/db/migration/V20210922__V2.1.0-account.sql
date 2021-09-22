alter table m_screening_organization_admin
    add status tinyint default 0 null comment '状态 0-启用 1-禁止 2-删除';