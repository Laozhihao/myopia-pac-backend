-- 新增筛查机构类型
alter table `m_screening_plan`
    add screening_org_type TINYINT(3) NOT NULL default 0 comment '筛查机构类型(0:筛查机构，1:学校，2:医院)' after `screening_org_id`;

alter table `m_screening_task_org`
    add screening_org_type TINYINT(3) NOT NULL default 0 comment '筛查机构类型(0:筛查机构，1:学校，2:医院)' after `screening_org_id`;

ALTER TABLE `m_screening_plan` modify COLUMN title varchar(30) NOT NULL COMMENT '筛查计划--标题';

alter table `m_screening_plan_school`
    add screening_grade_ids varchar(255) NULL  comment '筛查年级ID集合';
