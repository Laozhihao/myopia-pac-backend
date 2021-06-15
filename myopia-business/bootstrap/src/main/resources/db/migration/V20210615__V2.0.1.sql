-- parent
create table m_org_cooperation_hospital
(
  id               int auto_increment comment 'id'
    primary key,
  screening_org_id int                                 not null comment '筛查机构Id',
  hospital_id      int                                 not null comment '医院Id',
  is_top           tinyint                             not null default 0 comment '是否置顶',
  create_time      timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
  update_time      timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
  comment '筛查机构合作医院表' charset = utf8mb4;

alter table m_hospital
  add district_province_code tinyint null comment '行政区域-省Code（保留两位）' after district_id;

alter table m_hospital
  add avatar_file_id int null comment '头像资源Id' after address;

alter table m_hospital
  add is_cooperation tinyint default 0 null comment '是否合作医院 0-否 1-是' after remark;

alter table m_parent
  modify wx_nickname varchar(100) character set utf8mb4;

-- add rescreen
CREATE TABLE `m_stat_rescreen` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `screening_org_id` int(11) NOT NULL COMMENT '筛查结果--所属的机构id',
  `src_screening_notice_id` int(11) DEFAULT NULL COMMENT '通知id',
  `task_id` int(11) NOT NULL COMMENT '筛查结果--所属的任务id',
  `plan_id` int(11) NOT NULL COMMENT '筛查结果--所属的计划id',
  `school_id` int(11) NOT NULL COMMENT '筛查结果--执行的学校id',
  `screening_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查日期',
  `rescreen_num` int(11) DEFAULT NULL COMMENT '当日测试人数',
  `wearing_glasses_rescreen_num` int(11) DEFAULT NULL COMMENT '戴镜复测人数',
  `wearing_glasses_rescreen_index_num` int(11) DEFAULT NULL COMMENT '戴镜复测指标数',
  `without_glasses_rescreen_num` int(11) DEFAULT NULL COMMENT '非戴镜复测人数',
  `without_glasses_rescreen_index_num` int(11) DEFAULT NULL COMMENT '非戴镜复测指标数',
  `rescreen_item_num` int(11) DEFAULT NULL COMMENT '复测项次',
  `incorrect_item_num` int(11) DEFAULT NULL COMMENT '错误项次数',
  `incorrect_ratio` float(10,2) DEFAULT NULL COMMENT '错误率/发生率',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '统计时间',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id_school_id` (`plan_id`,`school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复测统计表';


ALTER TABLE `m_school_monitor_statistic` ADD COLUMN `screening_plan_id`  int(10) UNSIGNED NOT NULL COMMENT '监测情况--关联的筛查计划id' AFTER `screening_task_id`;

ALTER TABLE `m_screening_plan_school` ADD COLUMN `quality_controller_name`  varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '机构质控员名字' AFTER `school_name`;
ALTER TABLE `m_screening_plan_school` ADD COLUMN `quality_controller_commander`  varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '机构质控员队长' AFTER `quality_controller_name`;

ALTER TABLE `m_screening_task_org`
  MODIFY COLUMN `quality_controller_name`  varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '筛查任务--机构质控员名字（长度限制未知）' AFTER `screening_org_id`,
  MODIFY COLUMN `quality_controller_commander`  varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '筛查任务--机构质控员队长（长度限制未知）' AFTER `quality_controller_contact`;

-- big screening
alter table `m_district_big_screen_statistic` drop `mapData`;

-- hospital student
DROP TABLE IF EXISTS `h_hospital_student`;
CREATE TABLE `h_hospital_student`
(
  `id`              int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `hospital_id`     int(11) NOT NULL COMMENT '医院id',
  `student_id`      int(11) NOT NULL COMMENT '学生id',
  `create_user_id`  int(11) DEFAULT NULL COMMENT '创建人ID',
  `school_id`       int(11) DEFAULT NULL COMMENT '学校ID',
  `grade_id`        int(11) DEFAULT NULL COMMENT '年级ID',
  `class_id`        int(11) DEFAULT NULL COMMENT '班级ID',
  `sno`             varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '学号',
  `name`            varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学生姓名',
  `gender`          tinyint(1) NOT NULL COMMENT '性别 0-男 1-女',
  `birthday`        timestamp NULL COMMENT '出生日期',
  `nation`          tinyint(4) DEFAULT NULL COMMENT '民族 0-汉族',
  `id_card`         varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '身份证号码',
  `parent_phone`    varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT NULL COMMENT '家长手机号码',
  `mp_parent_phone` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '家长公众号手机号码',
  `province_id`     int(11) DEFAULT NULL COMMENT '省id',
  `city_id`         int(11) DEFAULT NULL COMMENT '市id',
  `area_id`         int(11) DEFAULT NULL COMMENT '区id',
  `town_id`         int(11) DEFAULT NULL COMMENT '镇/乡id',
  `address`         varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '详细地址',
  `status`          tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-启用 1-禁止 2-删除',
  `create_time`     timestamp(0)                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     timestamp(0)                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP (0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id_hospital_id_index`(`hospital_id`, `student_id`) USING BTREE,
  INDEX             `hospital_id_index`(`hospital_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '医院-学生表' ROW_FORMAT = Dynamic;

