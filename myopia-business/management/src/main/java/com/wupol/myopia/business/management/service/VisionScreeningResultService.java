package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.business.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.builder.ScreeningResultBuilder;
import com.wupol.myopia.business.management.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.management.domain.mapper.VisionScreeningResultMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.domain.vo.StudentScreeningCountVO;
import com.wupol.myopia.business.management.util.TwoTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class VisionScreeningResultService extends BaseService<VisionScreeningResultMapper, VisionScreeningResult> {

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    /**
     * 通过StudentId获取筛查结果
     *
     * @param studentId id
     * @return List<ScreeningResult>
     */
    public List<VisionScreeningResult> getByStudentId(Integer studentId) {
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

    /**
     * 获取学校ID
     *
     * @param taskId 通知任务
     * @param orgId  机构ID
     * @return 学校ID
     */
    public List<Integer> getSchoolIdByTaskId(Integer taskId, Integer orgId) {
        return baseMapper.getSchoolIdByTaskId(taskId, orgId);
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

    /**
     * 获取学生筛查次数
     *
     * @return List<StudentScreeningCountVO>
     */
    public List<StudentScreeningCountVO> countScreeningTime() {
        return baseMapper.countScreeningTime();
    }

    /**
     * 根据筛查计划关联的存档的学生id
     *
     * @param screeningPlanSchoolStudentIds
     * @return
     */
    public List<VisionScreeningResult> getByScreeningPlanSchoolStudentIds(Set<Integer> screeningPlanSchoolStudentIds) {
        LambdaQueryWrapper<VisionScreeningResult> visionScreeningResultLambdaQueryWrapper = new LambdaQueryWrapper<>();
        visionScreeningResultLambdaQueryWrapper.in(VisionScreeningResult::getId, screeningPlanSchoolStudentIds);
        return baseMapper.selectList(visionScreeningResultLambdaQueryWrapper);
    }

    /**
     * 获取学生的最新筛查报告
     *
     * @param studentId 学生ID
     * @return VisionScreeningResult
     */
    public VisionScreeningResult getLatestResultByStudentId(Integer studentId) {
        return baseMapper.getLatestResultByStudentId(studentId);
    }

    public List<VisionScreeningResult> getByTaskId(Integer taskId) {
        return baseMapper.selectList(new QueryWrapper<VisionScreeningResult>()
                .eq("task_id", taskId)
                .orderByDesc("create_time"));
    }

/*
    *//**
     * 获取已有的筛查结果
     *
     * @param screeningResultBasicData
     * @return
     * @throws IOException
     *//*
    public TwoTuple<VisionScreeningResult, ScreeningPlanSchoolStudent> getScreeningResultAndScreeningPlanSchoolStudent(ScreeningResultBasicData screeningResultBasicData) throws IOException {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudentQuery = new ScreeningPlanSchoolStudent().setScreeningOrgId(screeningResultBasicData.getDeptId()).setId(screeningResultBasicData.getStudentId());

        //倒叙取出来最新的一条
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(screeningPlanSchoolStudentQuery);
        if (screeningPlanSchoolStudent == null) {
            throw new ManagementUncheckedException("无法找到screeningPlanSchoolStudent");
        }
        // 获取已经存在的数据
        VisionScreeningResult visionScreeningResult = getScreeningResultD(screeningPlanSchoolStudent.getScreeningPlanId(), screeningPlanSchoolStudent.getScreeningOrgId(), screeningResultBasicData.getStudentId(),screeningResultBasicData.getIsState());
        TwoTuple<VisionScreeningResult, ScreeningPlanSchoolStudent> visionScreeningResultScreeningPlanSchoolStudentTwoTuple = new TwoTuple<>(visionScreeningResult, screeningPlanSchoolStudent);
        return visionScreeningResultScreeningPlanSchoolStudentTwoTuple;
    }*/

    /**
     * 是否需要更新
     *
     * @param isState
     * @param screeningPlanId
     * @param planId
     * @param screeningOrgId
     * @return
     */
    public List<VisionScreeningResult> getScreeningResult(@NotNull(message = "筛查计划ID不能为空") Integer screeningPlanId, Integer planId, Integer screeningOrgId, Integer screeningPlanSchoolStudentId) throws IOException {
        VisionScreeningResult visionScreeningResultQuery = new VisionScreeningResult().setPlanId(planId).setScreeningPlanSchoolStudentId(screeningPlanSchoolStudentId).setScreeningOrgId(screeningOrgId);
        QueryWrapper<VisionScreeningResult> queryWrapper = getQueryWrapper(visionScreeningResultQuery);
        List<VisionScreeningResult> visionScreeningResults = list(queryWrapper);
        return visionScreeningResults;
    }

    /**
     * 获取筛查数据
     *
     * @param screeningResultBasicData
     * @return
     * @throws IOException
     */
    public VisionScreeningResult getScreeningResult(ScreeningResultBasicData screeningResultBasicData,VisionScreeningResult visionScreeningResult ,ScreeningPlanSchoolStudent screeningPlanSchoolStudent) throws IOException {
        //获取VisionScreeningResult以及ScreeningPlanSchoolStudent
       // TwoTuple<VisionScreeningResult, ScreeningPlanSchoolStudent> screeningResultAndScreeningPlanSchoolStudent = getScreeningResultAndScreeningPlanSchoolStudent(screeningResultBasicData);
        //获取ScreeningPlanSchoolStudent
        //ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningResultAndScreeningPlanSchoolStudent.getSecond();
        //构建ScreeningResult
        return new ScreeningResultBuilder().setVisionScreeningResult(visionScreeningResult).setIsDoubleScreen(screeningResultBasicData.getIsState() == 1).setScreeningResultBasicData(screeningResultBasicData).setScreeningPlanSchoolStudent(screeningPlanSchoolStudent).build();
    }

    /**
     * 保存并更新数据
     *
     * @param screeningResultBasicData
     * @return
     * @throws IOException
     */
    public VisionScreeningResult saveOrUpdateStudentScreenData(VisionScreeningResult visionScreeningResult)   {
        if (visionScreeningResult.getId() != null) {
            //更新
            updateById(visionScreeningResult);
        } else {
            //创建
            save(visionScreeningResult);
        }
        return visionScreeningResult;
    }

    public  TwoTuple<VisionScreeningResult, VisionScreeningResult> getAllFirstAndSecondResult(ScreeningResultBasicData screeningResultBasicData) throws IOException {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudentQuery = new ScreeningPlanSchoolStudent().setScreeningOrgId(screeningResultBasicData.getDeptId()).setId(screeningResultBasicData.getStudentId());
        //倒叙取出来最新的一条
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(screeningPlanSchoolStudentQuery);
        if (screeningPlanSchoolStudent == null) {
            throw new ManagementUncheckedException("无法找到screeningPlanSchoolStudent");
        }
        // 获取已经存在的数据
        List<VisionScreeningResult> visionScreeningResults = getScreeningResult(screeningPlanSchoolStudent.getScreeningPlanId(), screeningPlanSchoolStudent.getScreeningOrgId(), screeningResultBasicData.getStudentId(), screeningResultBasicData.getIsState());
        VisionScreeningResult currentVisionScreeningResult=null;
        VisionScreeningResult anotherVisionScreeningResult=null;
        for (VisionScreeningResult visionScreeningResult : visionScreeningResults) {
            if (visionScreeningResult.getIsDoubleScreen() == (screeningResultBasicData.getIsState() == 1)) {
                currentVisionScreeningResult = visionScreeningResult;
            } else {
                anotherVisionScreeningResult = visionScreeningResult;
            }
        }
        TwoTuple<VisionScreeningResult, VisionScreeningResult> visionScreeningResultVisionScreeningResultTwoTuple = new TwoTuple<>();
        return visionScreeningResultVisionScreeningResultTwoTuple;

    }
}
