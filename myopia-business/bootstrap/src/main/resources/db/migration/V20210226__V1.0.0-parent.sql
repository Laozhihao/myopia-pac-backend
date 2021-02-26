-- 家长表
CREATE TABLE `m_parent` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '家长ID',
    `open_id` varchar(255) NOT NULL COMMENT '微信openId',
    `hash_key` varchar(255) NOT NULL COMMENT 'openId的hash值',
    `wx_header_img_url` varchar(500) DEFAULT NULL COMMENT '微信头像',
    `wx_nickname` varchar(100) DEFAULT NULL COMMENT '微信昵称',
    `user_id` int(11) DEFAULT NULL COMMENT '用户ID',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `m_parent_open_id_unique_index` (`open_id`) USING BTREE COMMENT 'openId唯一索引',
    UNIQUE KEY `m_parent_hash_key_unique_index` (`hash_key`) USING BTREE COMMENT 'openId的哈希值唯一索引',
    UNIQUE KEY `m_parent_user_id_unique_index` (`user_id`) USING BTREE COMMENT '用户ID唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='家长表';

-- 家长学生关系表
CREATE TABLE `m_parent_student` (
    `parent_id` int(11) NOT NULL COMMENT '家长ID',
    `student_id` int(11) NOT NULL COMMENT '学生ID',
    PRIMARY KEY (`parent_id`,`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='家长学生关系表';
