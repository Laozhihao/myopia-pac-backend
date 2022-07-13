-- 通知表 标题字段修改大小
ALTER TABLE m_notice MODIFY COLUMN title varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '标题';