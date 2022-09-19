alter table m_device
    add org_type int default 0 not null comment '机构类型 0-筛查机构 1-医院 2-学校' after binding_screening_org_id;
alter table m_device
    add bluetooth_mac varchar(32) null comment '蓝牙MAC地址' after type;

create table h_image_original
(
    id            int auto_increment comment 'id'
        primary key,
    file_id       int                                 not null comment '文件Id',
    patient_id    int                                 not null comment '患者Id',
    hospital_id   int                                 not null comment '医院Id',
    bluetooth_mac varchar(32)                         not null comment '蓝牙mac地址',
    md5           varchar(128)                        not null comment 'md5',
    create_time   timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint h_image_original_pk
        unique (md5)
)
    comment '图像原始表';

create table h_image_detail
(
    id                int auto_increment primary key comment 'id',
    image_original_id int                                 not null comment '图像原始表Id',
    file_id           int                                 not null comment '文件Id',
    patient_id        int                                 not null comment '患者Id',
    hospital_id       int                                 not null comment '医院Id',
    dcm_json          json                                null comment 'DICOM数据',
    batch_no          varchar(256)                        not null comment '批次号',
    create_time       timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time       timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '图像详情表';

alter table h_medical_record
    add fundus json null comment '眼底检查' after `tosca`;

alter table m_device
    add constraint m_device_pk unique (bluetooth_mac);

alter table m_device
    modify salesperson_name varchar(20) default '' null comment '销售名字';

alter table m_device
    modify customer_name varchar(20) default '' null comment '客户名字';