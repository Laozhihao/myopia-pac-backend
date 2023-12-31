DROP TABLE IF EXISTS `h_preschool_check_record`;
CREATE TABLE `h_preschool_check_record`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `student_id` int(11) NOT NULL COMMENT '学生id',
  `hospital_id` int(11) NOT NULL COMMENT '医院id',
  `is_referral` tinyint(4) NULL DEFAULT 0 COMMENT '是否有检查前转诊信息[0-没有；1-有]',
  `from_referral` json NULL COMMENT '检查前转诊信息',
  `month_age` tinyint(4) NOT NULL COMMENT '月龄[0-新生儿；1-满月；2-3月龄；3-6月龄；4-8月龄；5-12月龄；6-18月龄；7-24月龄；8-30月龄；9-36月龄；10-4岁；11-5岁；12-6岁；]',
  `outer_eye` json NULL COMMENT '眼外观',
  `eye_disease_factor` json NULL COMMENT '主要眼病高危因素',
  `light_reaction` json NULL COMMENT '光照反应',
  `blink_reflex` json NULL COMMENT '瞬目反射',
  `red_ball_test` json NULL COMMENT '红球试验',
  `visual_behavior_observation` json NULL COMMENT '视物行为观察',
  `red_reflex` json NULL COMMENT '红光反射',
  `ocular_inspection` json NULL COMMENT '眼位检查',
  `vision_data` json NULL COMMENT '视力检查',
  `monocular_masking_aversion_test` json NULL COMMENT '单眼遮盖厌恶试验',
  `refraction_data` json NULL COMMENT '屈光检查',
  `guide_content` varchar(320) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '健康指导',
  `conclusion` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '眼病筛查及视力评估',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '总休情况[0 异常 ；1 正常]',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_student_id_hospital_id_month_age`(`student_id`, `hospital_id`, `month_age`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '眼保健信息表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `h_receipt_list`;
CREATE TABLE `h_receipt_list`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `preschool_check_record_id` int(11) NOT NULL COMMENT '生成回执的眼保健检查单id',
  `student_id` int(11) NOT NULL COMMENT '学生id',
  `special_medical` json NULL COMMENT '专项检查情况',
  `medical_result` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '诊断结果',
  `further_referral` tinyint(4) NOT NULL COMMENT '是否进一步转诊[0 否; 1 是]',
  `referral_hospital` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '转诊医院',
  `from_hospital_id` int(11) NULL DEFAULT NULL COMMENT '回执单所有医院id',
  `from_doctor_id` int(11) NULL DEFAULT NULL COMMENT '开具回执单医生id',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_pcr_id`(`preschool_check_record_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '回执单' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `h_referral_record`;
CREATE TABLE `h_referral_record`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `preschool_check_record_id` int(11) NOT NULL COMMENT '生成转诊的眼保健检查单id',
  `student_id` int(11) NOT NULL COMMENT '学生id',
  `from_hospital_id` int(11) NULL DEFAULT NULL COMMENT '申请医院id',
  `from_doctor_id` int(11) NULL DEFAULT NULL COMMENT '申请医师id',
  `to_hospital_id` int(11) NULL DEFAULT NULL COMMENT '目标医院id',
  `to_hospital` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标医院名称',
  `to_department` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标科室名称',
  `special_medical` json NULL COMMENT '未做专项检查',
  `disease_medical` json NULL COMMENT '初筛异常项目',
  `conclusion` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '检查结论',
  `referral_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '转诊状态[0 待就诊；1 已接诊]',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_pcr_id`(`preschool_check_record_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '转诊信息表' ROW_FORMAT = Dynamic;

alter table m_screening_plan_school_student
    modify student_situation varchar(2048) default '' not null comment '筛查计划--参与筛查的当时情况';

alter table h_hospital_student
    add student_type int null comment '学生类型 1-医院端 2-0到6岁 3医院和0到6';

alter table m_student
    modify record_no varchar(32) null comment '检查建档编码';

alter table h_hospital_student
    modify record_no varchar(32) null comment '检查建档编码';

create index h_hospital_student_status_student_type_index
    on h_hospital_student (status, student_type);