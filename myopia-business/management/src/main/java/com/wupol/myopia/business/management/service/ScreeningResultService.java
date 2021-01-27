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

    /**
     * 通过StudentId获取筛查结果
     *
     * @param studentId id
     * @return List<ScreeningResult>
     */
    public List<ScreeningResult> getByStudentIds(Integer studentId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningResult>()
                .eq("student_id", studentId)
                .orderByDesc("create_time"));
    }

    /**
     * 通过计划ID获取结果
     *
     * @param planId 计划ID
     * @return 结果
     */
    public ScreeningResult getByPlanId(Integer planId) {
        return baseMapper.selectOne(new QueryWrapper<ScreeningResult>().eq("plan_id", planId));
    }

    public List<ScreeningResult> getByTaskIdGroupBySchoolId(Integer taskId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningResult>()
                .eq("task_id", taskId)
                .groupBy("school_id")
                .orderByDesc("create_time"));
    }
}
