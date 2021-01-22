-- ----------------------------
-- Table structure for m_screening_notice
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_notice`;
CREATE TABLE `m_screening_notice` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `title` varchar(25) NOT NULL COMMENT '筛查通知--标题（最大25个字符）',
    `content` varchar(255) NOT NULL COMMENT '筛查通知--通知内容（长度未知）',
    `start_time` timestamp NULL COMMENT '筛查通知--开始时间（时间戳）',
    `end_time` timestamp NULL COMMENT '筛查通知--结束时间（时间戳）',
    `type` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '筛查通知--通知类型（0是筛查通知-政府、1是筛查任务通知-筛查机构）',
    `gov_dept_id` int(10) unsigned NOT NULL COMMENT '筛查通知--所处部门id',
    `district_id` int(10) unsigned NOT NULL COMMENT '筛查通知--所处地区id',
    `release_status` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '筛查通知--通知状态（0未发布、1已发布）',
    `release_time` timestamp NULL COMMENT '筛查通知--发布时间（时间戳 ）',
    `operation_version` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '筛查通知--操作人版本（版本自增，便于解决数据修改覆盖）',
    `creator_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查通知--创建人id  ',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查通知--创建时间（时间戳）',
    `operator_id` int(10) NOT NULL DEFAULT '0' COMMENT '筛查通知--最后操作人id  ',
    `operate_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '筛查通知--最后操作时间（时间戳）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛查通知表';

-- ----------------------------
-- Table structure for m_screening_notice_dept_org
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_notice_dept_org`;
CREATE TABLE `m_screening_notice_dept_org` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_notice_id` int(10) unsigned NOT NULL COMMENT '筛查通知--筛查通知表id',
    `district_id` int(10) unsigned NOT NULL COMMENT '筛查通知--接收对象所在的区域id',
    `accept_gov_org_id` int(10) unsigned NOT NULL COMMENT '筛查通知--接收通知对象的id（机构id 或者 部门id）',
    `operation_status` tinyint(3) unsigned NOT NULL DEFAULT '000' COMMENT '筛查通知--操作状态（0未读 1 是已读 2是已创建）',
    `operator_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查通知--操作人id（查看或者编辑的人id）',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛查通知通知到的部门或者机构表';

-- ----------------------------
-- Table structure for m_screening_task
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_task`;
CREATE TABLE `m_screening_task` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_notice_id` int(10) unsigned NOT NULL COMMENT '筛查任务--所属的通知id',
    `title` varchar(25) NOT NULL COMMENT '筛查任务--标题',
    `content` varchar(255) NOT NULL COMMENT '筛查任务--内容',
    `start_time` timestamp NULL COMMENT '筛查任务--开始时间（时间戳）',
    `end_time` timestamp NULL COMMENT '筛查任务--结束时间（时间戳）',
    `gov_dept_id` int(10) unsigned NOT NULL COMMENT '筛查任务--所处部门id',
    `district_id` int(10) unsigned NOT NULL COMMENT '筛查任务--所处区域id',
    `release_status` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '筛查任务--发布状态 （0未发布 1已发布）',
    `release_time` timestamp NULL DEFAULT NULL COMMENT '筛查任务--发布时间（时间戳）',
    `creator_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查任务--创建者ID',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查任务--创建时间（时间戳）',
    `operator_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查任务--最后操作人id  ',
    `operate_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '筛查任务--最后操作时间（时间戳）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛查通知任务表';

-- ----------------------------
-- Table structure for m_screening_task_org
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_task_org`;
CREATE TABLE `m_screening_task_org` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_task_id` int(10) unsigned NOT NULL COMMENT '筛查任务--筛查任务id',
    `org_id` int(10) unsigned NOT NULL COMMENT '筛查任务--筛查机构id',
    `quality_controller_name` varchar(25) NOT NULL COMMENT '筛查任务--机构质控员名字（长度限制未知）',
    `quality_controller_contact` varchar(25) NOT NULL COMMENT '筛查任务--机构质控员联系方式（长度限制未知）',
    `quality_controller_commander` varchar(25) NOT NULL COMMENT '筛查任务--机构质控员队长（长度限制未知）',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛查任务关联的机构表';

-- ----------------------------
-- Table structure for m_screening_plan
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_plan`;
CREATE TABLE `m_screening_plan` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_task_id` int(10) unsigned NOT NULL COMMENT '筛查计划--所属的筛查任务id',
    `title` varchar(25) NOT NULL COMMENT '筛查计划--标题',
    `content` varchar(255) NOT NULL COMMENT '筛查计划--内容',
    `start_time` timestamp NULL COMMENT '筛查计划--开始时间（时间戳）',
    `end_time` timestamp NULL COMMENT '筛查计划--结束时间（时间戳）',
    `gov_dept_id` int(10) unsigned NOT NULL COMMENT '筛查计划--所处部门id',
    `district_id` int(10) unsigned NOT NULL COMMENT '筛查计划--所处区域id',
    `org_id` int(10) unsigned NOT NULL COMMENT '筛查计划--指定的筛查机构id',
    `release_status` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '筛查计划--发布状态 （0未发布 1已发布）',
    `release_time` timestamp NULL DEFAULT NULL COMMENT '筛查计划--发布时间（时间戳）',
    `creator_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查计划--创建者ID',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查计划--创建时间（时间戳）',
    `operator_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查计划--最后操作人id  ',
    `operate_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '筛查计划--最后操作时间（时间戳）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛查通知计划表';

