DROP TABLE IF EXISTS m_screening_organization;
create table m_screening_organization
(
    id                  int auto_increment comment 'id'
        primary key,
    create_user_id      int                                 null comment '创建人ID',
    gov_dept_id         int                                 not null comment '部门ID',
    district_id         int                                 not null comment '行政区域ID',
    district_detail     varchar(512)                        not null comment '行政区域json',
    name                varchar(32)                         not null comment '筛查机构名称',
    type                tinyint                             not null comment '筛查机构类型 0-医院,1-妇幼保健院,2-疾病预防控制中心,3-社区卫生服务中心,4-乡镇卫生院,5-中小学生保健机构,6-其他',
    type_desc           varchar(128)                        null default '' comment '机构类型描述',
    config_type         tinyint                             not null comment '配置 0-省级配置 1-单点配置',
    phone               varchar(32)                         null comment '联系方式',
    province_code       bigint                              null comment '省代码',
    city_code           bigint                              null comment '市代码',
    area_code           bigint                              null comment '区代码',
    town_code           bigint                              null comment '镇/乡代码',
    address             varchar(128)                        null comment '详细地址',
    remark              varchar(128)                        null comment '说明',
    notification_config json                                null comment '告知书配置',
    status              tinyint   default 0                 not null comment '状态 0-启用 1-禁止 2-删除',
    create_time         timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time         timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '筛查机构表';

DROP TABLE IF EXISTS m_screening_organization_admin;
create table m_screening_organization_admin
(
    id               int auto_increment comment 'id'
        primary key,
    create_user_id   int                                 null comment '创建人ID',
    screening_org_id int                                 not null comment '筛查机构表ID',
    user_id          int                                 not null comment '用户ID',
    gov_dept_id      int                                 not null comment '部门ID',
    create_time      timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '机构-管理员表';

DROP TABLE IF EXISTS m_screening_organization_staff;
create table m_screening_organization_staff
(
    id               int auto_increment comment 'id'
        primary key,
    gov_dept_id      int                                 not null comment '部门ID',
    screening_org_id int                                 not null comment '筛查机构表ID',
    user_id          int                                 not null comment '用户ID',
    create_user_id   int                                 null comment '创建人ID',
    remark           varchar(128)                        null comment '说明',
    create_time      timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '机构-人员表';

DROP TABLE IF EXISTS m_hospital;
create table m_hospital
(
    id              int auto_increment comment 'id'
        primary key,
    create_user_id  int                                 null comment '创建人ID',
    gov_dept_id     int                                 not null comment '部门ID',
    district_id     int                                 not null comment '行政区域ID',
    district_detail varchar(512)                        not null comment '行政区域json',
    name            varchar(32)                         not null comment '医院名称',
    level           tinyint                             not null comment '等级 0-一甲,1-一乙,2-一丙,3-二甲,4-二乙,5-二丙,6-三特,7-三甲,8-三乙,9-三丙 10-其他',
    level_desc      varchar(32)                         null comment '等级描述',
    type            tinyint                             not null comment '医院类型 0-定点医院 1-非定点医院',
    kind            tinyint                             not null comment '医院性质 0-公立 1-私立',
    province_code   bigint                              null comment '省代码',
    city_code       bigint                              null comment '市代码',
    area_code       bigint                              null comment '区代码',
    town_code       bigint                              null comment '镇/乡代码',
    address         varchar(128)                        null comment '详细地址',
    remark          varchar(128)                        null comment '说明',
    status          tinyint   default 0                 not null comment '状态 0-启用 1-禁止 2-删除',
    create_time     timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '医院表';

DROP TABLE IF EXISTS m_hospital_admin;
create table m_hospital_admin
(
    id             int auto_increment comment 'id'
        primary key,
    create_user_id int                                 null comment '创建人ID',
    hospital_id    int                                 not null comment '医院id',
    user_id        int                                 not null comment '用户id',
    gov_dept_id    int                                 not null comment '部门ID',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '医院-管理员表';

DROP TABLE IF EXISTS m_school;
create table m_school
(
    id              int auto_increment comment 'id'
        primary key,
    school_no       varchar(64)                         not null comment '学校编号',
    create_user_id  int                                 null comment '创建人ID',
    gov_dept_id     int                                 not null comment '部门ID',
    district_id     int                                 not null comment '行政区域ID',
    district_detail varchar(512)                        not null comment '行政区域json',
    name            varchar(32)                         not null comment '学校名称',
    kind            tinyint                             not null comment '学校性质 0-公办 1-私办 2-其他',
    kind_desc       varchar(32)                         null comment '学校性质描述 0-公办 1-私办 2-其他',
    lodge_status    tinyint                             null comment '寄宿状态 0-全部住校 1-部分住校 2-不住校',
    type            tinyint                             not null comment '学校类型 0-小学,1-初级中学,2-高级中学,3-完全中学,4-九年一贯制学校,5-十二年一贯制学校,6-职业高中,7其他',
    province_code   bigint                              null comment '省代码',
    city_code       bigint                              null comment '市代码',
    area_code       bigint                              null comment '区代码',
    town_code       bigint                              null comment '镇/乡代码',
    address         varchar(128)                        null comment '详细地址',
    remark          varchar(128)                        null comment '说明',
    status          tinyint   default 0                 not null comment '状态 0-启用 1-禁止 2-删除',
    create_time     timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '学校表';

DROP TABLE IF EXISTS m_school_admin;
create table m_school_admin
(
    id             int auto_increment comment 'id'
        primary key,
    create_user_id int                                 null comment '创建人ID',
    school_id      int                                 not null comment '学校ID',
    user_id        int                                 not null comment '用户表ID',
    gov_dept_id    int                                 not null comment '部门ID',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'

)
    comment '学校-管理表';

DROP TABLE IF EXISTS m_school_grade;
create table m_school_grade
(
    id             int auto_increment comment 'id'
        primary key,
    create_user_id int                                 null comment '创建人ID',
    school_id      int                                 not null comment '学校ID',
    grade_code     varchar(8)                          not null comment '年级编码',
    name           varchar(32)                         not null comment '年级名称',
    status         tinyint   default 0                 not null comment '状态 0-启用 1-禁止 2-删除',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '学校-年级表';

DROP TABLE IF EXISTS m_school_class;
create table m_school_class
(
    id             int auto_increment comment 'id'
        primary key,
    grade_id       int                                 not null comment '年级ID',
    create_user_id int                                 null comment '创建人ID',
    school_id      int                                 not null comment '学校ID',
    name           varchar(32)                         not null comment '班级名称',
    seat_count     int                                 null comment '座位数',
    status         tinyint   default 0                 not null comment '状态 0-启用 1-禁止 2-删除',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '学校-班级表';

DROP TABLE IF EXISTS m_student;
create table m_student
(
    id                  int auto_increment comment 'id'
        primary key,
    school_no           varchar(64)                         null comment '学校编号',
    create_user_id      int                                 null comment '创建人ID',
    sno                 varchar(64)                         not null comment '学号',
    grade_id            int                                 null comment '年级ID',
    grade_type          tinyint                             null comment '学龄段',
    class_id            int                                 null comment '班级ID',
    name                varchar(32)                         not null comment '学生姓名',
    gender              tinyint(1)                          not null comment '性别 1-男 2-女',
    birthday            timestamp                           null comment '出生日期',
    nation              tinyint                             null comment '民族 0-汉族',
    id_card             varchar(32)                         not null comment '身份证号码',
    parent_phone        varchar(16)                         null comment '家长手机号码',
    mp_parent_phone     varchar(16)                         null comment '家长公众号手机号码',
    province_code       bigint                              null comment '省代码',
    city_code           bigint                              null comment '市代码',
    area_code           bigint                              null comment '区代码',
    town_code           bigint                              null comment '镇/乡代码',
    address             varchar(128)                        null comment '详细地址',
    avatar              varchar(128)                        null comment '头像',
    current_situation   varchar(128)                        null comment '当前情况',
    vision_label        tinyint unsigned                    null comment '视力标签 0-零级、1-一级、2-二级、3-三级',
    last_screening_time timestamp                           null comment '最近筛选时间',
    remark              varchar(256)                        null comment '备注',
    status              tinyint   default 0                 not null comment '状态 0-启用 1-禁止 2-删除',
    create_time         timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time         timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '学校-学生表' charset = utf8mb4;

create unique index m_school_school_no_uindex
    on m_school (school_no);

DROP TABLE IF EXISTS `m_district`;
CREATE TABLE `m_district`  (
                               `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '行政区ID',
                               `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '行政区名称',
                               `code` bigint(12) NOT NULL COMMENT '行政区代码',
                               `parent_code` bigint(12) NOT NULL COMMENT '上级行政区代码（省级统一为100000000000）',
                               `area_code` tinyint NULL COMMENT '片区代码:每个省、自治区﹑直辖市按照社会经济发展不同水平进行片区编码，片区代码按照经济水平，省会〈好片)=1，中片=2，差片=3',
                               `monitor_code` tinyint NULL COMMENT '监测点代码:每个省、自治区、直辖市按照城区﹑郊县/区进行编码，城区=1，郊县/区=2',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '行政区域表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `m_government_department`;
CREATE TABLE `m_government_department`  (
                                            `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '部门ID',
                                            `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '部门名称',
                                            `pid` int(11) NOT NULL COMMENT '上级部门ID',
                                            `district_id` int(11) NOT NULL COMMENT '所属行政区ID',
                                            `create_user_id` int(11) DEFAULT NULL COMMENT '创建人',
                                            `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态：0-启用 1-禁止 2-删除',
                                            `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '备注',
                                            `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '政府部门表' ROW_FORMAT = Dynamic;

-- 初始化部门表
INSERT INTO `m_government_department`(id, `name`, `pid`, `district_id`, `create_user_id`) VALUES (1, '运行中心', -1, -1, -1);

DROP TABLE IF EXISTS m_notice;
create table m_notice
(
    id             int auto_increment comment 'id'
        primary key,
    create_user_id int                                 null comment '创建人',
    link_id        int                                 null comment '关联ID',
    notice_user_id int                                 null comment '通知的userId',
    type           tinyint                             not null comment '类型 0-站内信 1-筛查通知',
    status         tinyint   default 0                 not null comment '状态 0-未读 1-已读 2-删除',
    title          varchar(128)                        null comment '标题',
    content        varchar(512)                        not null comment '内容',
    download_url   varchar(1024)                       null comment '文件url',
    start_time     timestamp                           null comment '开时时间',
    end_time       timestamp                           null comment '结束时间',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '消息表' charset = utf8mb4;

DROP TABLE IF EXISTS m_template;
create table m_template
(
    id          int auto_increment comment 'id'
        primary key,
    type        tinyint                             not null comment '1-档案卡 2-筛查报告',
    name        varchar(32)                         not null comment '模板名称',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '模板表';

DROP TABLE IF EXISTS m_template_district;
create table m_template_district
(
    id            int auto_increment comment 'id'
        primary key,
    template_id   int                                 not null comment '模板ID',
    district_id   int                                 not null comment '行政部门 使用模板的省份',
    district_name varchar(16)                         not null comment '省份名字',
    create_time   timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '模板区域表';

INSERT INTO m_template (id, type, name, create_time, update_time)
VALUES (1, 1, '学生档案卡-模板1', '2021-01-22 12:08:23', '2021-01-22 12:08:23');
INSERT INTO m_template (id, type, name, create_time, update_time)
VALUES (2, 1, '学生档案卡-模板2', '2021-01-22 12:08:23', '2021-01-22 12:08:23');
INSERT INTO m_template (id, type, name, create_time, update_time)
VALUES (3, 1, '学生档案卡-模板3', '2021-01-22 12:08:23', '2021-01-22 12:08:23');
INSERT INTO m_template (id, type, name, create_time, update_time)
VALUES (4, 1, '学生档案卡-模板4', '2021-01-22 12:08:23', '2021-01-22 12:08:23');
INSERT INTO m_template (id, type, name, create_time, update_time)
VALUES (5, 2, '筛查报告-模板1', '2021-01-22 12:08:57', '2021-01-22 12:08:57');
INSERT INTO m_template (id, type, name, create_time, update_time)
VALUES (6, 2, '筛查报告-模板2', '2021-01-22 12:08:57', '2021-01-22 12:08:57');
INSERT INTO m_template (id, type, name, create_time, update_time)
VALUES (7, 2, '筛查报告-模板3', '2021-01-22 12:08:57', '2021-01-22 12:08:57');
INSERT INTO m_template (id, type, name, create_time, update_time)
VALUES (8, 2, '筛查报告-模板4', '2021-01-22 12:08:57', '2021-01-22 12:08:57');

create unique index m_hospital_name_uindex
    on m_hospital (name);

create unique index m_screening_organization_name_uindex
    on m_screening_organization (name);

create unique index m_school_name_uindex
    on m_school (name);