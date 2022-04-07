-- sql 加上注释说明描述
alter table m_stat_rescreen
    add type int default 0 null comment '0-视力筛查 1-常见病' after school_id;

alter table m_stat_rescreen
    add physique_rescreen_num int null comment '体格复查人数';

alter table m_stat_rescreen
    add physique_index_num int null comment '体格复查指数';