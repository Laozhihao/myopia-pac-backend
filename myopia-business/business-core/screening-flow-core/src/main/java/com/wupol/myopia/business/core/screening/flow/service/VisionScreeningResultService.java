package com.wupol.myopia.business.core.screening.flow.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.service.StudentCommonDiseaseIdService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.VisionScreeningResultMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import com.wupol.myopia.business.core.screening.flow.util.ReScreenCardUtil;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class VisionScreeningResultService extends BaseService<VisionScreeningResultMapper, VisionScreeningResult> {

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Resource
    private StatConclusionService statConclusionService;
    @Autowired
    private StudentCommonDiseaseIdService studentCommonDiseaseIdService;

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
     * 通过StudentId获取筛查结果
     *
     * @param studentId id
     * @return List<ScreeningResult>
     */
    public IPage<VisionScreeningResult> getByStudentIdWithPage(PageRequest pageRequest, Integer studentId) {
        return baseMapper.getByStudentIdWithPage(pageRequest.toPage(), studentId);
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
    public List<StudentScreeningCountDTO> countScreeningTime() {
        return baseMapper.countScreeningTime();
    }

    /**
     * 获取昨天筛查数据的筛查计划Id（必须有筛查通知，也就是省级配置的筛查机构筛查的数据）
     *
     * @return 筛查计划Id
     */
    public List<Integer> getYesterdayScreeningPlanIds() {
        Date yesterdayStartTime = DateUtil.getYesterdayStartTime();
        Date yesterdayEndTime = DateUtil.getYesterdayEndTime();
        return baseMapper.getHaveSrcScreeningNoticePlanIdsByTime(yesterdayStartTime, yesterdayEndTime);
    }

    /**
     * 通过指定的日期获取筛查计划ID集合
     */
    public List<Integer> getScreeningPlanIdsByDate(String dateStr) {
        if (StrUtil.isBlank(dateStr)) {
            dateStr = LocalDate.now().minusDays(1).toString();

        }
        LocalDateTime startTime = LocalDate.parse(dateStr).atTime(0, 0, 0, 0);
        LocalDateTime endTime = LocalDate.parse(dateStr).atTime(23, 59, 59, 999);
        return baseMapper.getHaveSrcScreeningNoticePlanIdsByTime(DateUtil.toDate(startTime), DateUtil.toDate(endTime));
    }

    /**
     * 根据筛查计划关联的存档的学生id
     *
     * @param screeningPlanSchoolStudentIds 计划的学生ID
     * @return List<VisionScreeningResult>
     */
    public List<VisionScreeningResult> getByScreeningPlanSchoolStudentIds(Set<Integer> screeningPlanSchoolStudentIds, boolean isDoubleScreen) {
        LambdaQueryWrapper<VisionScreeningResult> visionScreeningResultLambdaQueryWrapper = new LambdaQueryWrapper<>();
        visionScreeningResultLambdaQueryWrapper.eq(VisionScreeningResult::getIsDoubleScreen, isDoubleScreen).in(VisionScreeningResult::getScreeningPlanSchoolStudentId, screeningPlanSchoolStudentIds);
        return baseMapper.selectList(visionScreeningResultLambdaQueryWrapper);
    }

    /**
     * 根据筛查计划关联的存档的学生id
     *
     * @param screeningPlanSchoolStudentIds 计划的学生ID
     * @return List<VisionScreeningResult>
     */
    public List<VisionScreeningResult> getByScreeningPlanSchoolStudentIds(Set<Integer> screeningPlanSchoolStudentIds) {
        LambdaQueryWrapper<VisionScreeningResult> visionScreeningResultLambdaQueryWrapper = new LambdaQueryWrapper<>();
        visionScreeningResultLambdaQueryWrapper.eq(VisionScreeningResult::getIsDoubleScreen, false).in(VisionScreeningResult::getScreeningPlanSchoolStudentId, screeningPlanSchoolStudentIds);
        return baseMapper.selectList(visionScreeningResultLambdaQueryWrapper);
    }

    /**
     * 根据筛查计划ID集查询
     *
     * @param planIds 计划的学生ID
     * @return List<VisionScreeningResult>
     */
    public List<VisionScreeningResult> getByPlanIdsOrderByUpdateTimeDesc(Set<Integer> planIds) {
        LambdaQueryWrapper<VisionScreeningResult> visionScreeningResultLambdaQueryWrapper = new LambdaQueryWrapper<>();
        visionScreeningResultLambdaQueryWrapper.eq(VisionScreeningResult::getIsDoubleScreen, false).in(VisionScreeningResult::getPlanId, planIds).orderByDesc(VisionScreeningResult::getUpdateTime);
        return baseMapper.selectList(visionScreeningResultLambdaQueryWrapper);
    }

    /**
     * 根据筛查计划ID集查询
     *
     * @param planIds
     */
    public List<VisionScreeningResult> getByPlanIds(List<Integer> planIds) {
        LambdaQueryWrapper<VisionScreeningResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(VisionScreeningResult::getPlanId, planIds);
        return baseMapper.selectList(queryWrapper);
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

    /**
     * 是否需要更新
     *
     * @param planId         计划ID
     * @param screeningOrgId 筛查机构ID
     * @return List<VisionScreeningResult>
     */
    public List<VisionScreeningResult> getScreeningResult(Integer planId, Integer screeningOrgId, Integer screeningPlanSchoolStudentId) {
        VisionScreeningResult visionScreeningResultQuery = new VisionScreeningResult().setPlanId(planId).setScreeningPlanSchoolStudentId(screeningPlanSchoolStudentId).setScreeningOrgId(screeningOrgId);
        QueryWrapper<VisionScreeningResult> queryWrapper = getQueryWrapper(visionScreeningResultQuery);
        return list(queryWrapper);
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

    /**
     * 获取学生的筛查记录
     *
     * @return 学生筛查记录
     */
    public List<VisionScreeningResult> getStudentResults() {
        return baseMapper.getStudentResults();
    }

    /**
     * 通过筛查学生查询筛查结果
     *
     * @param planStudentId 筛查学生
     * @return 筛查结果
     */
    public VisionScreeningResult getByPlanStudentId(Integer planStudentId) {
        return baseMapper.getByPlanStudentId(planStudentId);
    }

    /**
     * 通过筛查学生查询筛查结果
     *
     * @param planStudentIds 筛查学生
     * @return 筛查结果
     */
    public List<VisionScreeningResult> getByPlanStudentIds(List<Integer> planStudentIds) {
        if (CollectionUtils.isEmpty(planStudentIds)) {
            return Collections.emptyList();
        }
        return baseMapper.getByPlanStudentIds(planStudentIds);
    }

    public List<Integer> getBySchoolIdPlanId(Integer planId) {
        return baseMapper.getBySchoolIdPlanId(planId);
    }

    /**
     * 通过学校Id和计划Id获取筛查学生Id
     *
     * @param planId   计划Id
     * @param schoolId 学校Id
     * @return List<Integer>
     */
    public List<Integer> getByPlanStudentIdPlanIdAndSchoolId(Integer planId, Integer schoolId) {
        return baseMapper.getByPlanIdAndSchoolId(planId, schoolId).stream().map(VisionScreeningResult::getScreeningPlanSchoolStudentId).collect(Collectors.toList());
    }

    /**
     * 通过学校Id和计划Id获取信息
     *
     * @param planId   计划Id
     * @param schoolId 学校Id
     * @return List<Integer>
     */
    public List<VisionScreeningResult> getByPlanIdAndSchoolId(Integer planId, Integer schoolId) {
        return baseMapper.getByPlanIdAndSchoolId(planId, schoolId);
    }


    /**
     * 通过筛查学生查询最新筛查结果
     *
     * @param planStudentIds 筛查学生
     * @return 筛查结果
     */
    public VisionScreeningResult getLatestByPlanStudentIds(List<Integer> planStudentIds) {
        return baseMapper.getLatestByPlanStudentIds(planStudentIds);
    }

    public List<VisionScreeningResult> getByPlanId(Integer planId) {
        return baseMapper.getByPlanId(planId);
    }

    /**
     * 通过学生Id获取结果
     *
     * @param studentIds 学生Ids
     * @return List<VisionScreeningResult>
     */
    public List<VisionScreeningResult> getByStudentIds(List<Integer> studentIds) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return new ArrayList<>();
        }
        return baseMapper.getByStudentIds(studentIds);
    }

    /**
     * 更新学生计划结果
     *
     * @param plan         计划
     * @param planStudents 筛查学生
     */
    public void updatePlanStudentAndVisionResult(ScreeningPlan plan, List<ScreeningPlanSchoolStudent> planStudents) {
        if (CollectionUtils.isEmpty(planStudents)) {
            return;
        }
        // 设置常见病ID
        if (!ScreeningTypeEnum.isVisionScreeningType(plan.getScreeningType())) {
            planStudents.forEach(x -> x.setCommonDiseaseId(studentCommonDiseaseIdService.getStudentCommonDiseaseId(x.getSchoolDistrictId(), x.getSchoolId(), x.getGradeId(), x.getStudentId(), plan.getStartTime())));
        }
        // 新增或更新筛查计划学生
        screeningPlanSchoolStudentService.saveOrUpdateBatch(planStudents);
        // 获取所有结果
        Integer planId = plan.getId();
        List<VisionScreeningResult> resultList = getByPlanId(planId);
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }

        // 获取所有的筛查数据结论
        StatConclusionQueryDTO statConclusionQueryDTO = new StatConclusionQueryDTO();
        statConclusionQueryDTO.setPlanId(planId);
        List<StatConclusion> statConclusionList = statConclusionService.listByQuery(statConclusionQueryDTO);
        if (CollectionUtils.isEmpty(statConclusionList)) {
            return;
        }
        Map<Integer, StatConclusion> statConclusionMap = statConclusionList.stream().collect(Collectors.toMap(StatConclusion::getResultId, Function.identity()));

        // 更新学生筛查数据的 studentId、schoolId
        List<VisionScreeningResult> updateResultList = new ArrayList<>();
        List<StatConclusion> updateStatConclusionList = new ArrayList<>();

        Map<Integer, List<VisionScreeningResult>> visionMap = resultList.stream().collect(Collectors.groupingBy(VisionScreeningResult::getScreeningPlanSchoolStudentId));
        planStudents.forEach(planStudent -> {
            List<VisionScreeningResult> results = visionMap.get(planStudent.getId());
            if (!CollectionUtils.isEmpty(results)) {
                results.forEach(result -> {
                    if (Objects.nonNull(result)) {
                        result.setStudentId(planStudent.getStudentId());
                        result.setSchoolId(planStudent.getSchoolId());
                        updateResultList.add(result);
                        StatConclusion statConclusion = statConclusionMap.get(result.getId());
                        if (Objects.nonNull(statConclusion)) {
                            statConclusion.setScreeningPlanSchoolStudentId(planStudent.getId());
                            statConclusion.setStudentId(planStudent.getStudentId());
                            statConclusion.setSchoolId(planStudent.getSchoolId());
                            updateStatConclusionList.add(statConclusion);
                        }
                    }
                });
            }
        });
        updateBatchById(updateResultList);
        statConclusionService.updateBatchById(updateStatConclusionList);
    }

    /**
     * 通过计划id，学校id获取复查学生数据
     *
     * @param planId    计划Id
     * @param schoolIds 学校Id
     * @return List<VisionScreeningResult>
     */
    public List<VisionScreeningResult> getRescreenBySchoolIds(Integer planId, List<Integer> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return new ArrayList<>();
        }
        return baseMapper.getRescreenBySchoolIds(planId, schoolIds);
    }

    /**
     * 通过筛查学生查询初筛筛查结果
     *
     * @param planStudentIds 筛查学生
     * @return 筛查结果
     */
    public List<VisionScreeningResult> getFirstByPlanStudentIds(List<Integer> planStudentIds) {
        if (CollectionUtils.isEmpty(planStudentIds)) {
            return Collections.emptyList();
        }
        return baseMapper.getFirstByPlanStudentIds(planStudentIds);
    }


    /**
     * 获取学生初筛/复测（默认初测）
     * @param planId 计划ID
     * @param screeningPlanSchoolStudentId 学生ID
     * @param isDoubleScreen false：初测  true：复测
     * @return
     */
    public VisionScreeningResult getOneScreeningResult(Integer planId, Integer screeningPlanSchoolStudentId, boolean isDoubleScreen) {
        return findOne(new VisionScreeningResult().setPlanId(planId).setScreeningPlanSchoolStudentId(screeningPlanSchoolStudentId).setIsDoubleScreen(isDoubleScreen));
    }

    /**
     * 获取学生筛查结果明细
     *
     * @param planId    筛查ID
     * @param planStudentId 计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dto.VisionScreeningResultDTO
     **/
    public VisionScreeningResultDTO getStudentScreeningResultDetail(Integer planId, Integer planStudentId){
        List<VisionScreeningResult> visionScreeningResultList = findByList(new VisionScreeningResult().setPlanId(planId).setScreeningPlanSchoolStudentId(planStudentId));
        VisionScreeningResult firstResult = visionScreeningResultList.stream().filter(x -> Boolean.FALSE.equals(x.getIsDoubleScreen())).findFirst().orElse(null);
        if (Objects.isNull(firstResult)) {
            return new VisionScreeningResultDTO();
        }

        VisionScreeningResultDTO visionScreeningResultDTO = new VisionScreeningResultDTO();
        BeanUtils.copyProperties(firstResult, visionScreeningResultDTO);

        visionScreeningResultDTO.setSaprodontiaStat(SaprodontiaStat.parseFromSaprodontiaDataDO(firstResult.getSaprodontiaData()))
                .setGender(screeningPlanSchoolStudentService.getById(firstResult.getScreeningPlanSchoolStudentId()).getGender())
                .setLeftSE(StatUtil.getSphericalEquivalent(EyeDataUtil.leftSph(firstResult), EyeDataUtil.leftCyl(firstResult)))
                .setRightSE(StatUtil.getSphericalEquivalent(EyeDataUtil.rightSph(firstResult),EyeDataUtil.rightCyl(firstResult)))
                .setSaprodontiaData(Optional.ofNullable(firstResult.getSaprodontiaData()).orElse(new SaprodontiaDataDO()))
                .setSpineData(Optional.ofNullable(firstResult.getSpineData()).orElse(new SpineDataDO()))
                .setBloodPressureData(Optional.ofNullable(firstResult.getBloodPressureData()).orElse(new BloodPressureDataDO()))
                .setDiseasesHistoryData(Optional.ofNullable(firstResult.getDiseasesHistoryData()).orElse(new DiseasesHistoryDO()))
                .setPrivacyData(Optional.ofNullable(firstResult.getPrivacyData()).orElse(new PrivacyDataDO()))
                .setDeviationData(Optional.ofNullable(firstResult.getDeviationData()).orElse(new DeviationDO()))
                .setOtherEyeDiseases(Optional.ofNullable(firstResult.getOtherEyeDiseases()).orElse(new OtherEyeDiseasesDO()));
        // 做完全部复测项目才会出现复测情况模块
        VisionScreeningResult reScreeningResult = visionScreeningResultList.stream().filter(x -> Boolean.TRUE.equals(x.getIsDoubleScreen())).findFirst().orElse(null);
        if(Objects.nonNull(reScreeningResult) && ObjectsUtil.allNotNull(reScreeningResult.getVisionData(), reScreeningResult.getComputerOptometry(), reScreeningResult.getHeightAndWeightData())) {
            visionScreeningResultDTO.setRescreening(Optional.ofNullable(ReScreenCardUtil.reScreeningResult(firstResult, reScreeningResult)).orElse(new ReScreenDTO()));
        }
        return visionScreeningResultDTO;
    }

    /**
     * 根据筛查任务ID统计每个计划下筛查中的学校数量
     *
     * @param taskId
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.dos.ScreeningSchoolCount>
     **/
    public List<ScreeningSchoolCount> countScreeningSchoolByTaskId(Integer taskId) {
        Assert.notNull(taskId, "筛查任务ID不能为空");
        return baseMapper.countScreeningSchoolByTaskId(taskId);
    }

    public List<VisionScreeningResult> getByIds(List<Integer> ids) {
        return baseMapper.getByIds(ids);
    }

    public int selectScreeningResultByDistrictIdAndTaskId(List<Integer> districtIds, List<Integer> taskIds) {
        return baseMapper.selectScreeningResultByDistrictIdAndTaskId(districtIds,taskIds);
    }
}
