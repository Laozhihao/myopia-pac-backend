package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.myopia.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.builder.ScreeningResultBuilder;
import com.wupol.myopia.business.management.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.management.domain.mapper.ScreeningResultMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
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
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

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
    public TwoTuple<VisionScreeningResult, ScreeningPlanSchoolStudent> getScreeningResultAndScreeningPlanSchoolStudent(ScreeningResultBasicData screeningResultBasicData) throws IOException {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudentQuery = new ScreeningPlanSchoolStudent().setScreeningPlanId(screeningResultBasicData.getDeptId()).setStudentId(screeningResultBasicData.getStudentId());
        //倒叙取出来最新的一条
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(screeningPlanSchoolStudentQuery);
        if (screeningPlanSchoolStudent == null) {
            throw new ManagementUncheckedException("无法找到screeningPlanSchoolStudent");
        }
        VisionScreeningResult visionScreeningResult = getScreeningResult(screeningPlanSchoolStudent, screeningResultBasicData.getStudentId());
        TwoTuple<VisionScreeningResult, ScreeningPlanSchoolStudent> visionScreeningResultScreeningPlanSchoolStudentTwoTuple = new TwoTuple<>(visionScreeningResult, screeningPlanSchoolStudent);
        return visionScreeningResultScreeningPlanSchoolStudentTwoTuple;
    }


    public VisionScreeningResult getScreeningResult(ScreeningPlanSchoolStudent screeningPlanSchoolStudent, Integer studentId) throws IOException {
        return this.getScreeningResult(screeningPlanSchoolStudent.getScreeningPlanId(), screeningPlanSchoolStudent.getScreeningOrgId(), studentId);
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
        TwoTuple<VisionScreeningResult, ScreeningPlanSchoolStudent> screeningResultAndScreeningPlanSchoolStudent = getScreeningResultAndScreeningPlanSchoolStudent(screeningResultBasicData);
        VisionScreeningResult visionScreeningResult = screeningResultAndScreeningPlanSchoolStudent.getFirst();
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningResultAndScreeningPlanSchoolStudent.getSecond();
        return new ScreeningResultBuilder().setVisionScreeningResult(visionScreeningResult).setScreeningResultBasicData(screeningResultBasicData).setScreeningPlanSchoolStudent(screeningPlanSchoolStudent).build();
    }
}
