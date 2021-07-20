/*
 Navicat Premium Data Transfer

 Source Server Type    : MySQL
 Source Server Version : 50733
 Source Schema         : myopia_business

 Target Server Type    : MySQL
 Target Server Version : 50733
 File Encoding         : 65001

 Date: 10/05/2021 09:48:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for h_department
-- ----------------------------
DROP TABLE IF EXISTS `h_department`;
CREATE TABLE `h_department`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `hospital_id` int(11) NOT NULL COMMENT '医院id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '科室名称',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `hospital_id_index`(`hospital_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '医院-科室' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for h_doctor
-- ----------------------------
DROP TABLE IF EXISTS `h_doctor`;
CREATE TABLE `h_doctor`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gender` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-男 1-女',
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `user_id` int(11) DEFAULT NULL COMMENT '用户ID',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '说明',
  `hospital_id` int(11) NOT NULL COMMENT '医院id',
  `department_id` int(11) NOT NULL COMMENT '科室id',
  `department_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '科室名称',
  `title_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '职称',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-启用 1-禁止',
  `avatar_file_id` int(11) DEFAULT NULL COMMENT '头像',
  `sign_file_id` int(11) DEFAULT NULL COMMENT '电子签名',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `hospital_id_index`(`hospital_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '医院-医生表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for h_hospital_student
-- ----------------------------
DROP TABLE IF EXISTS `h_hospital_student`;
CREATE TABLE `h_hospital_student`  (
  `id` int(11) NOT NULL COMMENT '学生id',
  `hospital_id` int(11) NOT NULL COMMENT '医院id',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建人ID',
  `school_id` int(11) DEFAULT NULL COMMENT '学校ID',
  `grade_id` int(11) DEFAULT NULL COMMENT '年级ID',
  `class_id` int(11) DEFAULT NULL COMMENT '班级ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学生姓名',
  `gender` tinyint(1) NOT NULL COMMENT '性别 0-男 1-女',
  `birthday` timestamp NULL COMMENT '出生日期',
  `nation` tinyint(4) DEFAULT NULL COMMENT '民族 0-汉族',
  `id_card` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '身份证号码',
  `parent_phone` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '家长手机号码',
  `mp_parent_phone` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '家长公众号手机号码',
  `province_id` int(11) DEFAULT NULL COMMENT '省id',
  `city_id` int(11) DEFAULT NULL COMMENT '市id',
  `area_id` int(11) DEFAULT NULL COMMENT '区id',
  `town_id` int(11) DEFAULT NULL COMMENT '镇/乡id',
  `address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '详细地址',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-启用 1-禁止 2-删除',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id_hospital_id_index`(`hospital_id`, `id`) USING BTREE,
  INDEX `hospital_id_index`(`hospital_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '医院-学生表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for h_medical_record
-- ----------------------------
DROP TABLE IF EXISTS `h_medical_record`;
CREATE TABLE `h_medical_record`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `student_id` int(11) NOT NULL COMMENT '学生id',
  `hospital_id` int(11) DEFAULT NULL COMMENT '医院id',
  `department_id` int(11) DEFAULT NULL COMMENT '科室id',
  `doctor_id` int(11) NOT NULL COMMENT '医生id',
  `consultation` json COMMENT '问诊内容',
  `vision` json COMMENT '视力检查',
  `diopter` json COMMENT '屈光检查',
  `biometrics` json COMMENT '生物测量',
  `tosca` json COMMENT '角膜地形图',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态。0检查中，1检查完成',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `student_id_index`(`student_id`) USING BTREE,
  INDEX `hospital_id_index`(`hospital_id`) USING BTREE,
  INDEX `doctor_id_index`(`doctor_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '医院-检查单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for h_medical_report
-- ----------------------------
DROP TABLE IF EXISTS `h_medical_report`;
CREATE TABLE `h_medical_report`  (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `no` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '报告编号',
  `hospital_id` int(11) DEFAULT NULL COMMENT '医院id',
  `department_id` int(11) DEFAULT NULL COMMENT '科室id',
  `student_id` int(11) NOT NULL COMMENT '学生id',
  `medical_record_id` int(11) NOT NULL COMMENT '对应的检查单id',
  `doctor_id` int(11) NOT NULL COMMENT '医生id',
  `glasses_situation` tinyint(4) DEFAULT NULL COMMENT '配镜情况。1配框架眼镜，2配OK眼镜，3配隐形眼镜',
  `medical_content` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '医生诊断',
  `image_id_list` json COMMENT '影像列表id',
  `report_conclusion_data` json COMMENT '固化的结论数据',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `student_id_index`(`student_id`) USING BTREE,
  INDEX `medical_record_id_index`(`medical_record_id`) USING BTREE,
  INDEX `doctor_id_index`(`doctor_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '医院-检查报告' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_big_screen_map
-- ----------------------------
DROP TABLE IF EXISTS `m_big_screen_map`;
CREATE TABLE `m_big_screen_map`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '行政区ID',
  `district_Id` int(11) NOT NULL DEFAULT 0 COMMENT '行政区名称',
  `json` json COMMENT '行政区代码',
  `city_center_location` json,
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上级行政区代码（省级统一为100000000000）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '行政区域表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_district
-- ----------------------------
DROP TABLE IF EXISTS `m_district`;
CREATE TABLE `m_district`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '行政区ID',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '行政区名称',
  `code` bigint(20) NOT NULL COMMENT '行政区代码',
  `parent_code` bigint(20) NOT NULL COMMENT '上级行政区代码（省级统一为100000000000）',
  `area_code` tinyint(4) DEFAULT NULL COMMENT '片区代码:每个省、自治区﹑直辖市按照社会经济发展不同水平进行片区编码，片区代码按照经济水平，省会〈好片)=1，中片=2，差片=3',
  `monitor_code` tinyint(4) DEFAULT NULL COMMENT '监测点代码:每个省、自治区、直辖市按照城区﹑郊县/区进行编码，城区=1，郊县/区=2',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `m_district_pk`(`code`) USING BTREE,
  INDEX `m_district_parent_code_code_index`(`parent_code`, `code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '行政区域表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_district_attentive_objects_statistic
-- ----------------------------
DROP TABLE IF EXISTS `m_district_attentive_objects_statistic`;
CREATE TABLE `m_district_attentive_objects_statistic`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `district_id` int(10) UNSIGNED NOT NULL COMMENT '重点视力对象--所属的地区id',
  `vision_label0_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '重点视力对象--零级预警人数（默认0）',
  `vision_label0_ratio` decimal(5, 2) NOT NULL COMMENT '重点视力对象--零级预警比例（均为整数，如10.01%，数据库则是1001）',
  `vision_label1_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '重点视力对象--一级预警人数（默认0）',
  `vision_label1_ratio` decimal(5, 2) NOT NULL COMMENT '重点视力对象--一级预警比例（均为整数，如10.01%，数据库则是1001）',
  `vision_label2_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '重点视力对象--二级预警人数（默认0）',
  `vision_label2_ratio` decimal(5, 2) NOT NULL COMMENT '重点视力对象--二级预警比例（均为整数，如10.01%，数据库则是1001）',
  `vision_label3_numbers` int(10) NOT NULL DEFAULT 0 COMMENT '重点视力对象--三级预警人数（默认0）',
  `vision_label3_ratio` decimal(5, 2) NOT NULL COMMENT '重点视力对象--三级预警比例（均为整数，如10.01%，数据库则是1001）',
  `key_warning_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '重点视力对象--重点视力对象数量（默认0）',
  `student_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '重点视力对象--学生总数 ',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '重点视力对象--更新时间',
  `is_total` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否合计数据',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `district_attentive_objects_statistic_unique`(`district_id`, `is_total`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '某个地区层级最新统计的重点视力对象情况表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_district_big_screen_statistic
-- ----------------------------
DROP TABLE IF EXISTS `m_district_big_screen_statistic`;
CREATE TABLE `m_district_big_screen_statistic`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `screening_notice_id` int(11) DEFAULT NULL COMMENT '大屏展示--所属的通知id',
  `district_id` int(10) UNSIGNED DEFAULT NULL COMMENT '大屏展示--地区的id',
  `gov_dept_id` int(10) UNSIGNED DEFAULT 0 COMMENT '大屏展示--政府部门id',
  `valid_data_num` int(11) DEFAULT NULL COMMENT '大屏展示--有效数据的数量',
  `real_screening_num` int(11) DEFAULT NULL COMMENT '大屏展示--实际筛查的数量',
  `plan_screening_num` int(11) DEFAULT NULL COMMENT '大屏展示--计划筛查学生数',
  `progress_rate` decimal(7, 2) DEFAULT NULL COMMENT '大屏展示--筛查进度',
  `real_screening` json COMMENT '大屏展示--实际筛查情况',
  `low_vision` json COMMENT '大屏展示--视力低下情况',
  `myopia` json COMMENT '大屏展示--近视情况',
  `ametropia` json COMMENT '大屏展示--屈光不正情况',
  `focus_objects` json COMMENT '大屏展示--重点视力对象情况',
  `avg_vision` json COMMENT '大屏展示--平均视力情况',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '大屏展示--更新时间',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '地区层级某次筛查计划统计监控监测情况表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_district_monitor_statistic
-- ----------------------------
DROP TABLE IF EXISTS `m_district_monitor_statistic`;
CREATE TABLE `m_district_monitor_statistic`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `screening_notice_id` int(10) UNSIGNED NOT NULL COMMENT '监测情况--所属的通知id',
  `finish_ratio` decimal(5, 2) NOT NULL COMMENT '监测情况--完成率',
  `screening_task_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '监测情况--关联的任务id（is_total情况下，可能为0）',
  `district_id` int(10) UNSIGNED NOT NULL COMMENT '监测情况--所属的地区id（筛查范围）',
  `investigation_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '监测情况--戴镜人数（默认0）',
  `without_glass_dsn` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '监测情况--脱镜复测数量（默认0）',
  `without_glass_dsin` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '监测情况--脱镜复测指标数（dsin = double screening index numbers默认0）',
  `wearing_glass_dsn` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '监测情况--戴镜复测数量（默认0）',
  `wearing_glass_dsin` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '监测情况--戴镜复测指标数（dsin = double screening index numbers默认0）',
  `dsn` int(10) NOT NULL DEFAULT 0 COMMENT '监测情况--复测数量（默认0）',
  `error_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '监测情况--筛查错误数（默认0）',
  `error_ratio` decimal(5, 2) NOT NULL COMMENT '监测情况--筛查错误率（默认0，单位%）',
  `rescreening_item_numbers` int(10) NOT NULL DEFAULT 0 COMMENT '复测项数量',
  `plan_screening_numbers` int(10) NOT NULL DEFAULT 0 COMMENT '监测情况--计划的学生数量（默认0）',
  `real_screening_numbers` int(10) NOT NULL DEFAULT 0 COMMENT '监测情况--实际筛查的学生数量（默认0）',
  `is_total` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否合计数据',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '监测情况--更新时间',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `district_monitor_statistic_unique`(`screening_notice_id`, `district_id`, `is_total`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '地区层级某次筛查计划统计监控监测情况表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_district_vision_statistic
-- ----------------------------
DROP TABLE IF EXISTS `m_district_vision_statistic`;
CREATE TABLE `m_district_vision_statistic`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `screening_notice_id` int(10) UNSIGNED NOT NULL COMMENT '视力情况--所属的通知id',
  `screening_task_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--关联的任务id（is_total情况下，可能为0）',
  `district_id` int(10) UNSIGNED NOT NULL COMMENT '视力情况--所属的地区id',
  `avg_left_vision` decimal(3, 2) UNSIGNED NOT NULL COMMENT '视力情况--平均左眼视力（小数点后一位，默认0.0）',
  `avg_right_vision` decimal(3, 2) UNSIGNED NOT NULL COMMENT '视力情况--平均右眼视力（小数点后一位，默认0.0）',
  `low_vision_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--视力低下人数（默认0）',
  `low_vision_ratio` decimal(5, 2) NOT NULL COMMENT '视力情况--视力低下比例（均为整数，如10.01%，数据库则是1001）',
  `wearing_glasses_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--戴镜人数（默认0）',
  `wearing_glasses_ratio` decimal(5, 2) NOT NULL COMMENT '视力情况--戴镜人数（均为整数，如10.01%，数据库则是1001）',
  `myopia_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--近视人数（默认0）',
  `myopia_ratio` decimal(5, 2) NOT NULL COMMENT '视力情况--近视比例（均为整数，如10.01%，数据库则是1001）',
  `ametropia_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--屈光不正人数（默认0）',
  `ametropia_ratio` decimal(5, 2) NOT NULL COMMENT '视力情况--屈光不正比例（均为整数，如10.01%，数据库则是1001）',
  `vision_label0_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--零级预警人数（默认0）',
  `vision_label0_ratio` decimal(5, 2) NOT NULL COMMENT '视力情况--零级预警比例（均为整数，如10.01%，数据库则是1001）',
  `vision_label1_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--一级预警人数（默认0）',
  `vision_label1_ratio` decimal(5, 2) NOT NULL COMMENT '视力情况--一级预警比例（均为整数，如10.01%，数据库则是1001）',
  `vision_label2_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--二级预警人数（默认0）',
  `vision_label2_ratio` decimal(5, 2) NOT NULL COMMENT '视力情况--二级预警比例（均为整数，如10.01%，数据库则是1001）',
  `vision_label3_numbers` int(10) NOT NULL DEFAULT 0 COMMENT '视力情况--三级预警人数（默认0）',
  `vision_label3_ratio` decimal(5, 2) NOT NULL COMMENT '视力情况--三级预警比例（均为整数，如10.01%，数据库则是1001）',
  `key_warning_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--重点视力对象数量（默认0）',
  `treatment_advice_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--建议就诊数量（默认0）',
  `treatment_advice_ratio` decimal(5, 2) NOT NULL COMMENT '视力情况--建议就诊比例（均为整数，如10.01%，数据库则是1001）',
  `plan_screening_numbers` int(10) UNSIGNED DEFAULT 0 COMMENT '视力情况--计划的学生数量（默认0）',
  `real_screening_numbers` int(10) UNSIGNED DEFAULT 0 COMMENT '视力情况--实际筛查的学生数量（默认0）',
  `valid_screening_numbers` int(10) UNSIGNED DEFAULT 0 COMMENT '视力情况--纳入统计的实际筛查学生数量（默认0）',
  `is_total` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否合计数据',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '视力情况--更新时间',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `district_vision_statistic_unique`(`screening_notice_id`, `district_id`, `is_total`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '地区层级某次筛查计划统计视力情况表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_government_department
-- ----------------------------
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
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name_district_id_unique`(`name`, `district_id`) USING BTREE,
  INDEX `district_id_index`(`district_id`) USING BTREE,
  INDEX `m_government_department_pid_status_index`(`pid`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '政府部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_hospital
-- ----------------------------
DROP TABLE IF EXISTS `m_hospital`;
CREATE TABLE `m_hospital`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建人ID',
  `gov_dept_id` int(11) NOT NULL COMMENT '部门ID',
  `district_id` int(11) NOT NULL COMMENT '行政区域ID',
  `district_detail` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '行政区域json',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '医院名称',
  `level` tinyint(4) NOT NULL COMMENT '等级 0-一甲,1-一乙,2-一丙,3-二甲,4-二乙,5-二丙,6-三特,7-三甲,8-三乙,9-三丙 10-其他',
  `level_desc` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '等级描述',
  `telephone` varchar(32) null comment '固定电话',
  `type` tinyint(4) NOT NULL COMMENT '医院类型 0-定点医院 1-非定点医院',
  `kind` tinyint(4) NOT NULL COMMENT '医院性质 0-公立 1-私立',
  `province_code` bigint(20) DEFAULT NULL COMMENT '省代码',
  `city_code` bigint(20) DEFAULT NULL COMMENT '市代码',
  `area_code` bigint(20) DEFAULT NULL COMMENT '区代码',
  `town_code` bigint(20) DEFAULT NULL COMMENT '镇/乡代码',
  `address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '详细地址',
  `remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '说明',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-启用 1-禁止 2-删除',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `m_hospital_name_uindex`(`name`) USING BTREE,
  INDEX `m_hospital_gov_dept_id_create_time_index`(`gov_dept_id`, `create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '医院表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_hospital_admin
-- ----------------------------
DROP TABLE IF EXISTS `m_hospital_admin`;
CREATE TABLE `m_hospital_admin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建人ID',
  `hospital_id` int(11) NOT NULL COMMENT '医院id',
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `gov_dept_id` int(11) NOT NULL COMMENT '部门ID',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '医院-管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_notice
-- ----------------------------
DROP TABLE IF EXISTS `m_notice`;
CREATE TABLE `m_notice`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建人',
  `link_id` int(11) DEFAULT NULL COMMENT '关联ID',
  `notice_user_id` int(11) DEFAULT NULL COMMENT '通知的userId',
  `type` tinyint(4) NOT NULL COMMENT '类型 0-站内信 1-筛查通知',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-未读 1-已读 2-删除',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '标题',
  `content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容',
  `file_id` int(11) DEFAULT NULL COMMENT '资源ID',
  `download_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件url',
  `start_time` timestamp NULL COMMENT '开时时间',
  `end_time` timestamp NULL COMMENT '结束时间',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `m_notice_status_notice_user_id_index`(`status`, `notice_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_parent
-- ----------------------------
DROP TABLE IF EXISTS `m_parent`;
CREATE TABLE `m_parent`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '家长ID',
  `open_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '微信openId',
  `hash_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'openId的hash值',
  `wx_header_img_url` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '微信头像',
  `wx_nickname` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '微信昵称',
  `user_id` int(11) DEFAULT NULL COMMENT '用户ID',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `m_parent_open_id_unique_index`(`open_id`) USING BTREE COMMENT 'openId唯一索引',
  UNIQUE INDEX `m_parent_hash_key_unique_index`(`hash_key`) USING BTREE COMMENT 'openId的哈希值唯一索引',
  UNIQUE INDEX `m_parent_user_id_unique_index`(`user_id`) USING BTREE COMMENT '用户ID唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '家长表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_parent_student
-- ----------------------------
DROP TABLE IF EXISTS `m_parent_student`;
CREATE TABLE `m_parent_student`  (
  `parent_id` int(11) NOT NULL COMMENT '家长ID',
  `student_id` int(11) NOT NULL COMMENT '学生ID',
  PRIMARY KEY (`parent_id`, `student_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '家长学生关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_resource_file
-- ----------------------------
DROP TABLE IF EXISTS `m_resource_file`;
CREATE TABLE `m_resource_file`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `file_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件名称',
  `bucket` char(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件bucket',
  `s3_key` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '文件s3 key',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `s3_key_unique_index`(`s3_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '文件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_school
-- ----------------------------
DROP TABLE IF EXISTS `m_school`;
CREATE TABLE `m_school`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `school_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学校编号',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建人ID',
  `gov_dept_id` int(11) NOT NULL COMMENT '部门ID',
  `district_id` int(11) NOT NULL COMMENT '行政区域ID',
  `district_province_code` tinyint(2) DEFAULT NULL COMMENT '行政区域-省Code（保留两位）',
  `district_detail` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '行政区域名',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学校名称',
  `kind` tinyint(4) NOT NULL COMMENT '学校性质 0-公办 1-私办 2-其他',
  `kind_desc` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '学校性质描述 0-公办 1-私办 2-其他',
  `lodge_status` tinyint(4) DEFAULT NULL COMMENT '寄宿状态 0-全部住校 1-部分住校 2-不住校',
  `type` tinyint(4) NOT NULL COMMENT '学校类型 0-小学,1-初级中学,2-高级中学,3-完全中学,4-九年一贯制学校,5-十二年一贯制学校,6-职业高中,7其他',
  `province_code` bigint(20) DEFAULT NULL COMMENT '省代码',
  `city_code` bigint(20) DEFAULT NULL COMMENT '市代码',
  `area_code` bigint(20) DEFAULT NULL COMMENT '区代码',
  `town_code` bigint(20) DEFAULT NULL COMMENT '镇/乡代码',
  `address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '详细地址',
  `remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '说明',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-启用 1-禁止 2-删除',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `m_school_school_no_uindex`(`school_no`) USING BTREE,
  UNIQUE INDEX `m_school_name_uindex`(`name`) USING BTREE,
  INDEX `m_school_status_create_time_index`(`status`, `create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学校表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_school_admin
-- ----------------------------
DROP TABLE IF EXISTS `m_school_admin`;
CREATE TABLE `m_school_admin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建人ID',
  `school_id` int(11) NOT NULL COMMENT '学校ID',
  `user_id` int(11) NOT NULL COMMENT '用户表ID',
  `gov_dept_id` int(11) NOT NULL COMMENT '部门ID',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学校-员工表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_school_class
-- ----------------------------
DROP TABLE IF EXISTS `m_school_class`;
CREATE TABLE `m_school_class`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `grade_id` int(11) NOT NULL COMMENT '年级ID',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建人ID',
  `school_id` int(11) NOT NULL COMMENT '学校ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '班级名称',
  `seat_count` int(11) DEFAULT NULL COMMENT '座位数',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-启用 1-禁止 2-删除',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `m_school_class_grade_id_status_index`(`grade_id`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学校-班级表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_school_grade
-- ----------------------------
DROP TABLE IF EXISTS `m_school_grade`;
CREATE TABLE `m_school_grade`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建人ID',
  `school_id` int(11) NOT NULL COMMENT '学校ID',
  `grade_code` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '年级编码',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '年级名称',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-启用 1-禁止 2-删除',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `m_school_grade_status_school_id_index`(`status`, `school_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学校-年级表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_school_monitor_statistic
-- ----------------------------
DROP TABLE IF EXISTS `m_school_monitor_statistic`;
CREATE TABLE `m_school_monitor_statistic`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `school_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '监测情况--学校名字',
  `finish_ratio` decimal(5, 2) NOT NULL COMMENT '监测情况---完成率',
  `school_type` int(10) UNSIGNED NOT NULL COMMENT '监测情况--学校类型',
  `school_id` int(10) UNSIGNED NOT NULL COMMENT '监测情况--学校id',
  `screening_org_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '监测情况--筛查机构名字',
  `screening_org_id` int(10) UNSIGNED NOT NULL COMMENT '监测情况--筛查机构id',
  `screening_notice_id` int(10) UNSIGNED NOT NULL COMMENT '监测情况--所属的通知id',
  `screening_task_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '监测情况--关联的任务id',
  `district_id` int(10) UNSIGNED NOT NULL COMMENT '监测情况--所属的地区id（筛查范围）',
  `investigation_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '监测情况--戴镜人数（默认0）',
  `without_glass_dsn` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '监测情况--脱镜复测数量（默认0）',
  `without_glass_dsin` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '监测情况--脱镜复测指标数（dsin = double screening index numbers默认0）',
  `wearing_glass_dsn` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '监测情况--戴镜复测数量（默认0）',
  `wearing_glass_dsin` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '监测情况--戴镜复测指标数（dsin = double screening index numbers默认0）',
  `dsn` int(11) NOT NULL DEFAULT 0 COMMENT '监测情况--复测数量（默认0）',
  `error_numbers` int(11) NOT NULL DEFAULT 0 COMMENT '监测情况--筛查错误数（默认0）',
  `error_ratio` decimal(5, 2) NOT NULL COMMENT '监测情况--筛查错误率（默认0，单位%）',
  `plan_screening_numbers` int(11) NOT NULL DEFAULT 0 COMMENT '监测情况--计划的学生数量（默认0）',
  `real_screening_numbers` int(11) NOT NULL DEFAULT 0 COMMENT '监测情况--实际筛查的学生数量（默认0）',
  `rescreening_item_numbers` int(11) NOT NULL DEFAULT 0 COMMENT '监测情况-复测项数量',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '监测情况--更新时间',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `school_monitor_statistic_unique`(`screening_task_id`, `screening_org_id`, `school_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '地区层级某次筛查计划统计监控监测情况表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_school_vision_statistic
-- ----------------------------
DROP TABLE IF EXISTS `m_school_vision_statistic`;
CREATE TABLE `m_school_vision_statistic`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `school_id` int(10) UNSIGNED NOT NULL COMMENT '视力情况--所属的学校id',
  `school_type` int(10) UNSIGNED NOT NULL COMMENT '视力情况--学校类型',
  `school_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '视力情况--学校名',
  `screening_org_id` int(10) UNSIGNED NOT NULL COMMENT '视力情况--筛查机构名字',
  `screening_org_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '视力情况-筛查机构名',
  `screening_notice_id` int(10) UNSIGNED NOT NULL COMMENT '视力情况--所属的通知id',
  `screening_task_id` int(10) UNSIGNED NOT NULL COMMENT '视力情况--所属的任务id',
  `screening_plan_id` int(10) UNSIGNED NOT NULL COMMENT '视力情况--关联的筛查计划id',
  `district_id` int(10) UNSIGNED NOT NULL COMMENT '视力情况--所属的地区id',
  `avg_left_vision` decimal(3, 1) UNSIGNED NOT NULL COMMENT '视力情况--平均左眼视力（小数点后一位，默认0.0）',
  `avg_right_vision` decimal(3, 1) UNSIGNED NOT NULL COMMENT '视力情况--平均右眼视力（小数点后一位，默认0.0）',
  `low_vision_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--视力低下人数（默认0）',
  `low_vision_ratio` decimal(5, 2) UNSIGNED NOT NULL COMMENT '视力情况--视力低下比例（均为整数，如10.01%，数据库则是1001）',
  `wearing_glasses_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--戴镜人数（默认0）',
  `wearing_glasses_ratio` decimal(5, 2) UNSIGNED NOT NULL COMMENT '视力情况--戴镜人数（均为整数，如10.01%，数据库则是1001）',
  `myopia_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--近视人数（默认0）',
  `myopia_ratio` decimal(5, 2) UNSIGNED NOT NULL COMMENT '视力情况--近视比例（均为整数，如10.01%，数据库则是1001）',
  `ametropia_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--屈光不正人数（默认0）',
  `ametropia_ratio` decimal(5, 2) UNSIGNED NOT NULL COMMENT '视力情况--屈光不正比例（均为整数，如10.01%，数据库则是1001）',
  `vision_label0_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--零级预警人数（默认0）',
  `vision_label0_ratio` decimal(5, 2) UNSIGNED NOT NULL COMMENT '视力情况--零级预警比例（均为整数，如10.01%，数据库则是1001）',
  `vision_label1_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--一级预警人数（默认0）',
  `vision_label1_ratio` decimal(5, 2) UNSIGNED NOT NULL COMMENT '视力情况--一级预警比例（均为整数，如10.01%，数据库则是1001）',
  `vision_label2_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--二级预警人数（默认0）',
  `vision_label2_ratio` decimal(5, 2) UNSIGNED NOT NULL COMMENT '视力情况--二级预警比例（均为整数，如10.01%，数据库则是1001）',
  `vision_label3_numbers` int(11) NOT NULL DEFAULT 0 COMMENT '视力情况--三级预警人数（默认0）',
  `vision_label3_ratio` decimal(5, 2) NOT NULL COMMENT '视力情况--三级预警比例（均为整数，如10.01%，数据库则是1001）',
  `key_warning_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--重点视力对象数量（默认0）',
  `treatment_advice_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--建议就诊数量（默认0）',
  `treatment_advice_ratio` decimal(5, 2) UNSIGNED NOT NULL COMMENT '视力情况--建议就诊比例（均为整数，如10.01%，数据库则是1001）',
  `plan_screening_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--计划的学生数量（默认0）',
  `real_screening_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--实际筛查的学生数量（默认0）',
  `valid_screening_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--纳入统计的实际筛查学生数量（默认0）',
  `focus_targets_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '视力情况--重点视力对象数量（默认0）',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '视力情况--更新时间',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `school_vision_statistic_unique`(`screening_plan_id`, `school_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学校某次筛查计划统计视力情况表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_screening_notice
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_notice`;
CREATE TABLE `m_screening_notice`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `title` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '筛查通知--标题（最大25个字符）',
  `content` varchar(10000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '筛查通知--通知内容（长度未知）',
  `start_time` timestamp NULL COMMENT '筛查通知--开始时间（时间戳）',
  `end_time` timestamp NULL COMMENT '筛查通知--结束时间（时间戳）',
  `type` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查通知--通知类型（0是筛查通知-政府、1是筛查任务通知-筛查机构）',
  `gov_dept_id` int(10) UNSIGNED NOT NULL COMMENT '筛查通知--所处部门id',
  `district_id` int(10) UNSIGNED NOT NULL COMMENT '筛查通知--所处地区id',
  `screening_task_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查通知--来源的筛查任务id（type为1有）',
  `release_status` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查通知--通知状态（0未发布、1已发布）',
  `release_time` timestamp NULL COMMENT '筛查通知--发布时间（时间戳 ）',
  `operation_version` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查通知--操作人版本（版本自增，便于解决数据修改覆盖）',
  `create_user_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查通知--创建人id  ',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查通知--创建时间（时间戳）',
  `operator_id` int(11) NOT NULL DEFAULT 0 COMMENT '筛查通知--最后操作人id  ',
  `operate_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '筛查通知--最后操作时间（时间戳）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '筛查通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_screening_notice_dept_org
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_notice_dept_org`;
CREATE TABLE `m_screening_notice_dept_org`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `screening_notice_id` int(10) UNSIGNED NOT NULL COMMENT '筛查通知--筛查通知表id',
  `district_id` int(10) UNSIGNED NOT NULL COMMENT '筛查通知--接收对象所在的区域id',
  `accept_org_id` int(10) UNSIGNED NOT NULL COMMENT '筛查通知--接收通知对象的id（机构id 或者 部门id）',
  `operation_status` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查通知--操作状态（0未读 1 是已读 2是删除 3是已读已创建）',
  `operator_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查通知--操作人id（查看或者编辑的人id）',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `screening_task_plan_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查通知--该通知对应的筛查任务或筛查计划ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `m_screening_notice_id_accept_org_id_index`(`screening_notice_id`, `accept_org_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '筛查通知通知到的部门或者机构表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_screening_organization
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_organization`;
CREATE TABLE `m_screening_organization`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建人ID',
  `gov_dept_id` int(11) NOT NULL COMMENT '部门ID',
  `district_id` int(11) NOT NULL COMMENT '行政区域ID',
  `district_detail` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '行政区域json',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '筛查机构名称',
  `type` tinyint(4) NOT NULL COMMENT '筛查机构类型 0-医院,1-妇幼保健院,2-疾病预防控制中心,3-社区卫生服务中心,4-乡镇卫生院,5-中小学生保健机构,6-其他',
  `type_desc` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '机构类型描述',
  `config_type` tinyint(4) NOT NULL COMMENT '配置 0-省级配置 1-单点配置',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系方式',
  `province_code` bigint(20) DEFAULT NULL COMMENT '省代码',
  `city_code` bigint(20) DEFAULT NULL COMMENT '市代码',
  `area_code` bigint(20) DEFAULT NULL COMMENT '区代码',
  `town_code` bigint(20) DEFAULT NULL COMMENT '镇/乡代码',
  `address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '详细地址',
  `remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '说明',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-启用 1-禁止 2-删除',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `notification_config` json COMMENT '筛查机构告知书配置',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `m_screening_organization_name_uindex`(`name`) USING BTREE,
  INDEX `m_screening_organization_id_create_time_index`(`id`, `create_time`) USING BTREE,
  INDEX `m_screening_organization_status_create_time_index`(`status`, `create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '筛查机构表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_screening_organization_admin
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_organization_admin`;
CREATE TABLE `m_screening_organization_admin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建人ID',
  `screening_org_id` int(11) NOT NULL COMMENT '筛查机构表ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `gov_dept_id` int(11) NOT NULL COMMENT '部门ID',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `m_screening_organization_admin_screening_org_id_index`(`screening_org_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '机构-管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_screening_organization_staff
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_organization_staff`;
CREATE TABLE `m_screening_organization_staff`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `gov_dept_id` int(11) NOT NULL COMMENT '部门ID',
  `screening_org_id` int(11) NOT NULL COMMENT '筛查机构表ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建人ID',
  `sign_file_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '电子签名',
  `remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '说明',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `m_screening_organization_staff_screening_org_id_index`(`screening_org_id`) USING BTREE,
  INDEX `m_screening_organization_staff_user_id_index`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '机构-人员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_screening_plan
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_plan`;
CREATE TABLE `m_screening_plan`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `src_screening_notice_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查计划--所属的筛查源通知id（也即task的来源通知id），自己创建时默认0',
  `screening_task_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查计划--所属的筛查任务id',
  `title` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '筛查计划--标题',
  `content` varchar(10000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '筛查计划--内容',
  `start_time` timestamp NULL COMMENT '筛查计划--开始时间（时间戳）',
  `end_time` timestamp NULL COMMENT '筛查计划--结束时间（时间戳）',
  `gov_dept_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查计划--所处部门id',
  `district_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查计划--所处区域id',
  `screening_org_id` int(10) UNSIGNED NOT NULL COMMENT '筛查计划--指定的筛查机构id',
  `student_numbers` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查计划--计划的学生总数',
  `release_status` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查计划--发布状态 （0未发布 1已发布）',
  `release_time` timestamp NULL COMMENT '筛查计划--发布时间（时间戳）',
  `create_user_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查计划--创建者ID',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查计划--创建时间（时间戳）',
  `operator_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查计划--最后操作人id  ',
  `operate_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '筛查计划--最后操作时间（时间戳）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `m_screening_plan_screening_org_id_release_status_index`(`screening_org_id`, `release_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '筛查通知计划表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_screening_plan_school
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_plan_school`;
CREATE TABLE `m_screening_plan_school`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `screening_plan_id` int(10) UNSIGNED NOT NULL COMMENT '筛查计划--计划id ',
  `school_id` int(10) UNSIGNED NOT NULL COMMENT '筛查计划--执行的学校id',
  `dept_id` int(11) DEFAULT NULL,
  `school_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '筛查计划--学校名字',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `screening_org_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `m_screening_plan_school_screening_plan_id_school_id_index`(`screening_plan_id`, `school_id`) USING BTREE,
  INDEX `m_screening_plan_school_school_id_index`(`school_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '筛查计划关联的学校表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_screening_plan_school_student
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_plan_school_student`;
CREATE TABLE `m_screening_plan_school_student`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `screening_plan_id` int(10) UNSIGNED NOT NULL COMMENT '筛查计划--计划id ',
  `school_id` int(10) UNSIGNED NOT NULL COMMENT '筛查计划--执行的学校id',
  `school_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '筛查计划--执行的学校名字',
  `plan_district_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查计划--所处区域id',
  `school_district_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查计划--学生学校所处区域id',
  `grade_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查计划--参与筛查的学生年级ID',
  `screening_task_id` int(11) DEFAULT NULL,
  `class_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查计划--参与筛查的学生班级ID',
  `screening_org_id` int(11) DEFAULT NULL,
  `student_id` int(10) UNSIGNED NOT NULL COMMENT '筛查计划--参与筛查的学生id',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `id_card` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '筛查计划--参与筛查的学生身份证号码',
  `student_age` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查计划--参与筛查的学生年龄',
  `student_situation` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '筛查计划--参与筛查的当时情况',
  `student_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '筛查计划--参与筛查的学生编号',
  `student_name` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '筛查计划--参与筛查的学生名字',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查计划--创建时间',
  `grade_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `class_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `gender` tinyint(4) DEFAULT NULL,
  `src_screening_notice_id` int(11) DEFAULT 0 COMMENT '筛查计划--所属的筛查源通知id（也即task的来源通知id），自己创建时默认0',
  `grade_type` int(11) DEFAULT NULL,
  `province_code` bigint(20) DEFAULT NULL COMMENT '省代码',
  `city_code` bigint(20) DEFAULT NULL COMMENT '市代码',
  `area_code` int(11) DEFAULT NULL COMMENT '区代码',
  `town_code` bigint(20) DEFAULT NULL COMMENT '镇/乡代码',
  `address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '详细地址',
  `nation` tinyint(4) DEFAULT NULL COMMENT '民族 0-汉族',
  `parent_phone` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '家长手机号码',
  `school_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '学校编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `m_student_school_id_src_screening_notice_id_index`(`school_id`, `src_screening_notice_id`) USING BTREE,
  INDEX `m_screening_plan_school_student_screening_plan_id_index`(`screening_plan_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '参与筛查计划的学生表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_screening_task
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_task`;
CREATE TABLE `m_screening_task`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `screening_notice_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查任务--所属的通知id',
  `title` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '筛查任务--标题',
  `content` varchar(10000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '筛查任务--内容',
  `start_time` timestamp NULL COMMENT '筛查任务--开始时间（时间戳）',
  `end_time` timestamp NULL COMMENT '筛查任务--结束时间（时间戳）',
  `gov_dept_id` int(10) UNSIGNED NOT NULL COMMENT '筛查任务--所处部门id',
  `district_id` int(10) UNSIGNED NOT NULL COMMENT '筛查任务--所处区域id',
  `release_status` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查任务--发布状态 （0未发布 1已发布）',
  `release_time` timestamp NULL COMMENT '筛查任务--发布时间（时间戳）',
  `create_user_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查任务--创建者ID',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查任务--创建时间（时间戳）',
  `operator_id` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '筛查任务--最后操作人id  ',
  `operate_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '筛查任务--最后操作时间（时间戳）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `m_screening_task_screening_notice_id_gov_dept_id_index`(`screening_notice_id`, `gov_dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '筛查通知任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_screening_task_org
-- ----------------------------
DROP TABLE IF EXISTS `m_screening_task_org`;
CREATE TABLE `m_screening_task_org`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `screening_task_id` int(10) UNSIGNED NOT NULL COMMENT '筛查任务--筛查任务id',
  `screening_org_id` int(10) UNSIGNED NOT NULL COMMENT '筛查任务--筛查机构id',
  `quality_controller_name` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '筛查任务--机构质控员名字（长度限制未知）',
  `quality_controller_contact` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '筛查任务--机构质控员联系方式（长度限制未知）',
  `quality_controller_commander` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '筛查任务--机构质控员队长（长度限制未知）',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `m_screening_task_org_screening_task_id_screening_org_id_index`(`screening_task_id`, `screening_org_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '筛查任务关联的机构表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_stat_conclusion
-- ----------------------------
DROP TABLE IF EXISTS `m_stat_conclusion`;
CREATE TABLE `m_stat_conclusion`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `result_id` int(11) NOT NULL COMMENT '源筛查数据id',
  `src_screening_notice_id` int(11) DEFAULT NULL COMMENT '通知id',
  `task_id` int(11) NOT NULL COMMENT '任务id',
  `plan_id` int(11) NOT NULL COMMENT '计划ID',
  `screening_org_id` int(11) NOT NULL COMMENT '筛查机构id',
  `district_id` int(11) NOT NULL COMMENT '所属地区id',
  `age` int(11) DEFAULT NULL COMMENT '年龄',
  `school_age` int(11) NOT NULL COMMENT '学龄',
  `gender` int(11) NOT NULL COMMENT '性别',
  `warning_level` int(11) DEFAULT NULL COMMENT '预警级别',
  `vision_l` decimal(3, 1) DEFAULT NULL COMMENT '左眼视力',
  `vision_r` decimal(3, 1) DEFAULT NULL COMMENT '右眼视力',
  `is_low_vision` tinyint(1) DEFAULT NULL COMMENT '是否视力低下',
  `is_refractive_error` tinyint(1) DEFAULT NULL COMMENT '是否屈光不正',
  `is_myopia` tinyint(1) DEFAULT NULL COMMENT '是否近视',
  `is_hyperopia` tinyint(1) DEFAULT NULL COMMENT '是否远视',
  `is_astigmatism` tinyint(1) DEFAULT NULL COMMENT '是否散光',
  `is_wearing_glasses` tinyint(1) DEFAULT NULL COMMENT '是否戴镜',
  `is_warning_msg` tinyint(1) DEFAULT NULL COMMENT '是否视力警告',
  `vision_warning_update_time` timestamp(0) NOT NULL COMMENT '视力异常更新时间',
  `is_recommend_visit` tinyint(1) DEFAULT NULL COMMENT '是否建议就诊',
  `is_rescreen` tinyint(1) NOT NULL COMMENT '是否复测',
  `rescreen_error_num` int(11) NOT NULL COMMENT '复测错误项次',
  `is_valid` tinyint(1) NOT NULL COMMENT '是否有效数据',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `screening_plan_school_student_id` int(11) NOT NULL COMMENT '筛查计划学生ID',
  `student_id` int(11) NOT NULL COMMENT '学生id',
  `school_grade_code` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '学校年级代码',
  `school_id` int(10) UNSIGNED DEFAULT NULL,
  `school_class_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '学校班级名称',
  `myopia_warning_level` int(11) DEFAULT NULL COMMENT '近视预警级别',
  `naked_vision_warning_level` int(11) DEFAULT NULL COMMENT '裸眼视力预警级别',
  `glasses_type` int(11) DEFAULT NULL COMMENT '眼镜类型',
  `vision_correction` int(11) DEFAULT NULL COMMENT '视力矫正状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `result_id_UNIQUE`(`result_id`) USING BTREE,
  INDEX `idx_m_stat_conclusion_result_id`(`result_id`) USING BTREE,
  INDEX `notice_INDEX`(`src_screening_notice_id`, `district_id`, `create_time`) USING BTREE,
  INDEX `district_INDEX`(`district_id`, `is_rescreen`, `is_valid`, `create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '筛查数据结论' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_student
-- ----------------------------
DROP TABLE IF EXISTS `m_student`;
CREATE TABLE `m_student`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `school_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '学校编号',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建人ID',
  `sno` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '学号',
  `grade_id` int(11) DEFAULT NULL COMMENT '年级ID',
  `grade_type` tinyint(4) DEFAULT NULL COMMENT '学龄段',
  `class_id` int(11) DEFAULT NULL COMMENT '班级ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学生姓名',
  `gender` tinyint(1) NOT NULL COMMENT '性别 0-男 1-女',
  `birthday` timestamp NULL COMMENT '出生日期',
  `nation` tinyint(4) DEFAULT NULL COMMENT '民族 0-汉族',
  `id_card` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '身份证号码',
  `parent_phone` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '家长手机号码',
  `mp_parent_phone` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '家长公众号手机号码',
  `province_code` bigint(20) DEFAULT NULL COMMENT '省代码',
  `city_code` bigint(20) DEFAULT NULL COMMENT '市代码',
  `area_code` bigint(20) DEFAULT NULL COMMENT '区代码',
  `town_code` bigint(20) DEFAULT NULL COMMENT '镇/乡代码',
  `address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '详细地址',
  `avatar_file_id` int(11) DEFAULT NULL COMMENT '头像资源ID',
  `current_situation` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '当前情况',
  `vision_label` tinyint(4) DEFAULT NULL COMMENT '视力标签 0-零级、1-一级、2-二级、3-三级',
  `last_screening_time` timestamp NULL COMMENT '最近筛选时间',
  `remark` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-启用 1-禁止 2-删除',
  `glasses_type` int(11) DEFAULT NULL COMMENT '戴镜类型',
  `is_myopia` tinyint(1) DEFAULT NULL COMMENT '是否近视',
  `is_hyperopia` tinyint(1) DEFAULT NULL COMMENT '是否远视',
  `is_astigmatism` tinyint(1) DEFAULT NULL COMMENT '是否散光',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `m_student_id_card_uindex`(`id_card`) USING BTREE,
  INDEX `m_student_status_create_time_index`(`status`, `create_time`) USING BTREE,
  INDEX `m_student_school_no_status_class_id_grade_id_index`(`school_no`, `status`, `class_id`, `grade_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学校-学生表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_template
-- ----------------------------
DROP TABLE IF EXISTS `m_template`;
CREATE TABLE `m_template`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `type` tinyint(4) NOT NULL COMMENT '1-档案卡 2-筛查报告',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板名称',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `m_template_type_index`(`type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_template_district
-- ----------------------------
DROP TABLE IF EXISTS `m_template_district`;
CREATE TABLE `m_template_district`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `template_id` int(11) NOT NULL COMMENT '模板ID',
  `district_id` int(11) NOT NULL COMMENT '行政部门 使用模板的省份',
  `district_name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '省份名字',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `m_template_district_template_id_index`(`template_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '模板区域表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for m_vision_screening_result
-- ----------------------------
DROP TABLE IF EXISTS `m_vision_screening_result`;
CREATE TABLE `m_vision_screening_result`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `screening_plan_school_student_id` int(11) DEFAULT NULL COMMENT '筛查计划中的学生id',
  `screening_org_id` int(11) NOT NULL COMMENT '筛查结果--所属的机构id',
  `task_id` int(11) NOT NULL COMMENT '筛查结果--所属的任务id',
  `create_user_id` int(11) NOT NULL COMMENT '筛查结果--创建的用户id',
  `plan_id` int(11) DEFAULT NULL COMMENT '筛查结果--所属的计划id',
  `school_id` int(11) NOT NULL COMMENT '筛查结果--执行的学校id',
  `student_id` int(11) NOT NULL COMMENT '筛查结果--参与筛查的学生id',
  `district_id` int(11) DEFAULT NULL COMMENT '筛查结果--所属的地区id',
  `vision_data` json COMMENT '筛查结果--视力数据',
  `computer_optometry` json COMMENT '筛查结果--电脑验光',
  `biometric_data` json COMMENT '筛查结果--生物测量',
  `other_eye_diseases` json COMMENT '筛查结果--其他眼疾',
  `is_double_screen` tinyint(4) NOT NULL DEFAULT 0 COMMENT '筛查结果--是否复筛（0否，1是）',
  `is_notice` tinyint(4) DEFAULT 0 COMMENT '是否发送短信通知 0-否 1-是',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '筛查结果--更新时间',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `m_school_id_screening_org_id_plan_id_is_double_screen_index`(`school_id`, `screening_org_id`, `plan_id`, `is_double_screen`) USING BTREE,
  INDEX `m_vision_screening_result_is_double_screen_student_id_index`(`is_double_screen`, `student_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '筛查结果表' ROW_FORMAT = Dynamic;

-- 异常警告表
DROP TABLE IF EXISTS `m_warning_msg`;
CREATE TABLE `m_warning_msg` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `student_id` int unsigned NOT NULL COMMENT '学生id',
  `msg_template_id` int unsigned NOT NULL COMMENT '短信模板id',
  `phone_numbers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '电话号码(发送的时候才记录)',
  `send_status` tinyint NOT NULL DEFAULT '0' COMMENT '发送状态,-1发送失败,0准备发送,1是发送成功,2是取消发送',
  `send_time` timestamp NULL DEFAULT NULL COMMENT '待发送的时间',
  `send_times` tinyint unsigned NOT NULL COMMENT '发送次数',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `send_day_of_year` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发送时间(yyyyD)',
  PRIMARY KEY (`id`),
  KEY `idx_send_day` (`send_day_of_year`) USING BTREE COMMENT '发送日期的索引',
  KEY `idx_student_id` (`student_id`) USING BTREE COMMENT '学生id索引'
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 设备上传的筛查数据
DROP TABLE IF EXISTS `m_device_screening_data`;
CREATE TABLE `m_device_screening_data`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '唯一主键',
  `screening_org_id` int UNSIGNED NOT NULL COMMENT '数据归属的机构id',
  `device_id` int UNSIGNED  NOT NULL COMMENT '设备表id',
  `device_sn` varchar(32) NOT NULL COMMENT '设备唯一id',
  `patient_id` varchar(32) NOT NULL COMMENT '患者id',
  `patient_name` varchar(20) NOT NULL DEFAULT '' COMMENT '受检者名字',
  `patient_age_group` tinyint(1) NOT NULL DEFAULT -1 COMMENT '受检者年龄段(未知=-1,1=(0M,12M] 2=(12M,36M], 3=(3y,6Y], 4=(6Y-20Y], 5=(20Y,100Y])',
  `patient_gender` tinyint(1) NOT NULL DEFAULT -1 COMMENT '受检者性别(性别 男=0  女=1  未知 = -1)',
  `patient_age` int(1) NOT NULL DEFAULT -1 COMMENT '受检者年龄/月龄',
  `patient_org` varchar(64) NOT NULL DEFAULT '' COMMENT '受检者单位(可能是公司或者学校)',
  `patient_cid` char(18) NOT NULL DEFAULT '' COMMENT '受检者身份Id',
  `patient_dept` varchar(64) NOT NULL DEFAULT '' COMMENT '受检者部门(班级)',
  `patient_pno` char(11) NOT NULL DEFAULT '' COMMENT '受检者电话',
  `check_mode` tinyint NOT NULL DEFAULT -1 COMMENT '筛查模式. 双眼模式=0 ; 左眼模式=1; 右眼模式=2; 未知=-1',
  `check_result` tinyint NOT NULL DEFAULT -1 COMMENT '筛查结果(1=优, 2=良, 3=差,-1=未知)',
  `check_type` tinyint NOT NULL DEFAULT 0 COMMENT '筛查方式(0=个体筛查,1=批量筛查)',
  `left_cyl` decimal(4, 2) NULL DEFAULT NULL COMMENT '左眼柱镜',
  `right_cyl` decimal(4, 2) NULL DEFAULT NULL COMMENT '右眼柱镜',
  `left_axsi` decimal(4, 2) NULL DEFAULT NULL COMMENT '左眼轴位',
  `right_axsi` decimal(4, 2) NULL DEFAULT NULL COMMENT '右眼轴位',
  `left_pr` decimal(4, 2) NULL DEFAULT NULL COMMENT '左眼瞳孔半径',
  `right_pr` decimal(4, 2) NULL DEFAULT NULL COMMENT '右眼瞳孔半径',
  `left_pa` decimal(4, 2) NULL DEFAULT NULL COMMENT '左眼等效球镜度',
  `right_pa` decimal(4, 2) NULL DEFAULT NULL COMMENT '右眼等效球镜度',
  `left_sph` decimal(4, 2) NULL DEFAULT NULL COMMENT '左眼球镜',
  `right_sph` decimal(4, 2) NULL DEFAULT NULL COMMENT '右眼球镜',
  `pd` decimal(4, 2) NULL DEFAULT NULL COMMENT '瞳距',
  `do_check` tinyint(1) NOT NULL DEFAULT -1 COMMENT '是否筛查(-1=未知,1=是,0=否)',
  `left_axsi_v` int NULL DEFAULT NULL COMMENT '左垂直⽅向斜视度数',
  `right_axsi_v` int NULL DEFAULT NULL COMMENT '右垂直⽅向斜视度数',
  `left_axsi_h` int NULL DEFAULT NULL COMMENT '左⽔平⽅向斜视度数',
  `right_axsi_h` int NULL DEFAULT NULL COMMENT '右⽔平⽅向斜视度数',
  `red_reflect_left` int NULL DEFAULT NULL COMMENT '红光反射左眼',
  `red_reflect_right` int NULL DEFAULT NULL COMMENT '红光反射右眼',
  `screening_time` timestamp(0) NULL DEFAULT NULL COMMENT '筛查时间',
  `update_time` timestamp(0) NULL COMMENT '更新时间',
  `create_time` timestamp(0) NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uni_screeningorgid_devicesn_patientid_screeningtime`(`screening_org_id`,`device_sn`,`patient_id`,`screening_time`) USING BTREE COMMENT '数据id_筛查机构id_筛查时间'
);

-- 设备上传的原始数据
DROP TABLE IF EXISTS `m_device_source_data`;
CREATE TABLE `m_device_source_data`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `device_type` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '设备类型(0=默认设备,1=vs666)',
  `patient_id` int UNSIGNED NOT NULL COMMENT '患者id',
  `device_id`  int UNSIGNED  NOT NULL COMMENT '设备表id',
  `device_code` varchar(32) NOT NULL DEFAULT '' COMMENT '设备编码',
  `device_sn` varchar(32) NOT NULL COMMENT '设备唯一id',
  `src_data` varchar(512) NOT NULL COMMENT '原始数据',
  `screening_org_id` int UNSIGNED NOT NULL COMMENT '筛查机构id',
  `screening_time` timestamp(0) NOT NULL COMMENT '筛查时间',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`)
);

-- 设备表
DROP TABLE IF EXISTS `m_device`;
CREATE TABLE `m_device`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `device_sn` varchar(32) NOT NULL COMMENT '设备唯一id',
  `device_code` varchar(32) NOT NULL COMMENT '设备编码',
  `salesperson_name` varchar(20) NOT NULL DEFAULT '' COMMENT '销售名字',
  `salesperson_phone` char(11) NULL DEFAULT NULL COMMENT '销售电话',
  `binding_screening_org_id` int UNSIGNED NOT NULL COMMENT '绑定机构id',
  `customer_name` varchar(20) NOT NULL DEFAULT '' COMMENT '客户名字',
  `customer_phone` char(11) NULL DEFAULT NULL COMMENT '客户电话',
  `sale_date` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '销售时间',
  `remark` varchar(500) NOT NULL DEFAULT '' COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-启用、1-停用',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uni_device_uid`(`device_sn`) USING BTREE COMMENT '设备唯一标识码索引',
  INDEX `idx_binding_screening_org_id`(`binding_screening_org_id`) USING BTREE COMMENT '绑定机构的普通索引'
);


SET FOREIGN_KEY_CHECKS = 1;


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

-- 初始化部门表
INSERT INTO `m_government_department`(id, `name`, `pid`, `district_id`, `create_user_id`) VALUES (1, '运行中心', -1, -1, -1);

-- 初始化学生档案卡模板表
INSERT INTO m_template (id, type, name) VALUES (1, 1, '学生档案卡-模板1');
INSERT INTO m_template (id, type, name) VALUES (2, 1, '学生档案卡-模板2');
INSERT INTO m_template (id, type, name) VALUES (3, 1, '学生档案卡-模板3');
INSERT INTO m_template (id, type, name) VALUES (4, 1, '学生档案卡-模板4');
INSERT INTO m_template (id, type, name) VALUES (5, 2, '筛查报告-模板1');
INSERT INTO m_template (id, type, name) VALUES (6, 2, '筛查报告-模板2');
INSERT INTO m_template (id, type, name) VALUES (7, 2, '筛查报告-模板3');
INSERT INTO m_template (id, type, name) VALUES (8, 2, '筛查报告-模板4');

-- 初始化“其他”学校
INSERT INTO m_school (id, school_no, create_user_id, gov_dept_id, district_id, district_detail, name, kind, kind_desc, type, status) VALUES (1, '1234567890', 1, 1, -1, '', '其他', 2, '其他', 7, 0);
INSERT INTO m_school_grade (id, create_user_id, school_id, grade_code, name, status) VALUES (1, 1, 1, '90', '其他', 0);
INSERT INTO m_school_class (grade_id, create_user_id, school_id, name, seat_count, status) VALUES (1, 1, 1, '其他', 30, 0);

INSERT INTO m_device_report_template (name, device_type, template_type) VALUES ('VS666报告-标准模板', 1, 1);