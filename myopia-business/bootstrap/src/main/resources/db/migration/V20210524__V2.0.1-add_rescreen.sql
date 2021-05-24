CREATE TABLE `m_stat_rescreen` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `screening_org_id` int(11) NOT NULL COMMENT '筛查结果--所属的机构id',
  `src_screening_notice_id` int(11) DEFAULT NULL COMMENT '通知id',
  `task_id` int(11) NOT NULL COMMENT '筛查结果--所属的任务id',
  `plan_id` int(11) NOT NULL COMMENT '筛查结果--所属的计划id',
  `school_id` int(11) NOT NULL COMMENT '筛查结果--执行的学校id',
  `screening_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '筛查日期',
  `rescreen_num` int(11) DEFAULT NULL COMMENT '当日测试人数',
  `wearing_glasses_rescreen_num` int(11) DEFAULT NULL COMMENT '戴镜复测人数',
  `wearing_glasses_rescreen_index_num` int(11) DEFAULT NULL COMMENT '戴镜复测指标数',
  `without_glasses_rescreen_num` int(11) DEFAULT NULL COMMENT '非戴镜复测人数',
  `without_glasses_rescreen_index_num` int(11) DEFAULT NULL COMMENT '非戴镜复测指标数',
  `rescreen_item_num` int(11) DEFAULT NULL COMMENT '复测项次',
  `incorrect_item_num` int(11) DEFAULT NULL COMMENT '错误项次数',
  `incorrect_ratio` float(10,2) DEFAULT NULL COMMENT '错误率/发生率',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '统计时间',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id_school_id` (`plan_id`,`school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复测统计表';


ALTER TABLE `m_school_monitor_statistic` ADD COLUMN `screening_plan_id`  int(10) UNSIGNED NOT NULL COMMENT '监测情况--关联的筛查计划id' AFTER `screening_task_id`;

ALTER TABLE `m_screening_plan_school` ADD COLUMN `quality_controller_name`  varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '机构质控员名字' AFTER `school_name`;
ALTER TABLE `m_screening_plan_school` ADD COLUMN `quality_controller_commander`  varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '机构质控员队长' AFTER `quality_controller_name`;

ALTER TABLE `m_screening_task_org`
MODIFY COLUMN `quality_controller_name`  varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '筛查任务--机构质控员名字（长度限制未知）' AFTER `screening_org_id`,
MODIFY COLUMN `quality_controller_contact`  varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '筛查任务--机构质控员联系方式（长度限制未知）' AFTER `quality_controller_name`,
MODIFY COLUMN `quality_controller_commander`  varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '筛查任务--机构质控员队长（长度限制未知）' AFTER `quality_controller_contact`;

