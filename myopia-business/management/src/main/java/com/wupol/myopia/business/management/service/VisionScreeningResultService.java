package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.VisionScreeningResultMapper;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.domain.vo.StudentScreeningCountVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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
            public VisionScreeningResult getByPlanId (Integer planId){
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
