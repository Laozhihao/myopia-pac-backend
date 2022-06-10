package com.wupol.myopia.business.core.screening.flow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.core.screening.flow.constant.SaprodontiaType;
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
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
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
     * 根据筛查计划关联的存档的学生id
     *
     * @param screeningPlanSchoolStudentIds 计划的学生ID
     * @return List<VisionScreeningResult>
     */
    public List<VisionScreeningResult> getByScreeningPlanSchoolStudentIds(Set<Integer> screeningPlanSchoolStudentIds) {
        LambdaQueryWrapper<VisionScreeningResult> visionScreeningResultLambdaQueryWrapper = new LambdaQueryWrapper<>();
        visionScreeningResultLambdaQueryWrapper.eq(VisionScreeningResult::getIsDoubleScreen,false).in(VisionScreeningResult::getScreeningPlanSchoolStudentId, screeningPlanSchoolStudentIds);
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
        visionScreeningResultLambdaQueryWrapper.eq(VisionScreeningResult::getIsDoubleScreen,false).in(VisionScreeningResult::getPlanId, planIds).orderByDesc(VisionScreeningResult::getUpdateTime);
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

    /**
     * 是否需要更新
     *
     * @param planId 计划ID
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
     * 通过学校Id和计划Id获取信息
     *
     * @param planId   计划Id
     * @param schoolId 学校Id
     * @return List<Integer>
     */
    public List<Integer> getByPlanIdAndSchoolId(Integer planId, Integer schoolId) {
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

        List<VisionScreeningResult> updateResultList = new ArrayList<>();
        List<StatConclusion> updateStatConclusionList = new ArrayList<>();

        Map<Integer, VisionScreeningResult> visionMap = resultList.stream()
                .collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));
        planStudents.forEach(planStudent -> {
            VisionScreeningResult result = visionMap.get(planStudent.getId());
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
        updateBatchById(updateResultList);
        statConclusionService.updateBatchById(updateStatConclusionList);
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
        VisionScreeningResult firstResult = visionScreeningResultList.stream().filter(x -> !x.getIsDoubleScreen()).findFirst().orElse(null);
        if (Objects.isNull(firstResult)) {
            return new VisionScreeningResultDTO();
        }

        VisionScreeningResultDTO visionScreeningResultDTO = new VisionScreeningResultDTO();
        BeanUtils.copyProperties(firstResult, visionScreeningResultDTO);
        // TODO：合并治豪分支后，复用其统计方法
        visionScreeningResultDTO.setSaprodontiaDataDTO(getSaprodontiaDataDTO(firstResult))
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
        VisionScreeningResult reScreeningResult = visionScreeningResultList.stream().filter(VisionScreeningResult::getIsDoubleScreen).findFirst().orElse(null);
        if(Objects.nonNull(reScreeningResult) && ObjectsUtil.allNotNull(reScreeningResult.getVisionData(), reScreeningResult.getComputerOptometry(), reScreeningResult.getHeightAndWeightData())) {
            visionScreeningResultDTO.setRescreening(Optional.ofNullable(ReScreenCardUtil.reScreeningResult(firstResult, reScreeningResult)).orElse(new ReScreenDTO()));
        }
        return visionScreeningResultDTO;
    }

    public SaprodontiaDataDTO getSaprodontiaDataDTO(VisionScreeningResult result){
        List<SaprodontiaDataDO.SaprodontiaItem> items = new ArrayList<>();
        if (Objects.nonNull(result)&&Objects.nonNull(result.getSaprodontiaData())){
            items.addAll(result.getSaprodontiaData().getAbove());
            items.addAll(result.getSaprodontiaData().getUnderneath());
        }
        return calculationTooth(items);
    }

    /**
     * 计算乳牙/恒牙
     * @param items
     */
    private SaprodontiaDataDTO calculationTooth(List<SaprodontiaDataDO.SaprodontiaItem> items) {
        SaprodontiaDataDTO saprodontiaDataDTO = new SaprodontiaDataDTO();
        int dCountDeciduous = 0;
        int mCountDeciduous = 0;
        int fFountDeciduous = 0;

        int dFountPermanent = 0;
        int mFountPermanent = 0;
        int fFountPermanent = 0;
        for (SaprodontiaDataDO.SaprodontiaItem item: items){
            if (item != null){
                if (item != null){
                    if (SaprodontiaType.DECIDUOUS_D.getName().equals(item.getDeciduous())){
                        dCountDeciduous++;
                    }
                    if (SaprodontiaType.DECIDUOUS_M.getName().equals(item.getDeciduous())){
                        mCountDeciduous++;
                    }
                    if (SaprodontiaType.DECIDUOUS_F.getName().equals(item.getDeciduous())){
                        fFountDeciduous++;
                    }
                    if (SaprodontiaType.PERMANENT_D.getName().equals(item.getPermanent())){
                        dFountPermanent++;
                    }
                    if (SaprodontiaType.PERMANENT_M.getName().equals(item.getPermanent())){
                        mFountPermanent++;
                    }
                    if (SaprodontiaType.PERMANENT_F.getName().equals(item.getPermanent())){
                        fFountPermanent++;
                    }
                }
            }
        }

        SaprodontiaStat deciduousTooth = new SaprodontiaStat();
        deciduousTooth.setDCount(dCountDeciduous);
        deciduousTooth.setMCount(mCountDeciduous);
        deciduousTooth.setFCount(fFountDeciduous);

        SaprodontiaStat permanentTooth = new SaprodontiaStat();
        permanentTooth.setDCount(dFountPermanent);
        permanentTooth.setMCount(mFountPermanent);
        permanentTooth.setFCount(fFountPermanent);

        saprodontiaDataDTO.setDeciduousTooth(deciduousTooth);
        saprodontiaDataDTO.setPermanentTooth(permanentTooth);

        return saprodontiaDataDTO;
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

}
