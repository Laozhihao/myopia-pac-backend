package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.business.management.domain.builder.ScreeningResultBuilder;
import com.wupol.myopia.business.management.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.management.domain.mapper.VisionScreeningResultMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.domain.vo.StudentScreeningCountVO;
import com.wupol.myopia.business.management.util.TwoTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
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
        return baseMapper.getByStudentId(studentId);
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
     * @param planId 计划od
     * @param orgId  机构ID
     * @return UserId
     */
    public List<Integer> getCreateUserIdByPlanId(Integer planId, Integer orgId) {
        return baseMapper.getCreateUserIdByPlanIdAndOrgId(planId, orgId);
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
     * 获取昨天筛查数据的筛查计划Id
     *
     * @return
     */
    public List<Integer> getYesterdayScreeningPlanIds() {
//        Date yesterdayStartTime = DateUtil.getYesterdayStartTime();
//        Date yesterdayEndTime = DateUtil.getYesterdayEndTime();
//        return baseMapper.getPlanIdsByTime(yesterdayStartTime, yesterdayEndTime);
        return baseMapper.getPlanIdsByTime(new Date(), new Date());
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
     */
    public ScreeningPlanSchoolStudent getScreeningResultAndScreeningPlanSchoolStudent(ScreeningResultBasicData screeningResultBasicData) throws IOException {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudentQuery = new ScreeningPlanSchoolStudent().setScreeningOrgId(screeningResultBasicData.getDeptId()).setId(screeningResultBasicData.getStudentId());
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(screeningPlanSchoolStudentQuery);
        if (screeningPlanSchoolStudent == null) {
            throw new ManagementUncheckedException("无法找到screeningPlanSchoolStudent");
        }
        // 获取已经存在的数据
        return  screeningPlanSchoolStudent;
    }

    /**
     * 是否需要更新
     *
     * @param planId
     * @param screeningOrgId
     * @return
     */
    public List<VisionScreeningResult> getScreeningResult(Integer planId, Integer screeningOrgId, Integer screeningPlanSchoolStudentId) throws IOException {
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
    public VisionScreeningResult getScreeningResult(ScreeningResultBasicData screeningResultBasicData, VisionScreeningResult visionScreeningResult  ) throws IOException {
        //获取VisionScreeningResult以及ScreeningPlanSchoolStudent
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = getScreeningResultAndScreeningPlanSchoolStudent(screeningResultBasicData);
        //构建ScreeningResult
        return new ScreeningResultBuilder().setVisionScreeningResult(visionScreeningResult).setIsDoubleScreen(screeningResultBasicData.getIsState() == 1).setScreeningResultBasicData(screeningResultBasicData).setScreeningPlanSchoolStudent(screeningPlanSchoolStudent).build();
    }

    /**
     * 保存并更新数据
     *
     * @param visionScreeningResult
     * @return
     * @throws IOException
     */
    public VisionScreeningResult saveOrUpdateStudentScreenData(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null && visionScreeningResult.getId() != null) {
            //更新
            updateById(visionScreeningResult);
        } else {
            //创建
            save(visionScreeningResult);
        }
        return visionScreeningResult;
    }

    /**
     * 获取筛查结果
     *
     * @param schoolId 学校ID
     * @param orgId    机构ID
     * @param planId   计划ID
     * @return List<VisionScreeningResult> 筛查结果
     */
    public List<VisionScreeningResult> getBySchoolIdAndOrgIdAndPlanId(Integer schoolId, Integer orgId, Integer planId) {
        return baseMapper.getBySchoolIdAndOrgIdAndPlanId(schoolId, orgId, planId);
    }

    public TwoTuple<VisionScreeningResult, VisionScreeningResult> getAllFirstAndSecondResult(ScreeningResultBasicData screeningResultBasicData) throws IOException {
         ScreeningPlanSchoolStudent screeningPlanSchoolStudentQuery = new ScreeningPlanSchoolStudent().setScreeningOrgId(screeningResultBasicData.getDeptId()).setId(screeningResultBasicData.getStudentId());
        //倒叙取出来最新的一条
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(screeningPlanSchoolStudentQuery);
        if (screeningPlanSchoolStudent == null) {
            throw new ManagementUncheckedException("无法找到screeningPlanSchoolStudent");
        }
        // 获取已经存在的数据
        List<VisionScreeningResult> visionScreeningResults = getScreeningResult(screeningPlanSchoolStudent.getScreeningPlanId(), screeningPlanSchoolStudent.getScreeningOrgId(), screeningResultBasicData.getStudentId());
        VisionScreeningResult currentVisionScreeningResult = null;
        VisionScreeningResult anotherVisionScreeningResult = null;
        for (VisionScreeningResult visionScreeningResult : visionScreeningResults) {
            if (visionScreeningResult.getIsDoubleScreen() == (screeningResultBasicData.getIsState() == 1)) {
                currentVisionScreeningResult = visionScreeningResult;
            } else {
                anotherVisionScreeningResult = visionScreeningResult;
            }
        }
        TwoTuple<VisionScreeningResult, VisionScreeningResult> visionScreeningResultVisionScreeningResultTwoTuple = new TwoTuple<>();
        visionScreeningResultVisionScreeningResultTwoTuple.setFirst(currentVisionScreeningResult);
        visionScreeningResultVisionScreeningResultTwoTuple.setSecond(anotherVisionScreeningResult);
        return visionScreeningResultVisionScreeningResultTwoTuple;

    }

    /**
     * 统计纳入统计的实际筛查数
     *
     * @param schoolId 学校ID
     * @param planId   计划ID
     * @return 统计个数
     */
    public Integer countIncludeScreeningNumbers(Integer schoolId, Integer planId) {
        return baseMapper.countIncludeScreeningNumbers(schoolId, planId);
    }
}
