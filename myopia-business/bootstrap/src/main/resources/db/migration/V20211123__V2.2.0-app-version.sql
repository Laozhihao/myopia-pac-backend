-- APP的apk版本管理表
CREATE TABLE `m_app_version` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID主键',
  `package_name` varchar(100) NOT NULL COMMENT '包名，例如：com.xbt.eyeproject',
  `channel` varchar(50) NOT NULL COMMENT '渠道，例如：Official-官方、HaiKou-海口、KunMing-昆明、JinCheng-晋城、YunCheng-运城',
  `version` varchar(30) NOT NULL COMMENT 'APP版本，例如：v1.2',
  `build_code` int(5) NOT NULL COMMENT '版本号，例如：10',
  `is_force_update` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否强制更新，0-否（默认）、1-是',
  `is_auto_update` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否自动下载，0-否（默认）、1-是',
  `apk_file_resource_id` int(11) NOT NULL COMMENT 'apk资源文件ID',
  `apk_file_name` varchar(100) DEFAULT NULL COMMENT 'apk文件名',
  `apk_file_size` bigint(20) DEFAULT NULL COMMENT 'apk大小，单位：b',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态，0-启用、1-停用（默认）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_user_id` int(11) DEFAULT NULL COMMENT '创建者ID',
  `thirdparty_qr_code_file_id` int(11) DEFAULT NULL COMMENT '第三方下载安装包二维码图片文件ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `package_name_channel_version_unique_index` (`package_name`,`channel`,`version`) USING BTREE COMMENT '包名、渠道和版本号作为唯一索引'
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='APP的apk版本管理表';