-- sql 加上注释说明描述
-- app筛查端原始数据
ALTER TABLE m_vision_screening_result ADD saprodontia_data json NULL COMMENT '龋齿检查';
ALTER TABLE m_vision_screening_result ADD spine_data json NULL COMMENT '脊柱检查';
ALTER TABLE m_vision_screening_result ADD blood_pressure_data json NULL COMMENT '血压检查';
ALTER TABLE m_vision_screening_result ADD diseases_history_data  json NULL COMMENT '疾病史';
ALTER TABLE m_vision_screening_result ADD privacy_data json NULL COMMENT '个人隐私';
ALTER TABLE m_vision_screening_result ADD deviation_data json NULL COMMENT '筛查不准确说明';
ALTER TABLE m_screening_plan_school_student ADD state tinyint(1) NOT NULL default 0 COMMENT '未做检查说明【0:无；1：请假；2：转学;3:其他】';
ALTER TABLE m_vision_screening_result ADD screening_type tinyint(1) NOT NULL default 0 COMMENT '筛查类型--来自筛查计划，筛查计划强一致 （0：视力筛查，1；常见病）';
ALTER TABLE m_screening_plan ADD screening_type tinyint(1) NOT NULL default 0 COMMENT '筛查类型（0：视力筛查，1；常见病）';
-- 筛查计划表添加筛查类型字段update_screening_end_time_status
ALTER TABLE m_screening_plan ADD update_screening_end_time_status tinyint(1) NOT NULL default 0 COMMENT '修改筛查结束时间状态（0：未修改，1；已修改）';
-- 筛查通知表添加筛查类型字段screening_type
ALTER TABLE m_screening_notice ADD screening_type tinyint(1) NOT NULL default 0 COMMENT '筛查类型（0：视力筛查，1；常见病）';
-- 筛查任务表添加筛查类型字段screening_type
ALTER TABLE m_screening_task ADD screening_type tinyint(1) NOT NULL default 0 COMMENT '筛查类型（0：视力筛查，1；常见病）';
-- 筛查计划关联的学校表字段quality_controller_name必填改为非必填
alter table m_screening_plan_school modify quality_controller_name varchar(25) DEFAULT NULL COMMENT '机构质控员名字';
-- 筛查计划关联的学校表字段quality_controller_commander必填改为非必填
alter table m_screening_plan_school modify quality_controller_commander varchar(25) DEFAULT NULL COMMENT '机构质控员队长';
-- 将筛查通知表的数据迁移到筛查通知通知到的部门或者机构表
INSERT into m_screening_notice_dept_org (screening_notice_id,district_id,accept_org_id,operation_status,operator_id,create_time) SELECT id,district_id,gov_dept_id,3,create_user_id,create_time FROM m_screening_notice;


-- 筛查数据结论表 新增筛查类型字段
ALTER TABLE m_stat_conclusion ADD screening_type TINYINT(3) DEFAULT 0 NOT NULL COMMENT '筛查类型：0-视力筛查、1-常见病';
ALTER TABLE m_stat_conclusion ADD physique_rescreen_error_num INT UNSIGNED DEFAULT 0 NOT NULL COMMENT '体格复测错误项次';

-- 筛查数据结论表新增常见病的字段
ALTER TABLE m_stat_conclusion ADD is_saprodontia TINYINT(1) NULL COMMENT '是否龋齿';
ALTER TABLE m_stat_conclusion ADD is_overweight TINYINT(1) NULL COMMENT '是否超重';
ALTER TABLE m_stat_conclusion ADD is_obesity TINYINT(1) NULL COMMENT '是否肥胖';
ALTER TABLE m_stat_conclusion ADD is_malnutrition TINYINT(1) NULL COMMENT '是否营养不良';
ALTER TABLE m_stat_conclusion ADD is_stunting TINYINT(1) NULL COMMENT '是否生长迟缓';
ALTER TABLE m_stat_conclusion ADD is_spinal_curvature TINYINT(1) NULL COMMENT '是否脊柱弯曲';
ALTER TABLE m_stat_conclusion ADD is_normal_blood_pressure TINYINT(1) NULL COMMENT '是否血压正常';
ALTER TABLE m_stat_conclusion ADD is_diseases_history TINYINT(1) NULL COMMENT '是否有疾病史';
ALTER TABLE m_stat_conclusion ADD is_nocturnal_emission TINYINT(1) NULL COMMENT '是否遗精';
ALTER TABLE m_stat_conclusion ADD is_menarche TINYINT(1) NULL COMMENT '是否初潮';
ALTER TABLE m_stat_conclusion ADD is_review TINYINT(1) NULL COMMENT '是否复查';