-- ----------------------------
-- Table structure for m_screening_plan_school
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_plan_school`;
CREATE TABLE `m_screening_plan_school` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_plan_id` int(10) unsigned NOT NULL COMMENT '筛查计划--计划id ',
    `school_id` int(10) unsigned NOT NULL COMMENT '筛查计划--执行的学校id',
    `school_name` varchar(32) DEFAULT NULL COMMENT '筛查计划--学校名字',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛查计划关联的学校表';

-- ----------------------------
-- Table structure for m_screening_plan_school_student
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_plan_school_student`;
CREATE TABLE `m_screening_plan_school_student` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_plan_id` int(10) unsigned NOT NULL COMMENT '筛查计划--计划id ',
    `school_id` int(10) unsigned NOT NULL COMMENT '筛查计划--执行的学校id',
    `school_name` varchar(32) NOT NULL COMMENT '筛查计划--执行的学校名字',
    `grade_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查计划--参与筛查的学生年级ID',
    `class_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查计划--参与筛查的学生班级ID',
    `student_id` int(10) unsigned NOT NULL COMMENT '筛查计划--参与筛查的学生id',
    `student_age` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '筛查计划--参与筛查的学生年龄',
    `student_situation` varchar(255) NOT NULL DEFAULT '' COMMENT '筛查计划--参与筛查的当时情况',
    `student_no` varchar(11) NOT NULL COMMENT '筛查计划--参与筛查的学生编号',
    `student_name` varchar(8) NOT NULL COMMENT '筛查计划--参与筛查的学生名字',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查计划--创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参与筛查计划的学生表';

