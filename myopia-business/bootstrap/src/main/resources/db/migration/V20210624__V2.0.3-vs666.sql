create table m_device_report_template
(
    id            int auto_increment comment 'id'
        primary key,
    name          varchar(32)                         not null comment '模板名称',
    device_type   tinyint                             not null comment '设备类型 1-VS666',
    template_type tinyint                             not null comment '模板类型 1-VS666模板1',
    create_time   timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '设备报告模板表';

create table m_screening_org_bind_device_report
(
    id                 int auto_increment comment 'id'
        primary key,
    template_id        int                                 not null comment '模板表id',
    screening_org_id   int                                 not null comment '筛查机构Id',
    screening_org_name varchar(32)                         not null comment '筛查机构名称',
    create_time        timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time        timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '筛查机构绑定设备报告模板表';

-- 设备上传的筛查数据
DROP TABLE IF EXISTS `m_device_screening_data`;
CREATE TABLE `m_device_screening_data`
(
    `id`                int UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '唯一主键',
    `screening_org_id`  int UNSIGNED  NOT NULL COMMENT '数据归属的机构id',
    `device_sn`         varchar(32)   NOT NULL COMMENT '设备唯一id',
    `patient_id`        varchar(32)   NOT NULL COMMENT '患者id',
    `patient_name`      varchar(20)   NOT NULL DEFAULT '' COMMENT '受检者名字',
    `patient_age_group` tinyint(1)    NOT NULL DEFAULT -1 COMMENT '受检者年龄段(未知=-1,1=(0M,12M] 2=(12M,36M], 3=(3y,6Y], 4=(6Y-20Y], 5=(20Y,100Y])',
    `patient_gender`    tinyint(1)    NOT NULL DEFAULT -1 COMMENT '受检者性别(性别 男=0  女=1  未知 = -1)',
    `patient_age`       int(1)        NOT NULL DEFAULT -1 COMMENT '受检者年龄/月龄',
    `patient_org`       varchar(64)   NOT NULL DEFAULT '' COMMENT '受检者单位(可能是公司或者学校)',
    `patient_cid`       char(18)      NOT NULL DEFAULT '' COMMENT '受检者身份Id',
    `patient_dept`      varchar(64)   NOT NULL DEFAULT '' COMMENT '受检者部门(班级)',
    `patient_pno`       char(11)      NOT NULL DEFAULT '' COMMENT '受检者电话',
    `check_mode`        tinyint       NOT NULL DEFAULT -1 COMMENT '筛查模式. 双眼模式=0 ; 左眼模式=1; 右眼模式=2; 未知=-1',
    `check_result`      tinyint       NOT NULL DEFAULT -1 COMMENT '筛查结果(1=优, 2=良, 3=差,-1=未知)',
    `check_type`        tinyint       NOT NULL DEFAULT 0 COMMENT '筛查方式(0=个体筛查,1=批量筛查)',
    `left_cyl`          decimal(4, 2) NULL     DEFAULT NULL COMMENT '左眼柱镜',
    `right_cyl`         decimal(4, 2) NULL     DEFAULT NULL COMMENT '右眼柱镜',
    `left_axsi`         decimal(4, 2) NULL     DEFAULT NULL COMMENT '左眼轴位',
    `right_axsi`        decimal(4, 2) NULL     DEFAULT NULL COMMENT '右眼柱位',
    `left_pr`           decimal(4, 2) NULL     DEFAULT NULL COMMENT '左眼瞳孔半径',
    `right_pr`          decimal(4, 2) NULL     DEFAULT NULL COMMENT '右眼瞳孔半径',
    `left_pa`           decimal(4, 2) NULL     DEFAULT NULL COMMENT '左眼等效球镜度',
    `right_pa`          decimal(4, 2) NULL     DEFAULT NULL COMMENT '右眼等效球镜度',
    `pd`                decimal(4, 2) NULL     DEFAULT NULL COMMENT '瞳距',
    `do_check`          tinyint(1)    NOT NULL DEFAULT -1 COMMENT '是否筛查(-1=未知,1=是,0=否)',
    `left_axsi_v`       int           NULL     DEFAULT NULL COMMENT '左垂直⽅向斜视度数',
    `right_axsi_v`      int           NULL     DEFAULT NULL COMMENT '右垂直⽅向斜视度数',
    `left_axsi_h`       int           NULL     DEFAULT NULL COMMENT '左⽔平⽅向斜视度数',
    `right_axsi_h`      int           NULL     DEFAULT NULL COMMENT '右⽔平⽅向斜视度数',
    `red_reflect_left`  int           NULL     DEFAULT NULL COMMENT '红光反射左眼',
    `red_reflect_right` int           NULL     DEFAULT NULL COMMENT '红光反射右眼',
    `screening_time`    timestamp(0)  NULL     DEFAULT NULL COMMENT '筛查时间',
    `update_time`       timestamp(0)  NULL COMMENT '更新时间',
    `create_time`       timestamp(0)  NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_screeningorgid_devicesn_patientid_screeningtime` (`screening_org_id`, `device_sn`, `patient_id`, `screening_time`) USING BTREE COMMENT '数据id_筛查机构id_筛查时间'
);

-- 设备上传的原始数据
DROP TABLE IF EXISTS `m_device_src_data`;
CREATE TABLE `m_device_src_data`
(
    `id`               int UNSIGNED     NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `device_type`      tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '设备类型(0=默认设备,1=vs666)',
    `patient_id`       int UNSIGNED     NOT NULL COMMENT '患者id',
    `device_code`      varchar(32)      NOT NULL DEFAULT '' COMMENT '设备编码',
    `device_sn`        varchar(32)      NOT NULL COMMENT '设备唯一id',
    `src_data`         varchar(512)     NOT NULL COMMENT '原始数据',
    `screening_org_id` int UNSIGNED     NOT NULL COMMENT '筛查机构id',
    `screening_time`   timestamp(0)     NOT NULL COMMENT '筛查时间',
    `create_time`      timestamp(0)     NOT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    PRIMARY KEY (`id`)
);

-- 设备绑定表
DROP TABLE IF EXISTS `m_device_binding`;
CREATE TABLE `m_device_binding`
(
    `id`                       int UNSIGNED NOT NULL AUTO_INCREMENT,
    `device_sn`                varchar(32)  NOT NULL COMMENT '设备唯一id',
    `device_code`              varchar(32)  NOT NULL COMMENT '设备编码',
    `salesperson_name`         varchar(20)  NOT NULL DEFAULT '' COMMENT '销售名字',
    `salesperson_phone`        char(11)     NOT NULL DEFAULT '' COMMENT '销售电话',
    `binding_screening_org_id` int UNSIGNED NOT NULL COMMENT '绑定机构id',
    `customer_name`            varchar(20)  NOT NULL DEFAULT '' COMMENT '客户名字',
    `customer_phone`           char(11)     NOT NULL DEFAULT '' COMMENT '客户电话',
    `sale_date`                timestamp(0) NOT NULL COMMENT '销售时间',
    `remark`                   varchar(500) NOT NULL DEFAULT '' COMMENT '备注',
    `status`                   tinyint      NOT NULL COMMENT '状态: 启用1 禁用(删除) -1',
    `update_time`              timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `create_time`              timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_device_uid` (`device_sn`) USING BTREE COMMENT '设备唯一标识码索引',
    INDEX `idx_binding_screening_org_id` (`binding_screening_org_id`) USING BTREE COMMENT '绑定机构的普通索引'
);

INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code, create_time, update_time) VALUES (335, '获取设备报告模板列表', 'managementDeviceList', 'get:/management/device/list', 0, 0, 1, 333, 1, '2021-06-28 11:54:21', '2021-06-28 11:54:21');
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code, create_time, update_time) VALUES (336, '获取模板Id获取筛查机构', 'managementDeviceGetOrgList', 'get:/management/device/getOrgList/**', 0, 0, 1, 333, 1, '2021-06-28 11:54:58', '2021-06-28 11:54:58');
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code, create_time, update_time) VALUES (337, '配置机构', 'managementDeviceConfiguration', 'post:/management/device/configuration', 0, 0, 1, 333, 1, '2021-06-28 11:55:28', '2021-06-28 11:55:28');
INSERT INTO o_permission (id, name, menu_btn_name, api_url, is_menu, is_page, `order`, pid, system_code, create_time, update_time) VALUES (338, '通过名称获取筛查机构', 'managementDeviceGetOrg', 'get:/management/device/getOrg/**', 0, 0, 1, 333, 1, '2021-06-28 11:56:02', '2021-06-28 11:56:02');