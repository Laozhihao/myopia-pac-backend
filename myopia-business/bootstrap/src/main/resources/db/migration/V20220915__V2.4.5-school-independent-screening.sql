-- 新增筛查机构类型
alter table `m_screening_plan`
    add screening_org_type TINYINT(3) NOT NULL default 0 comment '筛查机构类型(0:筛查机构，1:学校，2:医院)' after `screening_org_id`;

-- 学校的学生信息新增家庭信息
alter table `m_school_student` add family_info json DEFAULT NULL COMMENT '家庭信息';
alter table `m_school_student` add committeeCode bigint DEFAULT NULL COMMENT '委会行政区域编码';