ALTER TABLE m_stat_conclusion ADD is_anisometropia TINYINT(1) NULL COMMENT '是否屈光参差';
ALTER TABLE m_stat_conclusion MODIFY COLUMN is_saprodontia tinyint(1) NULL COMMENT '是否龋患';
ALTER TABLE m_stat_conclusion ADD saprodontia_teeth INT NULL COMMENT '龋患牙齿数';
ALTER TABLE m_stat_conclusion ADD is_saprodontia_loss TINYINT(1) NULL COMMENT '是否龋失';
ALTER TABLE m_stat_conclusion ADD saprodontia_loss_teeth INT NULL COMMENT '龋失牙齿数';
ALTER TABLE m_stat_conclusion ADD is_saprodontia_repair TINYINT(1) NULL COMMENT '是否龋补';
ALTER TABLE m_stat_conclusion ADD saprodontia_repair_teeth INT NULL COMMENT '龋补牙齿数';

ALTER TABLE m_stat_conclusion ADD rescreen_item_num INT NULL COMMENT '复测项次数';
ALTER TABLE m_stat_conclusion ADD low_vision_level INT NULL COMMENT '视力低下等级';
ALTER TABLE m_stat_conclusion ADD is_cooperative TINYINT(3) NULL COMMENT '是否配合检查(0-配合、1-不配合)';
ALTER TABLE m_stat_conclusion ADD screening_myopia TINYINT(3) NULL COMMENT '筛查性近视';

ALTER TABLE m_school_student ADD screening_myopia TINYINT(3) NULL COMMENT '筛查性近视';
ALTER TABLE m_student ADD screening_myopia TINYINT(3) NULL COMMENT '筛查性近视';

ALTER TABLE m_school_student ADD low_vision TINYINT(3) NULL COMMENT '视力低下';
ALTER TABLE m_student ADD low_vision TINYINT(3) NULL COMMENT '视力低下';


