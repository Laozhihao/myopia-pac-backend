-- ----------------------------
-- Table structure for m_screening_notice
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_notice`;
CREATE TABLE `m_screening_notice`
(
    `id`                int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `title`             varchar(25)    NOT NULL COMMENT '筛查通知--标题（最大25个字符）',
    `content`           varchar(10000) NOT NULL COMMENT '筛查通知--通知内容（长度未知）',
    `start_time`        timestamp NULL COMMENT '筛查通知--开始时间（时间戳）',
    `end_time`          timestamp NULL COMMENT '筛查通知--结束时间（时间戳）',
    `type`              tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '筛查通知--通知类型（0是筛查通知-政府、1是筛查任务通知-筛查机构）',
    `gov_dept_id`       int(10) unsigned NOT NULL COMMENT '筛查通知--所处部门id',
    `district_id`       int(10) unsigned NOT NULL COMMENT '筛查通知--所处地区id',
    `screening_task_id` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '筛查通知--来源的筛查任务id（type为1有）',
    `release_status`    tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '筛查通知--通知状态（0未发布、1已发布）',
    `release_time`      timestamp NULL COMMENT '筛查通知--发布时间（时间戳 ）',
    `operation_version` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '筛查通知--操作人版本（版本自增，便于解决数据修改覆盖）',
    `create_user_id`    int(10) unsigned NOT NULL DEFAULT 0 COMMENT '筛查通知--创建人id  ',
    `create_time`       timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查通知--创建时间（时间戳）',
    `operator_id`       int(10) NOT NULL DEFAULT 0 COMMENT '筛查通知--最后操作人id  ',
    `operate_time`      timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '筛查通知--最后操作时间（时间戳）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛查通知表';

-- ----------------------------
-- Table structure for m_screening_notice_dept_org
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_notice_dept_org`;
CREATE TABLE `m_screening_notice_dept_org`
(
    `id`                  int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_notice_id` int(10) unsigned NOT NULL COMMENT '筛查通知--筛查通知表id',
    `district_id`         int(10) unsigned NOT NULL COMMENT '筛查通知--接收对象所在的区域id',
    `accept_org_id`       int(10) unsigned NOT NULL COMMENT '筛查通知--接收通知对象的id（机构id 或者 部门id）',
    `screening_task_plan_id`       int(10) unsigned NOT NULL DEFAULT 0 COMMENT '筛查通知--该通知对应的筛查任务或筛查计划ID',
    `operation_status`    tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '筛查通知--操作状态（0未读 1 是已读 2是删除 3是已读已创建）',
    `operator_id`         int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查通知--操作人id（查看或者编辑的人id）',
    `create_time`         timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛查通知通知到的部门或者机构表';

-- ----------------------------
-- Table structure for m_screening_task
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_task`;
CREATE TABLE `m_screening_task`
(
    `id`                  int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_notice_id` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '筛查任务--所属的通知id，自己创建时默认0',
    `title`               varchar(25)    NOT NULL COMMENT '筛查任务--标题',
    `content`             varchar(10000) NOT NULL COMMENT '筛查任务--内容',
    `start_time`          timestamp NULL COMMENT '筛查任务--开始时间（时间戳）',
    `end_time`            timestamp NULL COMMENT '筛查任务--结束时间（时间戳）',
    `gov_dept_id`         int(10) unsigned NOT NULL COMMENT '筛查任务--所处部门id',
    `district_id`         int(10) unsigned NOT NULL COMMENT '筛查任务--所处区域id',
    `release_status`      tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '筛查任务--发布状态 （0未发布 1已发布）',
    `release_time`        timestamp NULL DEFAULT NULL COMMENT '筛查任务--发布时间（时间戳）',
    `create_user_id`      int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查任务--创建者ID',
    `create_time`         timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查任务--创建时间（时间戳）',
    `operator_id`         int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查任务--最后操作人id  ',
    `operate_time`        timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '筛查任务--最后操作时间（时间戳）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛查通知任务表';

-- ----------------------------
-- Table structure for m_screening_task_org
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_task_org`;
CREATE TABLE `m_screening_task_org`
(
    `id`                           int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_task_id`            int(10) unsigned NOT NULL COMMENT '筛查任务--筛查任务id',
    `screening_org_id`             int(10) unsigned NOT NULL COMMENT '筛查任务--筛查机构id',
    `quality_controller_name`      varchar(25) NOT NULL COMMENT '筛查任务--机构质控员名字（长度限制未知）',
    `quality_controller_contact`   varchar(25) NOT NULL COMMENT '筛查任务--机构质控员联系方式（长度限制未知）',
    `quality_controller_commander` varchar(25) NOT NULL COMMENT '筛查任务--机构质控员队长（长度限制未知）',
    `create_time`                  timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛查任务关联的机构表';

-- ----------------------------
-- Table structure for m_screening_plan
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_plan`;
CREATE TABLE `m_screening_plan`
(
    `id`                int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `src_screening_notice_id` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '筛查计划--所属的筛查源通知id（也即task的来源通知id），自己创建时默认0',
    `screening_task_id` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '筛查计划--所属的筛查任务id，自己创建时默认0',
    `title`             varchar(25)    NOT NULL COMMENT '筛查计划--标题',
    `content`           varchar(10000) NOT NULL COMMENT '筛查计划--内容',
    `start_time`        timestamp NULL COMMENT '筛查计划--开始时间（时间戳）',
    `end_time`          timestamp NULL COMMENT '筛查计划--结束时间（时间戳）',
    `gov_dept_id`       int(10) unsigned NOT NULL DEFAULT 0 COMMENT '筛查计划--所处部门id',
    `district_id`       int(10) unsigned NOT NULL DEFAULT 0 COMMENT '筛查计划--所处区域id',
    `screening_org_id`  int(10) unsigned NOT NULL COMMENT '筛查计划--指定的筛查机构id',
    `student_numbers`   int(10) unsigned NOT NULL DEFAULT 0 COMMENT '筛查计划--计划的学生总数',
    `release_status`    tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '筛查计划--发布状态 （0未发布 1已发布）',
    `release_time`      timestamp NULL DEFAULT NULL COMMENT '筛查计划--发布时间（时间戳）',
    `create_user_id`    int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查计划--创建者ID',
    `create_time`       timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查计划--创建时间（时间戳）',
    `operator_id`       int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查计划--最后操作人id  ',
    `operate_time`      timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '筛查计划--最后操作时间（时间戳）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛查通知计划表';

-- ----------------------------
-- Table structure for m_screening_plan_school
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_plan_school`;
CREATE TABLE `m_screening_plan_school`
(
    `id`                int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_plan_id` int(10) unsigned NOT NULL COMMENT '筛查计划--计划id ',
    `screening_org_id`  int(10) unsigned NOT NULL DEFAULT 0 COMMENT '筛查计划--指定的筛查机构id',
    `school_id`         int(10) unsigned NOT NULL COMMENT '筛查计划--执行的学校id',
    `school_name`       varchar(32)        DEFAULT NULL COMMENT '筛查计划--学校名字',
    `create_time`       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛查计划关联的学校表';

-- ----------------------------
-- Table structure for m_screening_plan_school_student
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_plan_school_student`;
CREATE TABLE `m_screening_plan_school_student`
(
    `id`                int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `src_screening_notice_id` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '筛查计划--所属的筛查源通知id（也即task的来源通知id），自己创建时默认0',
    `screening_task_id` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '筛查计划--所属的筛查任务id，自己创建时默认0',
    `screening_plan_id` int(10) unsigned NOT NULL COMMENT '筛查计划--计划id ',
    `screening_org_id`  int(10) unsigned NOT NULL DEFAULT 0 COMMENT '筛查计划--指定的筛查机构id',
    `district_id`       int(10) unsigned NOT NULL DEFAULT 0 COMMENT '筛查计划--所处区域id',
    `school_id`         int(10) unsigned NOT NULL COMMENT '筛查计划--执行的学校id',
    `school_name`       varchar(32)  NOT NULL COMMENT '筛查计划--执行的学校名字',
    `grade_id`          int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查计划--参与筛查的学生年级ID',
    `grade_name`        varchar(32) null comment '年级名称',
    `grade_type`        tinyint null comment '学龄段',
    `class_id`          int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查计划--参与筛查的学生班级ID',
    `class_name`        varchar(32) null comment '班级名称',
    `student_id`        int(10) unsigned NOT NULL COMMENT '筛查计划--参与筛查的学生id',
    `id_card`           varchar(32)  NOT NULL COMMENT '筛查计划--参与筛查的学生身份证号码',
    `birthday`          timestamp null comment '出生日期',
    `gender`            tinyint(1) null comment '性别 0-男 1-女',
    `student_age`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '筛查计划--参与筛查的学生年龄',
    `student_situation` varchar(1024) NOT NULL DEFAULT '' COMMENT '筛查计划--参与筛查的当时情况',
    `student_no`        varchar(64)  NOT NULL COMMENT '筛查计划--参与筛查的学生编号',
    `student_name`      varchar(8)   NOT NULL COMMENT '筛查计划--参与筛查的学生名字',
    `create_time`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查计划--创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参与筛查计划的学生表';




-- ----------------------------
-- Table structure for m_district_attentive_objects_statistic
-- ----------------------------
DROP TABLE IF EXISTS `m_district_attentive_objects_statistic`;
CREATE TABLE `m_district_attentive_objects_statistic`
(
    `id`                     int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_notice_id`    int(10) unsigned NOT NULL COMMENT '重点视力对象--所属的通知id',
    `screening_task_id`      int(10) unsigned NOT NULL DEFAULT 0 COMMENT '重点视力对象--关联的任务id（is_total情况下，可能为0）',
    `district_id`            int(10) unsigned NOT NULL COMMENT '重点视力对象--所属的地区id',
    `vision_label0_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '重点视力对象--零级预警人数（默认0）',
    `vision_label0_ratio`   int(10) unsigned NOT NULL DEFAULT '0' COMMENT '重点视力对象--零级预警比例（均为整数，如10.01%，数据库则是1001）',
    `vision_label1_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '重点视力对象--一级预警人数（默认0）',
    `vision_label1_ratio`   int(10) unsigned NOT NULL DEFAULT '0' COMMENT '重点视力对象--一级预警比例（均为整数，如10.01%，数据库则是1001）',
    `vision_label2_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '重点视力对象--二级预警人数（默认0）',
    `vision_label2_ratio`   int(10) unsigned NOT NULL DEFAULT '0' COMMENT '重点视力对象--二级预警比例（均为整数，如10.01%，数据库则是1001）',
    `vision_label3_numbers` int(11) NOT NULL DEFAULT '0' COMMENT '重点视力对象--三级预警人数（默认0）',
    `vision_label3_ratio`   int(11) NOT NULL DEFAULT '0' COMMENT '重点视力对象--三级预警比例（均为整数，如10.01%，数据库则是1001）',
    `key_warning_numbers`    int(10) unsigned NOT NULL DEFAULT '0' COMMENT '重点视力对象--重点视力对象数量（默认0）',
    `student_numbers`        int(10) unsigned NOT NULL COMMENT '重点视力对象--学生总数 ',
    `update_time`            timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '重点视力对象--更新时间',
    `is_total`               tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否合计数据',
    `create_time`            timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='某个地区层级最新统计的重点视力对象情况表';

-- ----------------------------
-- Table structure for m_district_monitor_statistic
-- ----------------------------
DROP TABLE IF EXISTS `m_district_monitor_statistic`;
CREATE TABLE `m_district_monitor_statistic`
(
    `id`                    int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_notice_id`   int(10) unsigned NOT NULL COMMENT '监测情况--所属的通知id',
    `screening_task_id`     int(10) unsigned NOT NULL DEFAULT 0 COMMENT '监测情况--关联的任务id（is_total情况下，可能为0）',
    `district_id`           int(10) unsigned NOT NULL COMMENT '监测情况--所属的地区id（筛查范围）',
    `investigation_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--戴镜人数（默认0）',
    `without_glass_dsn`     int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--脱镜复测数量（默认0）',
    `without_glass_dsin`    int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--脱镜复测指标数（dsin = double screening index numbers默认0）',
    `wearing_glass_dsn`     int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--戴镜复测数量（默认0）',
    `wearing_glass_dsin`    int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--戴镜复测指标数（dsin = double screening index numbers默认0）',
    `dsn`                   int(11) NOT NULL DEFAULT '0' COMMENT '监测情况--复测数量（默认0）',
    `error_numbers`         int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--筛查错误数（默认0）',
    `error_ratio`           tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--筛查错误率（默认0，单位%）',
    `plan_screening_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--计划的学生数量（默认0）',
    `screening_numbers`     int(11) NOT NULL DEFAULT '0' COMMENT '监测情况--实际筛查的学生数量（默认0）',
    `is_total`              tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否合计数据',
    `update_time`           timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '监测情况--更新时间',
    `create_time`           timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地区层级某次筛查计划统计监控监测情况表';

-- ----------------------------
-- Table structure for m_district_vision_statistic
-- ----------------------------
DROP TABLE IF EXISTS `m_district_vision_statistic`;
CREATE TABLE `m_district_vision_statistic`
(
    `id`                       int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_notice_id`      int(10) unsigned NOT NULL COMMENT '视力情况--所属的通知id',
    `screening_task_id`        int(10) unsigned NOT NULL DEFAULT 0 COMMENT '视力情况--关联的任务id（is_total情况下，可能为0）',
    `district_id`              int(10) unsigned NOT NULL COMMENT '视力情况--所属的地区id',
    `avg_left_vision`          decimal(10, 0) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--平均左眼视力（小数点后一位，默认0.0）',
    `avg_right_vision`         decimal(10, 0) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--平均右眼视力（小数点后一位，默认0.0）',
    `low_vision_numbers`       int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--视力低下人数（默认0）',
    `low_vision_ratio`         int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--视力低下比例（均为整数，如10.01%，数据库则是1001）',
    `wearing_glasses_numbers`  int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--戴镜人数（默认0）',
    `wearing_glasses_ratio`    int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--戴镜人数（均为整数，如10.01%，数据库则是1001）',
    `myopia_numbers`           int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--近视人数（默认0）',
    `myopia_ratio`             int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--近视比例（均为整数，如10.01%，数据库则是1001）',
    `ametropia_numbers`        int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--屈光不正人数（默认0）',
    `ametropia_ratio`          int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--屈光不正比例（均为整数，如10.01%，数据库则是1001）',
    `vision_label_0_numbers`   int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--零级预警人数（默认0）',
    `vision_label_0_ratio`     int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--零级预警比例（均为整数，如10.01%，数据库则是1001）',
    `vision_label_1_numbers`   int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--一级预警人数（默认0）',
    `vision_label_1_ratio`     int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--一级预警比例（均为整数，如10.01%，数据库则是1001）',
    `vision_label_2_numbers`   int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--二级预警人数（默认0）',
    `vision_label_2_ratio`     int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--二级预警比例（均为整数，如10.01%，数据库则是1001）',
    `vision_label_3_numbers`   int(10) NOT NULL DEFAULT '0' COMMENT '视力情况--三级预警人数（默认0）',
    `vision_label_3_ratio`     int(10) NOT NULL DEFAULT '0' COMMENT '视力情况--三级预警比例（均为整数，如10.01%，数据库则是1001）',
    `key_warning_numbers`      int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--重点视力对象数量（默认0）',
    `treatment_advice_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--建议就诊数量（默认0）',
    `treatment_advice_ratio`   int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--建议就诊比例（均为整数，如10.01%，数据库则是1001）',
    `plan_screening_numbers`   int(10) unsigned DEFAULT '0' COMMENT '视力情况--计划的学生数量（默认0）',
    `real_screening_numbers`   int(10) COMMENT '视力情况--实际筛查的学生数量（默认0）',
    `is_total`                 tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否合计数据',
    `update_time`              timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '视力情况--更新时间',
    `create_time`              timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地区层级某次筛查计划统计视力情况表';

-- ----------------------------
-- Table structure for m_school_vision_statistic
-- ----------------------------
DROP TABLE IF EXISTS `m_school_vision_statistic`;
CREATE TABLE `m_school_vision_statistic`
(
    `id`                       int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `school_id`                int(10) unsigned NOT NULL COMMENT '视力情况--所属的学校id',
    `screening_notice_id`      int(10) unsigned NOT NULL COMMENT '视力情况--所属的通知id',
    `screening_task_id`        int(10) unsigned NOT NULL COMMENT '视力情况--所属的任务id',
    `screening_plan_id`        int(10) unsigned NOT NULL COMMENT '视力情况--关联的筛查计划id',
    `district_id`              int(10) unsigned NOT NULL COMMENT '视力情况--所属的地区id',
    `avg_left_vision`          decimal(10, 0) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--平均左眼视力（小数点后一位，默认0.0）',
    `avg_right_vision`         decimal(10, 0) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--平均右眼视力（小数点后一位，默认0.0）',
    `low_vision_numbers`       int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--视力低下人数（默认0）',
    `low_vision_ratio`         int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--视力低下比例（均为整数，如10.01%，数据库则是1001）',
    `wearing_glasses_numbers`  int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--戴镜人数（默认0）',
    `wearing_glasses_ratio`    int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--戴镜人数（均为整数，如10.01%，数据库则是1001）',
    `myopia_numbers`           int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--近视人数（默认0）',
    `myopia_ratio`             int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--近视比例（均为整数，如10.01%，数据库则是1001）',
    `ametropia_numbers`        int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--屈光不正人数（默认0）',
    `ametropia_ratio`          int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--屈光不正比例（均为整数，如10.01%，数据库则是1001）',
    `vision_label0_numbers`   int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--零级预警人数（默认0）',
    `vision_label0_ratio`     int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--零级预警比例（均为整数，如10.01%，数据库则是1001）',
    `vision_label1_numbers`   int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--一级预警人数（默认0）',
    `vision_label1_ratio`     int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--一级预警比例（均为整数，如10.01%，数据库则是1001）',
    `vision_label2_numbers`   int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--二级预警人数（默认0）',
    `vision_label2_ratio`     int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--二级预警比例（均为整数，如10.01%，数据库则是1001）',
    `vision_label3_numbers`   int(11) NOT NULL DEFAULT '0' COMMENT '视力情况--三级预警人数（默认0）',
    `vision_label3_ratio`     int(11) NOT NULL DEFAULT '0' COMMENT '视力情况--三级预警比例（均为整数，如10.01%，数据库则是1001）',
    `key_warning_numbers`      int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--重点视力对象数量（默认0）',
    `treatment_advice_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--建议就诊数量（默认0）',
    `treatment_advice_ratio`   int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--建议就诊比例（均为整数，如10.01%，数据库则是1001）',
    `plan_screening_numbers`   int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--计划的学生数量（默认0）',
    `real_screening_numbers`   int(11) DEFAULT NULL COMMENT '视力情况--实际筛查的学生数量（默认0）',
    `update_time`              timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '视力情况--更新时间',
    `create_time`              timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校某次筛查计划统计视力情况表';

-- ----------------------------
-- Table structure for m_screening_result
-- ----------------------------
DROP TABLE IF EXISTS `m_vision_screening_result`;
CREATE TABLE `m_vision_screening_result`
(
    `id`                               INT(10) NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_plan_school_student_id` INT(10) NULL DEFAULT NULL COMMENT '筛查计划中的学生id',
    `screening_org_id`                 INT(10) NOT NULL COMMENT '筛查结果--所属的机构id',
    `task_id`                          INT(10) NOT NULL COMMENT '筛查结果--所属的任务id',
    `create_user_id`                   INT(10) NOT NULL COMMENT '筛查结果--创建的用户id',
    `plan_id`                          INT(10) NULL DEFAULT NULL COMMENT '筛查结果--所属的计划id',
    `school_id`                        INT(10) NOT NULL COMMENT '筛查结果--执行的学校id',
    `student_id`                       INT(10) NOT NULL COMMENT '筛查结果--参与筛查的学生id',
    `district_id`                      INT(10) NULL DEFAULT NULL COMMENT '筛查结果--所属的地区id',
    `vision_data`                      JSON NULL DEFAULT NULL COMMENT '筛查结果--视力数据',
    `computer_optometry`               JSON NULL DEFAULT NULL COMMENT '筛查结果--电脑验光',
    `biometric_data`                   JSON NULL DEFAULT NULL COMMENT '筛查结果--生物测量',
    `other_eye_diseases`               JSON NULL DEFAULT NULL COMMENT '筛查结果--其他眼疾',
    `is_double_screen`                 TINYINT(3) NOT NULL DEFAULT '0' COMMENT '筛查结果--是否复筛（0否，1是）',
    `update_time`                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查结果--更新时间',
    `create_time`                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) COMMENT='筛查结果表';