-- ----------------------------
-- Table structure for m_data_commit
-- ----------------------------
DROP TABLE IF EXISTS `m_data_commit`;
CREATE TABLE `m_data_commit` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_plan_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '筛查计划id',
    `commit_district_id` int(10) unsigned NOT NULL COMMENT '数据提交地区',
    `src_district_id` int(10) unsigned NOT NULL COMMENT '数据所在地区',
    `committer_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '提交人id',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查统计--创建时间（时间戳  not null）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据上交情况表';

-- ----------------------------
-- Table structure for m_district_attentive_objects_statistic
-- ----------------------------
DROP TABLE IF EXISTS `m_district_attentive_objects_statistic`;
CREATE TABLE `m_district_attentive_objects_statistic` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_task_id` int(10) unsigned NOT NULL COMMENT '重点视力对象--所属的任务id',
    `screening_plan_id` int(10) unsigned NOT NULL COMMENT '重点视力对象--关联的计划id',
    `district_id` int(10) unsigned NOT NULL COMMENT '重点视力对象--所属的地区id',
    `vision_label_0_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '重点视力对象--零级预警人数（默认0）',
    `vision_label_1_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '重点视力对象--一级预警人数（默认0）',
    `vision_label_2_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '重点视力对象--二级预警人数（默认0）',
    `vision_label_3_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '重点视力对象--三级预警人数（默认0）',
    `key_warning_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '重点视力对象--重点视力对象数量（默认0）',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '重点视力对象--统计时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='某个地区层级最新统计的重点视力对象情况表';

-- ----------------------------
-- Table structure for m_district_monitor_statistic
-- ----------------------------
DROP TABLE IF EXISTS `m_district_monitor_statistic`;
CREATE TABLE `m_district_monitor_statistic` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_task_id` int(10) unsigned NOT NULL COMMENT '监测情况--关联的任务id',
    `screening_plan_id` int(10) unsigned NOT NULL COMMENT '监测情况--关联的筛查计划id',
    `district_id` int(10) unsigned NOT NULL COMMENT '监测情况--所属的地区id',
    `investigation_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--戴镜人数（默认0）',
    `completion_ratio` tinyint(3) unsigned NOT NULL DEFAULT '100' COMMENT '监测情况--完成率（默认100,单位%）',
    `pass_ratio` tinyint(3) unsigned NOT NULL DEFAULT '100' COMMENT '监测情况--合格率（默认100,单位%）',
    `pass_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--合格人数（默认0）',
    `without_glass_dsn` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--脱镜复测数量（默认0）',
    `without_glass_dsr` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--脱镜复测比例（默认0）',
    `wearing_glass_dsn` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--戴镜复测数量（默认0）',
    `wearing_glass_dsr` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--戴镜复测比例（默认0）',
    `dsn` int(11) NOT NULL DEFAULT '0' COMMENT '监测情况--复测数量（默认0）',
    `error_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--筛查错误数（默认0）',
    `error_ratio` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--筛查错误率（默认0，单位%）',
    `plan_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '监测情况--计划的学生数量（默认0）',
    `screening_numbers` int(11) NOT NULL DEFAULT '0' COMMENT '监测情况--实际筛查的学生数量（默认0）',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '监测情况--统计时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地区层级某次筛查计划统计监控监测情况表';

