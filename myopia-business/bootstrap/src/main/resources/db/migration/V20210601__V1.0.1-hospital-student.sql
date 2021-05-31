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
