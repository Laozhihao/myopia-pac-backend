package com.wupol.myopia.business.core.screening.flow.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentCommonDiseaseIdService;
import com.wupol.myopia.business.core.school.service.StudentService;
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
    @Resource
    private StudentService studentService;

    /**
     * 通过StudentId获取筛查结果
     *
     * @param studentId id
     * @return List<ScreeningResult>
     */
    public List<VisionScreeningResult> getReleasePlanResultByStudentId(Integer studentId) {
        return baseMapper.getReleasePlanResultByStudentId(studentId);
    }

    /**
     * 通过StudentId获取筛查结果
     *
     * @param studentId id
     * @param needFilterAbolishPlan 是否需要过滤作废的计划
     * @return IPage<VisionScreeningResultDTO>
     */
    public IPage<VisionScreeningResultDTO> getByStudentIdWithPage(PageRequest pageRequest, Integer studentId,Integer schoolId, boolean needFilterAbolishPlan) {
        return baseMapper.getByStudentIdWithPage(pageRequest.toPage(), studentId,schoolId, needFilterAbolishPlan);
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
    public List<StudentScreeningCountDTO> countScreeningTime(List<Integer> studentIds) {
        return baseMapper.countScreeningTime(studentIds);
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
    public List<Integer> getReleasePlanIdsByDate(String dateStr) {
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
        return getByPlanIds(planIds,null);
    }

    public List<VisionScreeningResult> getByPlanIds(List<Integer> planIds,Boolean isDoubleScreen) {
        if (CollUtil.isEmpty(planIds)){
            return Lists.newArrayList();
        }
        return baseMapper.selectList(Wrappers.lambdaQuery(VisionScreeningResult.class)
                .in(VisionScreeningResult::getPlanId,planIds)
                .eq(Objects.nonNull(isDoubleScreen),VisionScreeningResult::getIsDoubleScreen,isDoubleScreen));
    }

    public List<VisionScreeningResult> getByPlanIdsAndIsDoubleScreenAndDistrictIds(List<Integer> planIds,Boolean isDoubleScreen,List<Integer> districtIdList,Integer schoolId) {
        return baseMapper.selectList(Wrappers.lambdaQuery(VisionScreeningResult.class)
                .in(VisionScreeningResult::getPlanId,planIds)
                .in(CollUtil.isNotEmpty(districtIdList),VisionScreeningResult::getDistrictId,districtIdList)
                .eq(Objects.nonNull(schoolId),VisionScreeningResult::getSchoolId,schoolId)
                .eq(VisionScreeningResult::getIsDoubleScreen,isDoubleScreen));
    }


    /**
     * 获取学生的最新筛查报告
     *
     * @param studentId 学生ID
     * @return VisionScreeningResult
     */
    public VisionScreeningResult getLatestResultOfReleasePlanByStudentId(Integer studentId) {
        return baseMapper.getLatestResultOfReleasePlanByStudentId(studentId);
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
        return baseMapper.selectList(
                Wrappers.lambdaQuery(VisionScreeningResult.class)
                .eq(VisionScreeningResult::getPlanId, planId));
    }

    public List<VisionScreeningResult> getByPlanIdAndIsDoubleScreenBatch(List<Integer> planIds,Boolean isDoubleScreen,Integer schoolId) {
        return baseMapper.selectList(Wrappers.lambdaQuery(VisionScreeningResult.class)
                .eq(VisionScreeningResult::getIsDoubleScreen,isDoubleScreen)
                .in(VisionScreeningResult::getPlanId,planIds)
                .eq(Objects.nonNull(schoolId),VisionScreeningResult::getSchoolId,schoolId));
    }

    public List<VisionScreeningResult> getByPlanIdAndIsDoubleScreen(Integer planId,Boolean isDoubleScreen,Integer schoolId) {
        return baseMapper.selectList(Wrappers.lambdaQuery(VisionScreeningResult.class)
                .eq(VisionScreeningResult::getIsDoubleScreen,isDoubleScreen)
                .eq(VisionScreeningResult::getPlanId,planId)
                .eq(Objects.nonNull(schoolId),VisionScreeningResult::getSchoolId,schoolId));
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
        if (ScreeningTypeEnum.isCommonDiseaseScreeningType(plan.getScreeningType())) {
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

        // 更新多端学生
        updateManagementStudent(planStudents);
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
     * 根据筛查任务ID统计每个计划下筛查中的学校数量 TODO
     *
     * @param taskId
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.dos.ScreeningSchoolCount>
     **/
    public List<ScreeningSchoolCount> countScreeningSchoolByTaskId(Integer taskId) {
        Assert.notNull(taskId, "筛查任务ID不能为空");
        return baseMapper.countScreeningSchoolByTaskId(taskId);
    }

    /**
     * 根据ID集获取，并根据创建时间倒序
     *
     * @param ids
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult>
     **/
    public List<VisionScreeningResult> getByIdsAndCreateTimeDesc(List<Integer> ids) {
        return baseMapper.getByIdsAndCreateTimeDesc(ids);
    }

    /**
     * 获取筛查区域
     *
     * @param districtIds 行政区域ID集合
     * @param taskIds 任务ID集合
     */
    public int selectScreeningResultByDistrictIdAndTaskId(List<Integer> districtIds, List<Integer> taskIds) {
        return baseMapper.selectScreeningResultByDistrictIdAndTaskId(districtIds,taskIds);
    }

    /**
     * 获取学生筛查次数
     *
     * @return List<StudentScreeningCountVO>
     */
    public List<StudentScreeningCountDTO> getVisionScreeningCountBySchoolId(Integer schoolId) {
        return baseMapper.getVisionScreeningCountBySchoolId(schoolId);
    }

    /**
     * 根据条件查询筛查结果
     * @param schoolIds 学校ID集合
     * @param screeningPlanId 筛查计划ID
     * @param screeningOrgId 筛查jigouID
     * @param isDoubleScreen 是否复查
     */
    public List<VisionScreeningResult> listByCondition(List<Integer> schoolIds, Integer screeningPlanId, Integer screeningOrgId, Boolean isDoubleScreen) {
        LambdaQueryWrapper<VisionScreeningResult> queryWrapper = Wrappers.lambdaQuery(VisionScreeningResult.class)
                .eq(VisionScreeningResult::getPlanId, screeningPlanId)
                .eq(VisionScreeningResult::getScreeningOrgId, screeningOrgId)
                .in(VisionScreeningResult::getSchoolId, schoolIds)
                .eq(VisionScreeningResult::getIsDoubleScreen, isDoubleScreen)
                .select(VisionScreeningResult::getId, VisionScreeningResult::getSchoolId);
        return baseMapper.selectList(queryWrapper);

    }

    public List<VisionScreeningResult> getByPlanIdsAndSchoolId(List<Integer> screeningPlanIds, Integer schoolId, Boolean isDoubleScreen) {
        return baseMapper.selectList(Wrappers.lambdaQuery(VisionScreeningResult.class)
                .in(VisionScreeningResult::getPlanId,screeningPlanIds)
                .eq(VisionScreeningResult::getSchoolId,schoolId)
                .eq(VisionScreeningResult::getIsDoubleScreen,isDoubleScreen));
    }

    /**
     * 获取学生筛查结果和结论信息
     *
     * @param studentIds 学生Id
     *
     * @return TwoTuple<Map < Integer, VisionScreeningResult>, Map<Integer, StatConclusion>>
     */
    public TwoTuple<Map<Integer, VisionScreeningResult>, Map<Integer, StatConclusion>> getStudentResultAndStatMap(List<Integer> studentIds) {

        if (CollectionUtils.isEmpty(studentIds)) {
            return new TwoTuple<>(new HashMap<>(), new HashMap<>());
        }
        // 结果表
        List<VisionScreeningResult> resultList = getByStudentIds(studentIds);
        Map<Integer, VisionScreeningResult> resultMap = resultList.stream().collect(Collectors.toMap(VisionScreeningResult::getStudentId,
                Function.identity(),
                (v1, v2) -> v1.getCreateTime().after(v2.getCreateTime()) ? v1 : v2));

        if (CollectionUtils.isEmpty(resultList)) {
            return new TwoTuple<>(new HashMap<>(), new HashMap<>());
        }
        // 结论表
        List<StatConclusion> statConclusions = statConclusionService.getByResultIds(resultList.stream().map(VisionScreeningResult::getId).collect(Collectors.toList()));
        Map<Integer, StatConclusion> statConclusionMap = statConclusions.stream().collect(Collectors.toMap(StatConclusion::getStudentId,
                Function.identity(),
                (v1, v2) -> v1.getCreateTime().after(v2.getCreateTime()) ? v1 : v2));

        return new TwoTuple<>(resultMap, statConclusionMap);
    }

    /**
     * 更新多端学生
     *
     * @param planStudents 计划学生
     */
    private void updateManagementStudent(List<ScreeningPlanSchoolStudent> planStudents) {
        Map<Integer, ScreeningPlanSchoolStudent> planStudentMap = planStudents.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getStudentId, Function.identity()));
        List<Integer> studentIds = planStudents.stream().map(ScreeningPlanSchoolStudent::getStudentId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(studentIds)) {
            return;
        }
        List<Student> students = studentService.getByIds(studentIds);
        studentService.updateBatchById(students.stream().map(s -> updateManagementStudent(planStudentMap.getOrDefault(s.getId(), new ScreeningPlanSchoolStudent()), s)).collect(Collectors.toList()));
    }

    /**
     * 更新多端学生
     *
     * @param planStudent 计划学生
     * @param student     多端学生
     *
     * @return 多端学生
     */
    private Student updateManagementStudent(ScreeningPlanSchoolStudent planStudent, Student student) {
        student.setName(planStudent.getStudentName());
        student.setGender(planStudent.getGender());
        student.setGradeType(planStudent.getStudentAge());
        student.setBirthday(planStudent.getBirthday());
        student.setSchoolId(planStudent.getSchoolId());
        student.setClassId(planStudent.getClassId());
        student.setGradeId(planStudent.getGradeId());
        student.setParentPhone(planStudent.getParentPhone());
        student.setSno(planStudent.getStudentNo());
        student.setNation(planStudent.getNation());
        return student;
    }

    /**
     * 获取学生筛查次数
     *
     * @return List<StudentScreeningCountVO>
     */
    public Map<Integer, Integer> countScreeningTimeMap(List<Integer> studentIds) {
        return countScreeningTime(studentIds).stream().collect(Collectors.toMap(
                StudentScreeningCountDTO::getStudentId, StudentScreeningCountDTO::getCount));
    }

    /**
     * 通过筛查学生查询筛查结果
     *
     * @param studentIds 筛查学生
     * @return 筛查结果
     */
    public Map<Integer, VisionScreeningResult> getLastByStudentIds(List<Integer> studentIds, Integer schoolId) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return new HashMap<>();
        }
        List<VisionScreeningResult> resultList = getByStudentIds(studentIds);
        return resultList.stream()
                .filter(s -> Objects.equals(s.getScreeningType(), ScreeningTypeEnum.VISION.getType()))
                .filter(s-> Objects.equals(s.getSchoolId(), schoolId))
                .collect(Collectors.toMap(VisionScreeningResult::getStudentId,
                Function.identity(),
                (v1, v2) -> v1.getCreateTime().after(v2.getCreateTime()) ? v1 : v2));
    }
}
