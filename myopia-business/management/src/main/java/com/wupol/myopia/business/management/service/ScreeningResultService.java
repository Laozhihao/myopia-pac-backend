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

    /**
     * 获取学校ID
     *
     * @param taskId 通知任务
     * @return 学校ID
     */
    public List<Integer> getSchoolIdByTaskId(Integer taskId) {
        return baseMapper.getSchoolIdByTaskId(taskId);
    }

    /**
     * 获取筛查人员ID
     *
     * @param taskId 通知任务
     * @return 学校ID
     */
    public List<Integer> getCreateUserIdByTaskId(Integer taskId) {
        return baseMapper.getCreateUserIdByTaskId(taskId);
    }
}
