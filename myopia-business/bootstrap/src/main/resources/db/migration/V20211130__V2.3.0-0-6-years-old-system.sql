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
