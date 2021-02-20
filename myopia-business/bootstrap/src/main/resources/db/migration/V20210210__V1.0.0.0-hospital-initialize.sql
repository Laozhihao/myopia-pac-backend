DROP TABLE IF EXISTS `h_hospital_student`;
CREATE TABLE `h_hospital_student`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `hospital_id` int NOT NULL COMMENT '医院id',
    `student_id`  int NOT NULL COMMENT '学生id',
    PRIMARY KEY (`id`),
    INDEX `hospital_id_index` (`hospital_id`),
    INDEX `student_id_index` (`student_id`)
) COMMENT ='医院-学生';

DROP TABLE IF EXISTS `h_department`;
CREATE TABLE `h_department`
(
    `id`          int                                 NOT NULL AUTO_INCREMENT,
    `hospital_id` int                                 NOT NULL COMMENT '医院id',
    `name`        varchar(255)                        NOT NULL COMMENT '科室名称',
    `create_time` timestamp default CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` timestamp default CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `hospital_id_index` (`hospital_id`)
) COMMENT ='医院-科室';

DROP TABLE IF EXISTS `h_doctor`;
CREATE TABLE `h_doctor`
(
    `id`              int          NOT NULL AUTO_INCREMENT,
    `gender`          tinyint      NOT NULL DEFAULT 0 COMMENT '状态 0-男 1-女',
    `name`            varchar(30)  NOT NULL COMMENT '名称',
    `user_id`         int          NOT NULL COMMENT '用户ID',
    `remark`          varchar(255) NULL COMMENT '说明',
    `hospital_id`     int          NOT NULL COMMENT '医院id',
    `department_id`   int          NOT NULL COMMENT '科室id',
    `department_name` varchar(50)  NULL COMMENT '科室名称',
    `title_name`      varchar(50)  NULL COMMENT '职称',
    `status`          tinyint      NOT NULL DEFAULT 0 COMMENT '状态 0-启用 1-禁止',
    `avatar_file_id`  int          NULL COMMENT '头像',
    `sign_file_id`    int          NULL COMMENT '电子签名',
    `create_time`     timestamp             default CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time`     timestamp             default CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `hospital_id_index` (`hospital_id`)
) COMMENT ='医院-医生表';

DROP TABLE IF EXISTS `h_consultation`;
CREATE TABLE `h_consultation`
(
    `id`           int                                 NOT NULL AUTO_INCREMENT,
    `student_id`   int                                 NOT NULL COMMENT '学生id',
    `hospital_id`  int                                 NULL COMMENT '医院id',
    `disease_list` json                                NULL COMMENT '病种',
    `create_time`  timestamp default CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `student_id_index` (`student_id`),
    INDEX `hospital_id_index` (`hospital_id`)
) COMMENT ='医院-学生问诊';

DROP TABLE IF EXISTS `h_medical_record`;
CREATE TABLE `h_medical_record`
(
    `id`              int                                 NOT NULL AUTO_INCREMENT,
    `student_id`      int                                 NOT NULL COMMENT '学生id',
    `hospital_id`     int                                 NULL COMMENT '医院id',
    `department_id`   int                                 NULL COMMENT '科室id',
    `doctor_id`       int                                 NOT NULL COMMENT '医生id',
    `consultation_id` int                                 NULL COMMENT '问诊内容id',
    `vision`          json                                NULL COMMENT '视力检查',
    `diopter`         json                                NULL COMMENT '屈光检查',
    `biometrics`      json                                NULL COMMENT '生物测量',
    `tosca`           json                                NULL COMMENT '角膜地形图',
    `status`          tinyint                             NOT NULL DEFAULT 0 COMMENT '状态。0检查中，1检查完成',
    `create_time`     timestamp default CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time`     timestamp default CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `student_id_index` (`student_id`),
    INDEX `hospital_id_index` (`hospital_id`),
    INDEX `doctor_id_index` (`doctor_id`)
) COMMENT ='医院-检查单';

DROP TABLE IF EXISTS `h_medical_report`;
CREATE TABLE `h_medical_report`
(
    `id`                int                                 NOT NULL AUTO_INCREMENT,
    `hospital_id`       int                                 NULL COMMENT '医院id',
    `department_id`     int                                 NULL COMMENT '科室id',
    `student_id`        int                                 NOT NULL COMMENT '学生id',
    `medical_record_id` int                                 NOT NULL COMMENT '对应的检查单id',
    `doctor_id`         int                                 NOT NULL COMMENT '医生id',
    `glasses_situation` tinyint                             NULL COMMENT '配镜情况。1配框架眼镜，2配OK眼镜，3配隐形眼镜',
    `medical_content`   varchar(300)                        NULL COMMENT '医生诊断',
    `file_id_list`      json                                NULL COMMENT '影像列表id',
    `create_time`       timestamp default CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time`       timestamp default CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `student_id_index` (`student_id`),
    INDEX `medical_record_id_index` (`medical_record_id`),
    INDEX `doctor_id_index` (`doctor_id`)
) COMMENT ='医院-检查报告';