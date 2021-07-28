alter table m_device_screening_data modify left_cyl decimal(7,3) null comment '左眼柱镜';
alter table m_device_screening_data modify right_cyl decimal(7,3) null comment '右眼柱镜';
alter table m_device_screening_data modify left_axsi decimal(7,3) null comment '左眼轴位';
alter table m_device_screening_data modify right_axsi decimal(7,3) null comment '右眼轴位';
alter table m_device_screening_data modify left_pr decimal(7,3) null comment '左眼瞳孔半径';
alter table m_device_screening_data modify right_pr decimal(7,3) null comment '右眼瞳孔半径';
alter table m_device_screening_data modify left_pa decimal(7,3) null comment '左眼等效球镜度';
alter table m_device_screening_data modify right_pa decimal(7,3) null comment '右眼等效球镜度';
alter table m_device_screening_data modify left_sph decimal(7,3) null comment '左眼球镜';
alter table m_device_screening_data modify right_sph decimal(7,3) null comment '右眼球镜';
alter table m_device_screening_data modify pd decimal(7,3) null comment '瞳距';

alter table m_device_source_data modify patient_id varchar(64) not null comment '患者id';