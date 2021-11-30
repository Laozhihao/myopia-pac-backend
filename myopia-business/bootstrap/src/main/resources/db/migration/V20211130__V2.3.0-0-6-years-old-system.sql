-- 0-6岁系统
ALTER TABLE `h_doctor`
ADD COLUMN `create_user_id`  int(11) NULL DEFAULT NULL COMMENT '创建人ID' AFTER `user_id`,
ADD COLUMN `phone`  char(11) NULL COMMENT '手机号码' AFTER `create_user_id`;