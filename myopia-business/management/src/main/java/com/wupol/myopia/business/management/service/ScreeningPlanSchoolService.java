package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.myopia.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.ScreeningPlanSchoolMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchool;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class ScreeningPlanSchoolService extends BaseService<ScreeningPlanSchoolMapper, ScreeningPlanSchool> {

    public List<ScreeningPlanSchool> getBySchoolId(Integer schoolId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningPlanSchool>().eq("school_id", schoolId));
    }

    /**
     * 根据学校名获取ScreeningPlanSchoolStudent
     * @param schoolName
     * @param deptId
     * @return
     */
    public List<ScreeningPlanSchool> getSchoolByOrgIdAndSchoolName(String schoolName, Integer deptId) {
        if (deptId == null) {
            throw new ManagementUncheckedException("deptId 不能为空");
        }
        LambdaQueryWrapper<ScreeningPlanSchool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlanSchool::getScreeningOrgId,deptId).like(ScreeningPlanSchool::getSchoolName,schoolName);
        List<ScreeningPlanSchool> screeningPlanSchools = baseMapper.selectList(queryWrapper);
        return screeningPlanSchools;
    }
}
