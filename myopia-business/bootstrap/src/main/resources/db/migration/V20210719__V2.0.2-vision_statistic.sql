alter table m_district_vision_statistic
    modify avg_left_vision decimal(3, 2) unsigned not null comment '视力情况--平均左眼视力（小数点后一位，默认0.0）';

alter table m_district_vision_statistic
    modify avg_right_vision decimal(3, 2) unsigned not null comment '视力情况--平均右眼视力（小数点后一位，默认0.0）';