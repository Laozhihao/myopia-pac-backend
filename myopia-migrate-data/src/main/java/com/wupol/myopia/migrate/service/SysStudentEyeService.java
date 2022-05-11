package com.wupol.myopia.migrate.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.migrate.domain.mapper.SysStudentEyeMapper;
import com.wupol.myopia.migrate.domain.model.SysGradeClass;
import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import com.wupol.myopia.migrate.domain.model.SysStudentEyeSimple;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2022-03-23
 */
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@DS("data_source_db")
@Service
public class SysStudentEyeService extends BaseService<SysStudentEyeMapper, SysStudentEye> {

    /**
     * 获取简单版筛查数据
     *
     * @param deptId 筛查机构ID
     * @return java.util.List<com.wupol.myopia.migrate.domain.model.SysStudentEyeSimple>
     **/
    public List<SysStudentEyeSimple> getSimpleDataList(String deptId) {
        if (StringUtils.isBlank(deptId)) {
            return Collections.emptyList();
        }
        return baseMapper.getSimpleDataList(deptId);
    }

    /**
     * 根据学校ID获取所有年级和班级信息
     *
     * @param schoolId 学校ID
     * @return java.util.List<com.wupol.myopia.migrate.domain.model.SysGradeClass>
     **/
    public List<SysGradeClass> getAllGradeAndClassBySchoolId(String schoolId) {
        Assert.hasText(schoolId,"schoolId不能为空");
        return baseMapper.getAllGradeAndClassBySchoolId(schoolId);
    }
}
