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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- 更改m_stat_conclusion的表结构
ALTER TABLE `m_stat_conclusion` ADD COLUMN `student_id` int(11) UNSIGNED  NOT NULL COMMENT '学生id' after `vision_correction` ;
ALTER TABLE `m_stat_conclusion` ADD COLUMN `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`;
ALTER TABLE `m_stat_conclusion` ADD COLUMN `is_vision_warning` tinyint(1) UNSIGNED COMMENT '是否视力警告' AFTER `is_rescreen`;
ALTER TABLE `m_stat_conclusion` ADD COLUMN `vision_warning_update_time` timestamp(0) NOT NULL COMMENT '视力异常更新时间' AFTER `is_vision_warning`;

-- 更新数据m_stat_conclusion表的student_id
update `m_stat_conclusion` c,`m_vision_screening_result` r set c.student_id = r.student_id where r.id = c.result_id;