DROP TABLE IF EXISTS `m_resource_file`;
CREATE TABLE `m_resource_file` (
    `id` int(20) UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `file_name` VARCHAR(100) NOT NULL COMMENT '文件名称',
    `bucket` char(100) NOT NULL COMMENT '文件bucket',
    `s3_key` VARCHAR(500) DEFAULT NULL  COMMENT '文件s3 key',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `s3_key_unique_index` (`s3_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='文件表';