create table m_screening_organization
(
    id             int auto_increment comment 'id'
        primary key,
    org_no         bigint                              not null comment '根据规则创建ID',
    create_user_id int                                 null comment '创建人ID',
    gov_dept_id    int                                 not null comment '部门id',
    name           varchar(32)                         not null comment '筛查机构名称',
    type           tinyint                             not null comment '筛查机构类型 0-医院,1-妇幼保健院,2-疾病预防控制中心,3-社区卫生服务中心,4-乡镇卫生院,5-中小学生保健机构,6-其他',
    type_desc      varchar(128)                        null default '' comment '机构类型描述',
    province_code  int                                 not null comment '省代码',
    city_code      int                                 not null comment '市代码',
    area_code      int                                 not null comment '区代码',
    town_code      int                                 not null comment '镇/乡代码',
    address        varchar(128)                        null comment '详细地址',
    remark         varchar(128)                        null comment '说明',
    status         tinyint   default 0                 not null comment '状态 0-启用 1-禁止 2-删除',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '筛查机构表';

create table m_screening_organization_staff
(
    id               int auto_increment comment 'id'
        primary key,
    gov_dept_id      int                                 not null comment '部门id',
    screening_org_id int                                 not null comment '筛查机构表id',
    staff_no         bigint                              not null comment '根据规则创建ID',
    user_id          int                                 not null comment '用户id',
    create_user_id   int                                 null comment '创建人ID',
    remark           varchar(128)                        null comment '说明',
    create_time      timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '机构-人员表';


create table m_hospital
(
    id             int auto_increment comment 'id'
        primary key,
    hospital_no    bigint                              not null comment '根据规则创建ID',
    create_user_id int                                 null comment '创建人ID',
    gov_dept_id    int                                 not null comment '部门id',
    name           varchar(32)                         not null comment '医院名称',
    level          tinyint                             not null comment '等级 0-一甲,1-一乙,2-一丙,3-二甲,4-二乙,5-二丙,6-三特,7-三甲,8-三乙,9-三丙 10-其他',
    level_desc     varchar(32)                         null comment '等级描述',
    type           tinyint                             not null comment '医院类型 0-定点医院 1-非定点医院',
    kind           tinyint                             not null comment '医院性质 0-公立 1-私立',
    province_code  int                                 not null comment '省代码',
    city_code      int                                 not null comment '市代码',
    area_code      int                                 not null comment '区代码',
    town_code      int                                 not null comment '镇/乡代码',
    address        varchar(128)                        null comment '详细地址',
    remark         varchar(128)                        null comment '说明',
    status         tinyint   default 0                 not null comment '状态 0-启用 1-禁止 2-删除',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '医院表';


create table m_hospital_staff
(
    id             int auto_increment comment 'id'
        primary key,
    create_user_id int                                 null comment '创建人ID',
    hospital_id    int                                 not null comment '医院id',
    user_id        int                                 not null comment '用户id',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '医院-员工表';



create table m_school
(
    id                  int auto_increment comment 'id'
        primary key,
    school_no           bigint       not null comment '根据规则创建ID',
    create_user_id      int          null comment '创建人ID',
    gov_dept_id         int          not null comment '部门id',
    name                varchar(32)  not null comment '学校名称',
    kind                tinyint      not null comment '学校性质 0-公办 1-私办 2-其他',
    kind_desc           varchar(32)  not null comment '学校性质描述 0-公办 1-私办 2-其他',
    lodge_status        tinyint      not null comment '寄宿状态 0-全部住校 1-部分住校 2-不住校',
    type                tinyint      not null comment '学校类型 0-小学,1-初级中学,2-高级中学,3-完全中学,4-九年一贯制学校,5-十二年一贯制学校,6-职业高中,7其他',
    total_online        int          not null default 0 comment '在校总人数',
    total_online_male   int          not null default 0 comment '在校-男生人数',
    total_online_female int          not null default 0 comment '在校-女生人数',
    total_lodge         int          not null default 0 comment '住校总人数',
    total_lodge_male    int          not null default 0 comment '住校-男生人数',
    total_lodge_female  int          not null default 0 comment '住校-女生人数',
    province_code       int          not null comment '省代码',
    city_code           int          not null comment '市代码',
    area_code           int          not null comment '区代码',
    town_code           int          not null comment '镇/乡代码',
    address             varchar(128) null comment '详细地址',
    remark              varchar(128) null comment '说明',
    status              tinyint               default 0 not null comment '状态 0-启用 1-禁止 2-删除',
    create_time         timestamp             default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time         timestamp             default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'

) comment '学校表';

create table m_school_staff
(
    id             int auto_increment comment 'id'
        primary key,
    school_id      int                                 not null comment '学校id',
    user_id        int                                 not null comment '用户表id',
    create_user_id int                                 null comment '创建人ID',
    gov_dept_id    int                                 not null comment '部门id',
    remark         varchar(128)                        null comment '说明',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'

) comment '学校-员工表';


create table m_school_grade
(
    id             int auto_increment comment 'id'
        primary key,
    create_user_id int                                 null comment '创建人ID',
    school_id      int                                 not null comment '学校ID',
    name           varchar(32)                         not null comment '年级名称',
    status         tinyint   default 0                 not null comment '状态 0-启用 1-禁止 2-删除',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '学校-年级表';

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
) comment '学校-班级表';

create table m_student
(
    id                  int auto_increment comment 'id'
        primary key,
    school_id           int                                 not null comment '学校ID',
    student_no          bigint                              not null comment '根据规则创建ID',
    create_user_id      int                                 null comment '创建人ID',
    sno                 int                                 not null comment '学号',
    grade_id            int                                 not null comment '班级id',
    class_id            int                                 not null comment '年级ID',
    name                varchar(8)                          not null comment '学生姓名',
    gender              tinyint(1)                          not null comment '性别 1-男 2-女',
    birthday            timestamp                           null comment '出生日期',
    nation              tinyint                             not null comment '民族 0-汉族',
    id_card             varchar(32)                         not null comment '身份证号码',
    parent_phone        varchar(16)                         not null comment '家长手机号码',
    mp_parent_phone     varchar(16)                         null comment '家长公众号手机号码',
    province_code       int                                 not null comment '省代码',
    city_code           int                                 not null comment '市代码',
    area_code           int                                 not null comment '区代码',
    town_code           int                                 not null comment '镇/乡代码',
    address             varchar(128)                        null comment '详细地址',
    current_situation   varchar(128)                        null comment '当前情况',
    labels              varchar(128)                        null comment '视力标签',
    screening_count     int       default 0                 not null comment '视力筛查次数',
    questionnaire_count int       default 0                 not null comment '问卷数',
    last_screening_time timestamp                           null comment '最近筛选次数',
    status              tinyint   default 0                 not null comment '状态 0-启用 1-禁止 2-删除',
    create_time         timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time         timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '学校-学生表';


DROP TABLE IF EXISTS `m_district`;
CREATE TABLE `m_district`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '行政区ID',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '行政区名称',
  `code` bigint(12) NOT NULL COMMENT '行政区代码',
  `parent_code` bigint(12) NOT NULL COMMENT '上级行政区代码（省级统一为100000000000）',
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