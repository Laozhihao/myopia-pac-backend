alter table m_district_big_screen_statistic
    add radar_chart_data json null comment '雷达图数据' after avg_vision;

alter table m_district_big_screen_statistic
    add ranking_data json null comment '排行榜数据' after radar_chart_data;