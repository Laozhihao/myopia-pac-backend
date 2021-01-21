package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.ScreeningResultMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class ScreeningResultService extends BaseService<ScreeningResultMapper, ScreeningResult> {

    List<ScreeningResult> getByPlanSchoolStudentIds(List<Integer> ids) {
        return baseMapper.selectList(new QueryWrapper<ScreeningResult>().in("screening_plan_school_student_id", ids));
    }

}
