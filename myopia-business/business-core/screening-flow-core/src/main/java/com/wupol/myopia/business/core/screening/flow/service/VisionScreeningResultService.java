package com.wupol.myopia.business.core.screening.flow.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningCountDTO;
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
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
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

    /***
     * @Description: 学生ID集合
     * @Param: [studentIds]
     * @return: java.util.List<com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult>
     * @Author: 钓猫的小鱼
     * @Date: 2022/1/12
     */
    public List<VisionScreeningResult> getByStudentIdsAndPlanId(Integer planId, List<Integer> studentIds, Integer isDoubleScreen) {
        return baseMapper.getByStudentIdsAndPlanId(planId, studentIds, isDoubleScreen);
    }

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
        List<VisionScreeningResult> resultList = baseMapper.getByPlanIdAndSchoolId(planId, schoolId);
        if (CollectionUtils.isEmpty(resultList)) {
            return new ArrayList<>();
        }

        return resultList.stream().map(VisionScreeningResult::getScreeningPlanSchoolStudentId).collect(Collectors.toList());
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
     * 获取学生初筛/复测（默认初测）
     *
     * @param planId                       计划ID
     * @param screeningPlanSchoolStudentId 学生ID
     * @param isDoubleScreen               false：初测  true：复测
     * @return
     */
    public VisionScreeningResult getIsDoubleScreeningResult(Integer planId, Integer screeningPlanSchoolStudentId, boolean isDoubleScreen) {
        VisionScreeningResult visionScreeningResultQuery = new VisionScreeningResult().setPlanId(planId).setScreeningPlanSchoolStudentId(screeningPlanSchoolStudentId).setIsDoubleScreen(isDoubleScreen);
        QueryWrapper<VisionScreeningResult> queryWrapper = getQueryWrapper(visionScreeningResultQuery);
        return getOne(queryWrapper);
    }

    /**
     * 获取学生初筛/复测（默认初测）
     *
     * @param planIds                      计划ID集合
     * @param screeningPlanSchoolStudentId 学生ID
     * @param isDoubleScreen               false：初测  true：复测
     * @return
     */
    public List<VisionScreeningResult> getIsDoubleScreeningResult(List<Integer> planIds, Integer screeningPlanSchoolStudentId, boolean isDoubleScreen) {
        return baseMapper.getIsDoubleScreeningResult(planIds, screeningPlanSchoolStudentId, isDoubleScreen);
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
     * 通过计划id，学校id获取复查学生数据
     *
     * @param planId    计划Id
     * @param schoolIds 学校Id
     * @return List<VisionScreeningResult>
     */
    public Map<Integer, List<VisionScreeningResult>> getMapRescreenBySchoolIds(Integer planId, List<Integer> schoolIds) {
        List<VisionScreeningResult> results = getRescreenBySchoolIds(planId, schoolIds);
        if (CollectionUtils.isEmpty(results)) {
            return new HashMap<>();
        }
        return results.stream().collect(Collectors.groupingBy(VisionScreeningResult::getSchoolId));
    }

    public VisionScreeningResultDTO getStudentEyeByStudentId(List<VisionScreeningResult> visionScreeningResults, List<VisionScreeningResult> doubleScreeningResults) {
        VisionScreeningResultDTO visionScreeningResultDTO = new VisionScreeningResultDTO();
        if (!visionScreeningResults.isEmpty()){
            BeanUtils.copyProperties(visionScreeningResults.get(0), visionScreeningResultDTO);
            visionScreeningResultDTO.setSaprodontiaDataDTO(getSaprodontiaDataDTO(visionScreeningResults.get(0)));
            ScreeningPlanSchoolStudent schoolStudent = new ScreeningPlanSchoolStudent();
            schoolStudent.setId(visionScreeningResults.get(0).getScreeningPlanSchoolStudentId());
            schoolStudent.setScreeningPlanId(visionScreeningResults.get(0).getPlanId());
            ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(schoolStudent);
            if (screeningPlanSchoolStudent != null){
                visionScreeningResultDTO.setGender(screeningPlanSchoolStudent.getGender());
            }
            if (!doubleScreeningResults.isEmpty() && doubleScreeningResults.get(0).getVisionData() !=null
                    && doubleScreeningResults.get(0).getComputerOptometry() != null && doubleScreeningResults.get(0).getHeightAndWeightData() != null){

                visionScreeningResultDTO.setRescreening(ReScreenCardUtil.reScreeningResult(visionScreeningResults.get(0),doubleScreeningResults.get(0)));
            }
            visionScreeningResultDTO.setLeftSE(getLeftSphericalEquivalent(visionScreeningResults.get(0)));
            visionScreeningResultDTO.setRightSE(getRightSphericalEquivalent(visionScreeningResults.get(0)));
            visionScreeningResultDTO.setSaprodontiaData(VisionScreeningResultDTO.saprodontiaDataDOIsNull(visionScreeningResultDTO.getSaprodontiaData()));
            visionScreeningResultDTO.setSpineData(VisionScreeningResultDTO.spineDataDOIsNull(visionScreeningResultDTO.getSpineData()));
            visionScreeningResultDTO.setBloodPressureData(VisionScreeningResultDTO.bloodPressureDataDOIsNull(visionScreeningResultDTO.getBloodPressureData()));
            visionScreeningResultDTO.setDiseasesHistoryData(VisionScreeningResultDTO.diseasesHistoryDOIsNull(visionScreeningResultDTO.getDiseasesHistoryData()));
            visionScreeningResultDTO.setPrivacyData(VisionScreeningResultDTO.privacyDataDOIsNull(visionScreeningResultDTO.getPrivacyData()));
            visionScreeningResultDTO.setDeviationData(VisionScreeningResultDTO.deviationDOIsNull(visionScreeningResultDTO.getDeviationData()));
            visionScreeningResultDTO.setOtherEyeDiseases(VisionScreeningResultDTO.otherEyeDiseasesDOIsNull(visionScreeningResultDTO.getOtherEyeDiseases()));
            visionScreeningResultDTO.setRescreening(VisionScreeningResultDTO.reScreenDTOIsNull(visionScreeningResultDTO.getRescreening()));
        }
        return visionScreeningResultDTO;
    }

    private BigDecimal getLeftSphericalEquivalent(VisionScreeningResult result){
        return StatUtil.getSphericalEquivalent(EyeDataUtil.leftSph(result),EyeDataUtil.leftCyl(result));
    }

    private BigDecimal getRightSphericalEquivalent(VisionScreeningResult result){
        return StatUtil.getSphericalEquivalent(EyeDataUtil.rightSph(result),EyeDataUtil.rightCyl(result));
    }


    public SaprodontiaDataDTO getSaprodontiaDataDTO(VisionScreeningResult result) {
        List<SaprodontiaDataDO.SaprodontiaItem> items = new ArrayList<>();
        if (Objects.nonNull(result) && Objects.nonNull(result.getSaprodontiaData())) {
            items.addAll(result.getSaprodontiaData().getAbove());
            items.addAll(result.getSaprodontiaData().getUnderneath());
        }

        return calculationTooth(items);
    }

    /**
     * 计算乳牙/恒牙
     *
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
        for (SaprodontiaDataDO.SaprodontiaItem item : items) {
            if (item != null) {
                if (item != null) {
                    if (SaprodontiaType.DECIDUOUS_D.getName().equals(item.getDeciduous())) {
                        dCountDeciduous++;
                    }
                    if (SaprodontiaType.DECIDUOUS_M.getName().equals(item.getDeciduous())) {
                        mCountDeciduous++;
                    }
                    if (SaprodontiaType.DECIDUOUS_F.getName().equals(item.getDeciduous())) {
                        fFountDeciduous++;
                    }
                    if (SaprodontiaType.PERMANENT_D.getName().equals(item.getPermanent())) {
                        dFountPermanent++;
                    }
                    if (SaprodontiaType.PERMANENT_M.getName().equals(item.getPermanent())) {
                        mFountPermanent++;
                    }
                    if (SaprodontiaType.PERMANENT_F.getName().equals(item.getPermanent())) {
                        fFountPermanent++;
                    }
                }
            }
        }

        SaprodontiaStatItem deciduousTooth = new SaprodontiaStatItem();
        deciduousTooth.setDCount(dCountDeciduous);
        deciduousTooth.setMCount(mCountDeciduous);
        deciduousTooth.setFCount(fFountDeciduous);

        SaprodontiaStatItem permanentTooth = new SaprodontiaStatItem();
        permanentTooth.setDCount(dFountPermanent);
        permanentTooth.setMCount(mFountPermanent);
        permanentTooth.setFCount(fFountPermanent);

        saprodontiaDataDTO.setDeciduousTooth(deciduousTooth);
        saprodontiaDataDTO.setPermanentTooth(permanentTooth);

        return saprodontiaDataDTO;
    }

    public VisionScreeningResult getIsDoubleScreen(Integer screeningPlanSchoolStudentId, Integer planId, Integer screeningType) {

        return baseMapper.getIsDoubleScreen(screeningPlanSchoolStudentId, planId, screeningType);
    }

    ;

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

}
