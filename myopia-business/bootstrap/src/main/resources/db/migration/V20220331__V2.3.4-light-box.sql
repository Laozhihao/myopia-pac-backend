alter table m_device
    add type int default 0 null comment '类型 0-默认 1-vs666 2-灯箱 3-体脂秤' after status;