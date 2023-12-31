DROP TABLE IF EXISTS `m_work_order`;
CREATE TABLE `m_work_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL COMMENT '学生姓名',
  `gender` tinyint(1) NOT NULL COMMENT '性别 0-男 1-女',
  `passport` varchar(32) DEFAULT NULL COMMENT '护照',
  `id_card` varchar(32) DEFAULT NULL COMMENT '身份证号码',
  `birthday` timestamp NULL DEFAULT NULL COMMENT '出生日期',
  `school_id` int(11) NOT NULL COMMENT '学校ID',
  `grade_id` int(11) NOT NULL COMMENT '年级ID',
  `class_id` int(11) NOT NULL COMMENT '班级ID',
  `sno` varchar(64) DEFAULT NULL COMMENT '学号',
  `status` tinyint(4) NOT NULL COMMENT '状态 0-已处理 1-未处理 2-无法处理',
  `create_user_id` int(11) NOT NULL COMMENT '创建人ID',
  `term` tinyint(4) NOT NULL COMMENT '提交页面 0-绑定页面 1-档案页面',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `old_data` json DEFAULT NULL COMMENT '工单选中的学生被处理前的数据快照',
  `content` varchar(255) DEFAULT NULL COMMENT '留言内容',
  `grade_type` tinyint(4) DEFAULT NULL COMMENT '学龄段',
  `parent_phone` varchar(16) NOT NULL COMMENT '家长手机号码',
  `wx_nickname` varchar(100) NOT NULL COMMENT '微信昵称',
  `screening_begin_time` timestamp NULL DEFAULT NULL COMMENT '筛查开始时间',
  `screening_end_time` timestamp NULL DEFAULT NULL COMMENT '筛查结束时间',
  `is_notice` tinyint(4) DEFAULT '0' COMMENT '是否发送短信通知 0-否 1-是',
  `screening_id` int(11) DEFAULT NULL COMMENT '修改筛查记录id',
  `new_data` json DEFAULT NULL COMMENT '工单选中的学生被处理后的数据快照',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;


alter table m_screening_organization_staff
    add type tinyint(1) NOT NULL DEFAULT '0' COMMENT '筛查人员类型（0普通筛查人员，1自动生成的筛查人员）';

alter table m_work_order
    add view_status tinyint(1) NOT NULL DEFAULT '0' COMMENT '用户查看工单处理状态（0未读，1已读）';

INSERT INTO `m_template` (`id`, `type`, `name`, `create_time`, `update_time`) VALUES (3, 1, '学生档案卡-近视筛查结果记录表', '2022-03-04 15:41:24', '2022-03-04 15:41:27');
UPDATE m_template	SET `name`='筛查报告-学校维度样板1' WHERE `name`='筛查报告-模板1';
UPDATE m_template	SET `name`='筛查报告-计划维度样板1' WHERE `name`='筛查报告-模板2';
UPDATE m_template	SET `name`='筛查报告-区域维度样板1' WHERE `name`='筛查报告-模板3';
DELETE FROM m_template WHERE `name`='筛查报告-模板4';

alter table m_device
    add type int default 0 null comment '类型 0-默认 1-vs666 2-灯箱 3-体脂秤' after status;