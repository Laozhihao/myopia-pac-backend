DROP TABLE IF EXISTS `m_warning_msg`;
CREATE TABLE `m_warning_msg`
(
    `id`              int unsigned NOT NULL AUTO_INCREMENT,
    `student_id`      int unsigned NOT NULL COMMENT '学生id',
    `msg_template_id` int unsigned NOT NULL COMMENT '短信模板id',
    `phone_numbers`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '电话号码(发送的时候才记录)',
    `send_status`     tinyint unsigned NOT NULL DEFAULT '0' COMMENT '发送状态,-1发送失败,0准备发送,1是发送成功,2是取消发送',
    `send_time`       timestamp NOT NULL COMMENT '待发送的时间',
    `update_time`     timestamp NOT NULL   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_time`     timestamp NOT NULL   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;