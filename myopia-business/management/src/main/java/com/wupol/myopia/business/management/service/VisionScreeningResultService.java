package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.myopia.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.builder.ScreeningResultBuilder;
import com.wupol.myopia.business.management.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.management.domain.mapper.ScreeningResultMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.util.TwoTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class VisionScreeningResultService extends BaseService<ScreeningResultMapper, VisionScreeningResult> {

    @Autowired
    private ScreeningPlanService screeningPlanService;

    /**
     * 通过StudentId获取筛查结果
     *
     * @param studentId id
     * @return List<ScreeningResult>
     */
    public List<VisionScreeningResult> getByStudentIds(Integer studentId) {
        return baseMapper.selectList(new QueryWrapper<VisionScreeningResult>()
                .eq("student_id", studentId)
                .orderByDesc("create_time"));
    }

    /**
     * 通过计划ID获取结果
     *
     * @param planId 计划ID
     * @return 结果
     */
    public VisionScreeningResult getByPlanId(Integer planId) {
        return baseMapper.selectOne(new QueryWrapper<VisionScreeningResult>().eq("plan_id", planId));
    }

    public List<VisionScreeningResult> getByTaskId(Integer taskId) {
        return baseMapper.selectList(new QueryWrapper<VisionScreeningResult>()
                .eq("task_id", taskId)
                .orderByDesc("create_time"));
    }


    /**
     * 获取已有的筛查结果
     *
     * @param screeningResultBasicData
     * @return
     * @throws IOException
     */
    public TwoTuple<VisionScreeningResult, ScreeningPlan> getScreeningResultAndScreeningPlan(ScreeningResultBasicData screeningResultBasicData) throws IOException {
        ScreeningPlan screeningPlan = screeningPlanService.getScreeningPlanDTO(screeningResultBasicData);
        if (screeningPlan == null) {
            throw new ManagementUncheckedException("");
        }
        VisionScreeningResult visionScreeningResult = getScreeningResult(screeningPlan, screeningResultBasicData.getStudentId());
        TwoTuple<VisionScreeningResult, ScreeningPlan> visionScreeningResultScreeningPlanTwoTuple = new TwoTuple<>(visionScreeningResult, screeningPlan);
        return visionScreeningResultScreeningPlanTwoTuple;
    }


    public VisionScreeningResult getScreeningResult(ScreeningPlan screeningPlan, Integer studentId) throws IOException {
        return this.getScreeningResult(screeningPlan.getId(), screeningPlan.getScreeningOrgId(), studentId);
    }

    /**
     * 是否需要更新
     *
     * @param planId
     * @param screeningOrgId
     * @return
     */
    public VisionScreeningResult getScreeningResult(Integer planId, Integer screeningOrgId, Integer studentId) throws IOException {
        VisionScreeningResult visionScreeningResultQuery = new VisionScreeningResult().setPlanId(planId).setStudentId(studentId).setScreeningOrgId(screeningOrgId);
        QueryWrapper<VisionScreeningResult> queryWrapper = getQueryWrapper(visionScreeningResultQuery);
        VisionScreeningResult visionScreeningResult = getOne(queryWrapper);
        return visionScreeningResult;
    }

    /**
     * 获取筛查数据
     *
     * @param screeningResultBasicData
     * @return
     * @throws IOException
     */
    public VisionScreeningResult getScreeningResult(ScreeningResultBasicData screeningResultBasicData) throws IOException {
        TwoTuple<VisionScreeningResult, ScreeningPlan> screeningResultAndScreeningPlan = getScreeningResultAndScreeningPlan(screeningResultBasicData);
        VisionScreeningResult screeningResult = screeningResultAndScreeningPlan.getFirst();
        ScreeningPlan screeningPlan = screeningResultAndScreeningPlan.getSecond();
        return new ScreeningResultBuilder().setVisionScreeningResult(screeningResult).setScreeningResultBasicData(screeningResultBasicData).setScreeningPlan(screeningPlan).build();
    }
}
