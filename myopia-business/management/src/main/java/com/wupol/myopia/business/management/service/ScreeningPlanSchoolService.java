package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    /**
     * 通过学校ID获取计划
     *
     * @param schoolId 学校ID
     * @return List<ScreeningPlanSchool>
     */
    public List<ScreeningPlanSchool> getBySchoolId(Integer schoolId) {
        return baseMapper
                .selectList(new QueryWrapper<ScreeningPlanSchool>()
                        .eq("school_id", schoolId));
    }
}
