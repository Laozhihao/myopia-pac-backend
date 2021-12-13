-- 表m_hospital新增字段
ALTER TABLE `m_hospital`
  ADD COLUMN `service_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '服务类型（配置），0：居民健康系统(默认)、1：0-6岁眼保健、2：0-6岁眼保健+居民健康系统',
  ADD COLUMN `cooperation_type` tinyint(1) COMMENT '合作类型，0-合作 1-试用',
  ADD COLUMN `cooperation_time_type` tinyint(1) COMMENT '合作期限类型，-1-自定义 0-30天 1-60天 2-180天 3-1年 4-2年 5-3年',
  ADD COLUMN `cooperation_start_time` timestamp(3) NULL DEFAULT NULL COMMENT '合作开始时间',
  ADD COLUMN `cooperation_end_time` timestamp(3) NULL DEFAULT NULL COMMENT '合作结束时间',
  ADD COLUMN `associate_screening_org_id` int(11) COMMENT '关联筛查机构的ID';

-- 医生信息表
truncate table `h_doctor`;
ALTER TABLE `h_doctor`
DROP COLUMN `gender`,
DROP COLUMN `status`;

-- 筛查机构表
ALTER TABLE `m_screening_organization`
ADD COLUMN `cooperation_type` tinyint(4) NULL DEFAULT NULL COMMENT '合作类型 0-合作 1-试用' AFTER `status`,
ADD COLUMN `cooperation_time_type` tinyint(4) NULL DEFAULT NULL COMMENT '合作期限类型 -1-自定义 0-30天 1-60天 2-180天 3-1年 4-2年 5-3年' AFTER `cooperation_type`,
ADD COLUMN `cooperation_start_time` timestamp(3) NULL DEFAULT NULL COMMENT '合作开始时间' AFTER `cooperation_time_type`,
ADD COLUMN `cooperation_end_time` timestamp(3) NULL DEFAULT NULL COMMENT '合作结束时间' AFTER `cooperation_start_time`;

-- 学校表
ALTER TABLE `m_school`
ADD COLUMN `cooperation_type` tinyint(4) NULL DEFAULT NULL COMMENT '合作类型 0-合作 1-试用' AFTER `status`,
ADD COLUMN `cooperation_time_type` tinyint(4) NULL DEFAULT NULL COMMENT '合作期限类型 -1-自定义 0-30天 1-60天 2-180天 3-1年 4-2年 5-3年' AFTER `cooperation_type`,
ADD COLUMN `cooperation_start_time` timestamp(3) NULL DEFAULT NULL COMMENT '合作开始时间' AFTER `cooperation_time_type`,
ADD COLUMN `cooperation_end_time` timestamp(3) NULL DEFAULT NULL COMMENT '合作结束时间' AFTER `cooperation_start_time`;

-- 0-6岁系统
alter table m_student
    add is_newborn_without_id_card tinyint(1) default false null comment '是否新生儿暂无身份证 false-否 true-是';

alter table m_student
    add family_info json null comment '家庭信息';

alter table m_student
    add committee_code bigint null comment '委会行政区域code';

alter table m_student
    add record_no bigint null comment '检查建档编码';

alter table h_hospital_student
    add is_newborn_without_id_card tinyint(1) default false null comment '是否新生儿暂无身份证 false-否 true-是';

alter table h_hospital_student
    add family_info json null comment '家庭信息';

alter table h_hospital_student
    add committee_code bigint null comment '委会行政区域code';

alter table h_hospital_student
    add record_no bigint null comment '检查建档编码';