-- ----------------------------
-- Table structure for m_district_vision_statistic
-- ----------------------------
DROP TABLE IF EXISTS `m_district_vision_statistic`;
CREATE TABLE `m_district_vision_statistic` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_task_id` int(10) unsigned NOT NULL COMMENT '视力情况--所属的任务id',
    `screening_plan_id` int(10) unsigned NOT NULL COMMENT '视力情况--关联的筛查计划id',
    `district_id` int(10) unsigned NOT NULL COMMENT '视力情况--所属的地区id',
    `avg_left_vision` decimal(10,0) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--平均左眼视力（小数点后一位，默认0.0）',
    `avg_right_vision` decimal(10,0) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--平均右眼视力（小数点后一位，默认0.0）',
    `low_vision_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--视力低下人数（默认0）',
    `wearing_glasses_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--戴镜人数（默认0）',
    `myopia_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--近视人数（默认0）',
    `vision_label_0_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--零级预警人数（默认0）',
    `vision_label_1_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--一级预警人数（默认0）',
    `vision_label_2_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--二级预警人数（默认0）',
    `vision_label_3_numbers` int(11) NOT NULL DEFAULT '0' COMMENT '视力情况--三级预警人数（默认0）',
    `key_warning_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--重点视力对象数量（默认0）',
    `plan_screening_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--计划的学生数量（默认0）',
    `real_screening_numners` int(11) DEFAULT NULL COMMENT '视力情况--实际筛查的学生数量（默认0）',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '视力情况--统计时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地区层级某次筛查计划统计视力情况表';

-- ----------------------------
-- Table structure for m_school_vision_statistic
-- ----------------------------
DROP TABLE IF EXISTS `m_school_vision_statistic`;
CREATE TABLE `m_school_vision_statistic` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `screening_task_id` int(10) unsigned NOT NULL COMMENT '视力情况--所属的任务id',
    `screening_plan_id` int(10) unsigned NOT NULL COMMENT '视力情况--关联的筛查计划id',
    `district_id` int(10) unsigned NOT NULL COMMENT '视力情况--所属的地区id',
    `school_id` int(10) unsigned NOT NULL COMMENT '视力情况--所属的学校id',
    `avg_left_vision` decimal(10,0) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--平均左眼视力（小数点后一位，默认0.0）',
    `avg_right_vision` decimal(10,0) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--平均右眼视力（小数点后一位，默认0.0）',
    `low_vision_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--视力低下人数（默认0）',
    `wearing_glasses_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--戴镜人数（默认0）',
    `myopia_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--近视人数（默认0）',
    `vision_label_0_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--零级预警人数（默认0）',
    `vision_label_1_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--一级预警人数（默认0）',
    `vision_label_2_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--二级预警人数（默认0）',
    `vision_label_3_numbers` int(11) NOT NULL DEFAULT '0' COMMENT '视力情况--三级预警人数（默认0）',
    `key_warning_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--重点视力对象数量（默认0）',
    `plan_screening_numbers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '视力情况--计划的学生数量（默认0）',
    `real_screening_numners` int(11) DEFAULT NULL COMMENT '视力情况--实际筛查的学生数量（默认0）',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '视力情况--统计时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校某次筛查计划统计视力情况表';

-- ----------------------------
-- Table structure for m_screening_raw_data
-- ----------------------------
DROP TABLE IF EXISTS `m_student_screening_raw_data`;
CREATE TABLE `m_student_screening_raw_data` (
    `id` int(11) NOT NULL COMMENT '主键id',
    `screening_plan_school_student_id` int(10) unsigned NOT NULL COMMENT '筛查原始数据--所属的学生id',
    `screening_raw_data` json NOT NULL COMMENT '筛查原始数据',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查原始数据--创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛查原始数据表';

-- ----------------------------
-- Table structure for m_screening_result
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_result`;
CREATE TABLE `m_screening_result` (
    `id` int(11) NOT NULL COMMENT '主键id',
    `screening_plan_school_student_id` int(10) unsigned NOT NULL COMMENT '筛查结果--所属的学生id',
    `task_id` int(11) NOT NULL COMMENT '筛查结果--所属的任务id',
    `plan_id` int(11) DEFAULT NULL COMMENT '筛查结果--所属的计划id',
    `school_id` int(10) unsigned NOT NULL COMMENT '筛查计划--执行的学校id',
    `student_id` int(10) unsigned NOT NULL COMMENT '筛查计划--参与筛查的学生id',
    `district_id` int(11) DEFAULT NULL COMMENT '筛查结果--所属的地区id',
    `left_naked_vision` decimal(2,1) NOT NULL DEFAULT '0.0' COMMENT '筛查结果--左眼裸视力（默认0.0）',
    `right_naked_vision` decimal(2,1) NOT NULL DEFAULT '0.0' COMMENT '筛查结果--右眼裸视力 （默认0.0）',
    `left_corrected_vision` decimal(2,1) NOT NULL DEFAULT '0.0' COMMENT '筛查结果--左眼矫正视力 （默认0.0）',
    `right_corrected_vision` decimal(2,1) NOT NULL DEFAULT '0.0' COMMENT '筛查结果--右眼眼矫正视力 （默认0.0）',
    `glasses_type` tinyint(3) NOT NULL DEFAULT '-1' COMMENT '筛查结果--戴镜情况：-1-默认、0-没有戴镜、1-佩戴框架眼镜、2-佩戴隐形眼镜、3-夜戴角膜塑形镜',
    `vision_label` tinyint(3) NOT NULL DEFAULT '-1' COMMENT '筛查结果-- 预警情况 ：-1-默认、0-是0级、1-是一级、2是二级、3是三级',
    `is_low_vision` tinyint(1) NOT NULL DEFAULT '-1' COMMENT '筛查结果--是否视力低下：-1-默认、0-否、1-是',
    `is_myopia` tinyint(1) NOT NULL DEFAULT '-1' COMMENT '筛查结果--是否近视：-1-默认、0-否、1-是',
    `is_key_warning_crown` tinyint(1) NOT NULL DEFAULT '-1' COMMENT '筛查结果--是否属于重点视力人群：-1-默认、0-否、1-是',
    `is_recommended_visits` tinyint(1) NOT NULL DEFAULT '-1' COMMENT '筛查结果--是否建议就诊：-1-默认、0-否、1-是',
    `is_double_screen` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '筛查结果--是否复筛（0否，1是）',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛查结果表';
