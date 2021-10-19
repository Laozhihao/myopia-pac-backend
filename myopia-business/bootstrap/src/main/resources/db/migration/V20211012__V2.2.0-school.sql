-- m_student表新增视力情况等级相关字段
ALTER TABLE `m_student`
  ADD COLUMN `myopia_level` tinyint(1) COMMENT '近视等级：0-正常、1-筛查性近视、2-近视前期、3-低度近视、4-中度近视、5-重度近视',
  ADD COLUMN `hyperopia_level` tinyint(1) COMMENT '远视等级：0-正常、1-远视、2-低度远视、3-中度远视、4-重度远视',
  ADD COLUMN `astigmatism_level` tinyint(1) COMMENT '散光等级：0-正常、1-低度散光、2-中度散光、3-重度散光';