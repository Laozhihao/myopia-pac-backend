-- 更新APP的apk版本管理表的status字段
ALTER TABLE `m_app_version`
  MODIFY COLUMN `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态，0-停用（默认）、1-启用';