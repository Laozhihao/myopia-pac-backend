DROP TABLE IF EXISTS `m_work_order`;
CREATE TABLE `m_work_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL COMMENT '学生姓名',
  `gender` int(1) NOT NULL COMMENT '性别 0-男 1-女',
  `passport` varchar(32) DEFAULT NULL COMMENT '护照',
  `id_card` varchar(32) DEFAULT NULL COMMENT '身份证号码',
  `birthday` timestamp NULL DEFAULT NULL COMMENT '出生日期',
  `school_id` int(11) NOT NULL COMMENT '学校ID',
  `grade_id` int(11) NOT NULL COMMENT '年级ID',
  `class_id` int(11) NOT NULL COMMENT '班级ID',
  `sno` varchar(64) DEFAULT NULL COMMENT '学号',
  `status` int(4) NOT NULL COMMENT '状态 0-已处理 1-未处理 2-无法处理',
  `create_user_id` int(11) NOT NULL COMMENT '创建人ID',
  `term` int(4) NOT NULL COMMENT '提交页面 0-绑定页面 1-档案页面',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `old_data` json DEFAULT NULL COMMENT '对比结果',
  `content` varchar(255) DEFAULT NULL COMMENT '留言内容',
  `grade_type` tinyint(4) DEFAULT NULL COMMENT '学龄段',
  `parent_phone` varchar(16) NOT NULL COMMENT '家长手机号码',
  `wx_nickname` varchar(100) NOT NULL COMMENT '微信昵称',
  `screening_begin_time` timestamp NULL DEFAULT NULL COMMENT '筛查开始时间',
  `screening_end_time` timestamp NULL DEFAULT NULL COMMENT '筛查结束时间',
  `is_notice` tinyint(4) DEFAULT '0' COMMENT '是否发送短信通知 0-否 1-是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;