-- 新增筛查数据结果统计表
DROP TABLE IF EXISTS `m_screening_result_statistic`;
CREATE TABLE `m_screening_result_statistic` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `screening_notice_id` int NOT NULL COMMENT '所属的通知id',
  `screening_task_id` int NOT NULL COMMENT '关联的任务id（is_total情况下，可能为0）',
  `screening_plan_id` int NOT NULL COMMENT '筛查计划id',
  `screening_type` tinyint NOT NULL COMMENT '筛查类型 （0-视力筛查、1-常见病筛查）',
  `screening_org_id` int NOT NULL COMMENT '筛查机构id',
  `school_id` int NOT NULL COMMENT '学校ID',
  `school_type` tinyint NOT NULL COMMENT '学校类型 0-小学,1-初级中学,2-高级中学,3-完全中学,4-九年一贯制学校,5-十二年一贯制学校,6-职业高中,7-其他,8-幼儿园',
  `school_num` int DEFAULT NULL COMMENT '学校数',
  `district_id` int unsigned NOT NULL COMMENT '筛查范围、所属的地区id',
  `plan_screening_num` int unsigned NOT NULL DEFAULT '0' COMMENT '计划的学生数量（默认0）',
  `real_screening_num` int unsigned NOT NULL DEFAULT '0' COMMENT '实际筛查的学生数量（默认0）',
  `finish_ratio` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '完成率',
  `valid_screening_num` int unsigned NOT NULL DEFAULT '0' COMMENT '纳入统计的实际筛查学生数量（默认0）',
  `valid_screening_ratio` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '纳入统计的实际筛查学生比例',
  `is_total` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否合计数据',
  `vision_analysis` json DEFAULT NULL COMMENT '视力分析',
  `rescreen_situation` json DEFAULT NULL COMMENT '复测情况',
  `vision_warning` json DEFAULT NULL COMMENT '视力预警',
  `saprodontia` json DEFAULT NULL COMMENT '龋齿情况',
  `common_disease` json DEFAULT NULL COMMENT '常见病分析',
  `questionnaire` json DEFAULT NULL COMMENT '问卷情况',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `screening_result_statistic_unique` (`screening_plan_id`,`screening_type`,`screening_org_id`,`school_id`,`school_type`,`district_id`,`is_total`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='筛查结果统计表';

-- 复查统计
alter table m_stat_rescreen
    add screening_type tinyint(1) default 0 null comment '0-视力筛查 1-常见病' after school_id;

alter table m_stat_rescreen
    add physique_rescreen_num int null comment '体格复查人数';

alter table m_stat_rescreen
    add physique_index_num int null comment '体格复查指数';

alter table m_stat_rescreen
    add physique_rescreen_item_num int null comment '体格-复测项次';

alter table m_stat_rescreen
    add physique_incorrect_item_num int null comment '体格-错误项次数';

alter table m_stat_rescreen
    add physique_incorrect_ratio float(10, 2) null comment '体格-错误率/发生率';

alter table m_school
    modify type tinyint not null comment '学校类型 0-小学,1-初级中学,2-高级中学,3-完全中学,4-九年一贯制学校,5-十二年一贯制学校,6-职业高中,7其他,8幼儿园,9大学';

-- 学生常见病ID表，m_student_common_disease_id
DROP TABLE IF EXISTS `m_student_common_disease_id`;
CREATE TABLE `m_student_common_disease_id` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID主键',
  `student_id` int(11) NOT NULL COMMENT '学生ID',
  `area_district_short_code` varchar(6) NOT NULL COMMENT '区/县行政区域编码简称（6位）',
  `school_id` int(11) NOT NULL COMMENT '学校ID',
  `grade_id` int(11) NOT NULL COMMENT '年级ID',
  `year` int(4) NOT NULL COMMENT '年份，如：2016、2019、2022',
  `common_disease_code` varchar(4) NOT NULL COMMENT '学生常见病编码，4位（同一年，同年级下，从0001到9999）',
  `common_disease_id` varchar(16) NOT NULL COMMENT '学生常见病ID，16位',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `area_type` tinyint(1) NOT NULL COMMENT '片区类型',
  `monitor_type` tinyint(1) NOT NULL COMMENT '监测点类型',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_index_student_id_grade_id_year_code` (`student_id`,`grade_id`,`year`,`common_disease_code`,`area_district_short_code`) USING BTREE COMMENT '唯一索引'
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='学生常见病ID';

-- 学校常见病编码表，m_school_common_disease_code
DROP TABLE IF EXISTS `m_school_common_disease_code`;
CREATE TABLE `m_school_common_disease_code` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_district_short_code` varchar(6) NOT NULL COMMENT '区/县行政区域编码简称（6位）',
  `school_id` int(11) NOT NULL COMMENT '学校ID',
  `year` int(4) NOT NULL COMMENT '年份，如：2016、2019、2022',
  `code` varchar(2) NOT NULL COMMENT '学校常见病编码，2位，同年同区域下学校从01到99',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_index_school_id_year_code` (`school_id`,`year`,`code`,`area_district_short_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='学校常见病编码';

-- 表m_screening_plan_school_student，加字段
ALTER TABLE m_screening_plan_school_student ADD COLUMN common_disease_id varchar(16) COMMENT '常见病ID，16位';

-- 模板表加字段
ALTER TABLE m_template ADD COLUMN biz tinyint(1) NOT NULL DEFAULT 1 COMMENT '业务类型：1-视力筛查、2-常见病' AFTER id;
-- 更新模板名称
UPDATE m_template SET name = '视力筛查报告-学校维度样板1' WHERE id = 5;
UPDATE m_template SET name = '视力筛查报告-计划维样板1' WHERE id = 6;
UPDATE m_template SET name = '视力筛查报告-区域维度样板1' WHERE id = 7;
-- 新增模板
INSERT INTO `m_template` ( `id`, `biz`, `type`, `name` )
VALUES
       ( 8, 2, 1, '学生监测表-常见病筛查结果记录表' ),
       ( 9, 2, 2, '常见病筛查报告-学校维度样板1' ),
       ( 10, 2, 2, '常见病筛查报告-计划维度样板1' ),
       ( 11, 2, 2, '常见病筛查报告-区域维度样板1' );
-- 新模板默认为全国使用
INSERT INTO `m_template_district`(`template_id`, `district_id`, `district_name`) SELECT 8, id, name FROM m_district WHERE parent_code = 100000000;
INSERT INTO `m_template_district`(`template_id`, `district_id`, `district_name`) SELECT 9, id, name FROM m_district WHERE parent_code = 100000000;
INSERT INTO `m_template_district`(`template_id`, `district_id`, `district_name`) SELECT 10, id, name FROM m_district WHERE parent_code = 100000000;
INSERT INTO `m_template_district`(`template_id`, `district_id`, `district_name`) SELECT 11, id, name FROM m_district WHERE parent_code = 100000000;

ALTER TABLE m_stat_conclusion ADD disease_num json NULL COMMENT '疾病统计数';