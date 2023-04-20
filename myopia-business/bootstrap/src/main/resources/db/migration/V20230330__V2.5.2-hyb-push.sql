alter table m_parent
    add hyb_bind_status tinyint default 0 not null comment '护眼宝绑定状态 0-未绑定 1-已绑定 2-解除绑定';