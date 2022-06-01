package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolCommonDiseaseReportVO;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.common.utils.constant.MyopiaLevelEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.VisionCorrection;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * 按学校常见病报告
 *
 * @author hang.yuan 2022/5/19 14:09
 */
@Service
public class SchoolCommonDiseaseReportService {

    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    @Autowired
    private SchoolSaprodontiaMonitorService schoolSaprodontiaMonitorService;
    @Autowired
    private SchoolHeightAndWeightMonitorService schoolHeightAndWeightMonitorService;
    @Autowired
    private SchoolBloodPressureAndSpinalCurvatureMonitorService schoolBloodPressureAndSpinalCurvatureMonitorService;
    @Autowired
    private SchoolClassScreeningMonitorService schoolClassScreeningMonitorService;


    public SchoolCommonDiseaseReportVO schoolCommonDiseaseReport(Integer schoolId,Integer planId){
        SchoolCommonDiseaseReportVO schoolCommonDiseaseReportVO = new SchoolCommonDiseaseReportVO();


        List<StatConclusion> statConclusionList = getStatConclusionList(schoolId,planId,Boolean.TRUE,Boolean.FALSE);

        //全局变量
        getGlobalVariableVO(schoolId,planId,schoolCommonDiseaseReportVO);
        //筛查人数和实际筛查人数
        setNum(schoolId,planId,schoolCommonDiseaseReportVO);
        //视力分析
        getVisionAnalysisVO(statConclusionList,schoolCommonDiseaseReportVO);
        //常见病分析
        getSchoolCommonDiseasesAnalysisVO(statConclusionList,schoolCommonDiseaseReportVO);

        return schoolCommonDiseaseReportVO;
    }

    /**
     * 全局变量
     */
    private void getGlobalVariableVO(Integer schoolId,Integer planId,SchoolCommonDiseaseReportVO districtCommonDiseaseReportVO){
        SchoolCommonDiseaseReportVO.GlobalVariableVO globalVariableVO = new SchoolCommonDiseaseReportVO.GlobalVariableVO();
        String format="YYYY";

        ScreeningPlan screeningPlan = screeningPlanService.getById(planId);
        if (Objects.isNull(screeningPlan)){
            throw new BusinessException(String.format("不存在筛查计划: planId=%s",planId));
        }

        School school = schoolService.getById(schoolId);
        if (Objects.isNull(school)){
            throw new BusinessException(String.format("不存在学校: schoolId=%s",schoolId));
        }

        ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(screeningPlan.getScreeningOrgId());
        if (Objects.isNull(screeningOrganization)){
            throw new BusinessException(String.format("不存在筛查机构: screeningOrgId=%s",screeningPlan.getScreeningOrgId()));
        }

        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByPlanIdAndSchoolId(planId, schoolId);
        if (CollectionUtil.isEmpty(screeningResults)){
            throw new BusinessException("暂无筛查数据！");
        }
        globalVariableVO.setReportDate(new Date());

        Date min = screeningResults.stream().map(VisionScreeningResult::getUpdateTime).min(Comparator.comparing(Date::getTime)).orElse(new Date());
        Date max = screeningResults.stream().map(VisionScreeningResult::getUpdateTime).max(Comparator.comparing(Date::getTime)).orElse(new Date());

        if (DateUtil.betweenDay(max,new Date()) > 3){
            globalVariableVO.setReportDate(max);
        }

        String dataYear = DateUtil.format(min,format);
        globalVariableVO.setDataYear(dataYear);

        Set<String> years= Sets.newHashSet(DateUtil.format(min, format),DateUtil.format(max, format));

        if (years.size()==1){
            List<String> yearPeriod= Lists.newArrayList(DateUtil.format(min, DatePattern.CHINESE_DATE_PATTERN),DateUtil.format(max, "MM月dd日"));
            String screeningTimePeriod = CollectionUtil.join(yearPeriod, StrUtil.DASHED);
            globalVariableVO.setScreeningTimePeriod(screeningTimePeriod);
        }else {
            List<String> yearPeriod= Lists.newArrayList(DateUtil.format(min, DatePattern.CHINESE_DATE_PATTERN),
                    DateUtil.format(max, DatePattern.CHINESE_DATE_PATTERN));
            String screeningTimePeriod = CollectionUtil.join(yearPeriod, StrUtil.DASHED);
            globalVariableVO.setScreeningTimePeriod(screeningTimePeriod);

        }

        globalVariableVO.setSchoolName(school.getName());
        globalVariableVO.setTakeQuestionnaireNum(0);
        globalVariableVO.setScreeningOrgName(screeningOrganization.getName());

        districtCommonDiseaseReportVO.setGlobalVariableVO(globalVariableVO);
    }



    /**
     * 筛查人数和实际筛查人数
     */
    private void setNum(Integer schoolId,Integer planId,SchoolCommonDiseaseReportVO districtCommonDiseaseReportVO){
        ScreeningPlan screeningPlan = screeningPlanService.getById(planId);
        if (Objects.isNull(screeningPlan)){
            throw new BusinessException(String.format("不存在筛查计划: planId=%s",planId));
        }

        districtCommonDiseaseReportVO.setScreeningStudentNum(screeningPlan.getStudentNumbers());

        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByPlanIdAndSchoolId(planId, schoolId);
        if (CollectionUtil.isEmpty(screeningResults)){
            districtCommonDiseaseReportVO.setActualScreeningNum(0);
        }else {
            districtCommonDiseaseReportVO.setActualScreeningNum(screeningResults.size());
        }

    }

    /**
     * 视力分析
     */
    private void getVisionAnalysisVO(List<StatConclusion> statConclusionList,SchoolCommonDiseaseReportVO districtCommonDiseaseReportVO){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<StatConclusion> primaryAndAboveStatConclusionList = statConclusionList.stream().filter(sc -> !Objects.equals(sc.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());
        SchoolCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO = new SchoolCommonDiseaseReportVO.VisionAnalysisVO();

        TwoTuple<BigDecimal, BigDecimal> averageVisionTuple = StatUtil.calculateAverageVision(primaryAndAboveStatConclusionList);
        BigDecimal add = averageVisionTuple.getFirst().add(averageVisionTuple.getSecond());
        BigDecimal averageVision = BigDecimalUtil.divide(add, new BigDecimal("2"), 1);
        BigDecimal avgVisionRatio = MathUtil.ratioNotSymbol(averageVision, new BigDecimal("6"));

        VisionAnalysisNum visionAnalysisNum = new VisionAnalysisNum().build(primaryAndAboveStatConclusionList).ratioNotSymbol();

        visionAnalysisVO.setValidScreeningNum(visionAnalysisNum.validScreeningNum);
        visionAnalysisVO.setLowVision(getItem(visionAnalysisNum, VisionAnalysisNum::getLowVisionNum, VisionAnalysisNum::getLowVisionRatio));
        visionAnalysisVO.setAvgVision(averageVision);
        visionAnalysisVO.setMyopia(getItem(visionAnalysisNum, VisionAnalysisNum::getMyopiaNum, VisionAnalysisNum::getMyopiaRatio));
        visionAnalysisVO.setNightWearingOrthokeratologyLenses(getItem(visionAnalysisNum, VisionAnalysisNum::getNightWearingOrthokeratologyLensesNum, VisionAnalysisNum::getNightWearingOrthokeratologyLensesRatio));
        visionAnalysisVO.setMyopiaLevelEarly(getItem(visionAnalysisNum, VisionAnalysisNum::getMyopiaLevelEarlyNum, VisionAnalysisNum::getMyopiaLevelEarlyRatio));
        visionAnalysisVO.setLowMyopia(getItem(visionAnalysisNum, VisionAnalysisNum::getLowMyopiaNum, VisionAnalysisNum::getLowMyopiaRatio));
        visionAnalysisVO.setHighMyopia(getItem(visionAnalysisNum, VisionAnalysisNum::getHighMyopiaNum, VisionAnalysisNum::getHighMyopiaRatio));
        visionAnalysisVO.setAstigmatism(getItem(visionAnalysisNum, VisionAnalysisNum::getAstigmatismNum, VisionAnalysisNum::getAstigmatismRatio));
        visionAnalysisVO.setMyopiaEnoughCorrected(getItem(visionAnalysisNum, VisionAnalysisNum::getMyopiaEnoughCorrectedNum, VisionAnalysisNum::getMyopiaEnoughCorrectedRatio));
        visionAnalysisVO.setMyopiaUncorrected(getItem(visionAnalysisNum, VisionAnalysisNum::getMyopiaUncorrectedNum, VisionAnalysisNum::getMyopiaUncorrectedRatio));
        visionAnalysisVO.setMyopiaUnderCorrected(getItem(visionAnalysisNum, VisionAnalysisNum::getMyopiaUnderCorrectedNum, VisionAnalysisNum::getMyopiaUnderCorrectedRatio));

        districtCommonDiseaseReportVO.setVisionAnalysisVO(visionAnalysisVO);
    }
    private SchoolCommonDiseasesAnalysisVO.Item getItem(CommonDiseasesNum commonDiseasesNum, Function<CommonDiseasesNum,Integer> function, Function<CommonDiseasesNum,BigDecimal> mapper){
        Integer num = Optional.of(commonDiseasesNum).map(function).orElse(ReportConst.ZERO);
        BigDecimal ratio = Optional.of(commonDiseasesNum).map(mapper).orElse(ReportConst.ZERO_BIG_DECIMAL);
        if (ObjectsUtil.allNotNull(num,ratio)){
            return new SchoolCommonDiseasesAnalysisVO.Item(num,ratio);
        }
        return null;
    }

    private SchoolCommonDiseaseReportVO.Item getItem(VisionAnalysisNum visionAnalysisNum,Function<VisionAnalysisNum,Integer> function,Function<VisionAnalysisNum,BigDecimal> mapper){
        Integer num = Optional.of(visionAnalysisNum).map(function).orElse(ReportConst.ZERO);
        BigDecimal ratio = Optional.of(visionAnalysisNum).map(mapper).orElse(ReportConst.ZERO_BIG_DECIMAL);
        if (ObjectsUtil.allNotNull(num,ratio)){
            return new SchoolCommonDiseaseReportVO.Item(num,ratio);
        }
        return null;
    }


    private List<StatConclusion> getStatConclusionList(Integer schoolId,Integer planId,Boolean isValid,Boolean isRescreen){
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatConclusion::getPlanId, planId);
        queryWrapper.eq(StatConclusion::getSchoolId, schoolId);
        queryWrapper.eq(StatConclusion::getIsValid, isValid);
        queryWrapper.eq(StatConclusion::getIsRescreen, isRescreen);
        return statConclusionService.list(queryWrapper);
    }

    /**
     * 常见病分析
     */
    private void getSchoolCommonDiseasesAnalysisVO(List<StatConclusion> statConclusionList,SchoolCommonDiseaseReportVO districtCommonDiseaseReportVO){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        List<StatConclusion> primaryAndAboveStatConclusionList = statConclusionList.stream().filter(sc -> !Objects.equals(sc.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());

        SchoolCommonDiseasesAnalysisVO schoolCommonDiseasesAnalysisVO = new SchoolCommonDiseasesAnalysisVO();
        //常见病分析变量
        getCommonDiseasesAnalysisVariableVO(primaryAndAboveStatConclusionList,schoolCommonDiseasesAnalysisVO);
        //龋齿监测结果
        schoolSaprodontiaMonitorService.getSchoolSaprodontiaMonitorVO(primaryAndAboveStatConclusionList,schoolCommonDiseasesAnalysisVO);
        //体重身高监测结果
        schoolHeightAndWeightMonitorService.getSchoolHeightAndWeightMonitorVO(primaryAndAboveStatConclusionList,schoolCommonDiseasesAnalysisVO);
        //血压与脊柱弯曲异常监测结果
        schoolBloodPressureAndSpinalCurvatureMonitorService.getSchoolBloodPressureAndSpinalCurvatureMonitorVO(primaryAndAboveStatConclusionList,schoolCommonDiseasesAnalysisVO);
        //各学校筛查情况
        schoolClassScreeningMonitorService.getSchoolClassScreeningMonitorVO(primaryAndAboveStatConclusionList,schoolCommonDiseasesAnalysisVO);

        districtCommonDiseaseReportVO.setSchoolCommonDiseasesAnalysisVO(schoolCommonDiseasesAnalysisVO);

    }


    /**
     * 常见病分析变量
     */
    private void getCommonDiseasesAnalysisVariableVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolCommonDiseasesAnalysisVO.CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO = new SchoolCommonDiseasesAnalysisVO.CommonDiseasesAnalysisVariableVO();

        int abnormalSpineCurvatureNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSpinalCurvature)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

      CommonDiseasesNum commonDiseasesNum = new CommonDiseasesNum().build(statConclusionList).ratioNotSymbol();

        commonDiseasesAnalysisVariableVO.setValidScreeningNum(statConclusionList.size());
        commonDiseasesAnalysisVariableVO.setAbnormalSpineCurvatureNum(abnormalSpineCurvatureNum);
        commonDiseasesAnalysisVariableVO.setDmft(getItem(commonDiseasesNum, CommonDiseasesNum::getDmftNum, CommonDiseasesNum::getDmftRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontia(getItem(commonDiseasesNum, CommonDiseasesNum::getSaprodontiaNum, CommonDiseasesNum::getSaprodontiaRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontiaLoss(getItem(commonDiseasesNum, CommonDiseasesNum::getSaprodontiaLossNum, CommonDiseasesNum::getSaprodontiaLossRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontiaRepair(getItem(commonDiseasesNum, CommonDiseasesNum::getSaprodontiaRepairNum, CommonDiseasesNum::getSaprodontiaRepairRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontiaLossAndRepair(getItem(commonDiseasesNum, CommonDiseasesNum::getSaprodontiaLossAndRepairNum, CommonDiseasesNum::getSaprodontiaLossAndRepairRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontiaLossAndRepairTeeth(getItem(commonDiseasesNum, CommonDiseasesNum::getSaprodontiaLossAndRepairTeethNum, CommonDiseasesNum::getSaprodontiaLossAndRepairTeethRatio));
        commonDiseasesAnalysisVariableVO.setOverweight(getItem(commonDiseasesNum, CommonDiseasesNum::getOverweightNum, CommonDiseasesNum::getOverweightRatio));
        commonDiseasesAnalysisVariableVO.setObese(getItem(commonDiseasesNum, CommonDiseasesNum::getObeseNum, CommonDiseasesNum::getObeseRatio));
        commonDiseasesAnalysisVariableVO.setHighBloodPressure(getItem(commonDiseasesNum, CommonDiseasesNum::getHighBloodPressureNum, CommonDiseasesNum::getHighBloodPressureRatio));
        commonDiseasesAnalysisVariableVO.setAbnormalSpineCurvature(getItem(commonDiseasesNum, CommonDiseasesNum::getAbnormalSpineCurvatureNum, CommonDiseasesNum::getAbnormalSpineCurvatureRatio));
        districtCommonDiseasesAnalysisVO.setCommonDiseasesAnalysisVariableVO(commonDiseasesAnalysisVariableVO);

    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class VisionAnalysisNum extends EntityFunction{
        /**
         * 有效筛查人数
         */
        private Integer validScreeningNum;

        /**
         * 视力低下人数
         */
        private Integer lowVisionNum;
        /**
         * 视力低下率
         */
        private BigDecimal lowVisionRatio;

        /**
         * 近视人数
         */
        private Integer myopiaNum;
        /**
         * 近视率
         */
        private BigDecimal myopiaRatio;

        /**
         * 夜戴角膜塑形镜人数
         */
        private Integer nightWearingOrthokeratologyLensesNum;

        /**
         * 夜戴角膜塑形镜率
         */
        private BigDecimal nightWearingOrthokeratologyLensesRatio;

        /**
         * 近视前期人数
         */
        private Integer myopiaLevelEarlyNum;

        /**
         * 近视前期率
         */
        private BigDecimal myopiaLevelEarlyRatio;

        /**
         * 低度近视人数
         */
        private Integer lowMyopiaNum;

        /**
         * 低度近视率
         */
        private BigDecimal lowMyopiaRatio;

        /**
         * 高度近视人数
         */
        private Integer highMyopiaNum;
        /**
         * 高度近视率
         */
        private BigDecimal highMyopiaRatio;

        /**
         * 散光人数
         */
        private Integer astigmatismNum;

        /**
         * 散光率
         */
        private BigDecimal astigmatismRatio;

        /**
         * 近视足矫人数
         */
        private Integer myopiaEnoughCorrectedNum;

        /**
         * 近视足矫率
         */
        private BigDecimal myopiaEnoughCorrectedRatio;

        /**
         * 近视未矫人数
         */
        private Integer myopiaUncorrectedNum;

        /**
         * 近视未矫率
         */
        private BigDecimal myopiaUncorrectedRatio;

        /**
         * 近视欠矫人数
         */
        private Integer myopiaUnderCorrectedNum;

        /**
         * 近视欠矫率
         */
        private BigDecimal myopiaUnderCorrectedRatio;

        public VisionAnalysisNum build(List<StatConclusion> statConclusionList){
            this.validScreeningNum = statConclusionList.size();
            this.lowVisionNum = getCount(statConclusionList, StatConclusion::getIsLowVision);
            this.myopiaNum = getCount(statConclusionList, StatConclusion::getIsMyopia);
            this.nightWearingOrthokeratologyLensesNum =getCount(statConclusionList,StatConclusion::getGlassesType, GlassesTypeEnum.ORTHOKERATOLOGY.code);
            this.myopiaLevelEarlyNum  = getCount(statConclusionList,StatConclusion::getMyopiaLevel,MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code);
            this.lowMyopiaNum  = getCount(statConclusionList,StatConclusion::getMyopiaLevel,MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code);
            this.highMyopiaNum   = getCount(statConclusionList,StatConclusion::getMyopiaLevel,MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.code);
            this.astigmatismNum  = getCount(statConclusionList, StatConclusion::getIsAstigmatism);
            this.myopiaEnoughCorrectedNum = getCount(statConclusionList,StatConclusion::getVisionCorrection,VisionCorrection.ENOUGH_CORRECTED.code);
            this.myopiaUncorrectedNum = getCount(statConclusionList,StatConclusion::getVisionCorrection,VisionCorrection.UNCORRECTED.code);
            this.myopiaUnderCorrectedNum = getCount(statConclusionList,StatConclusion::getVisionCorrection,VisionCorrection.UNDER_CORRECTED.code);
            return this;
        }

        /**
         * 不带%
         */
        public VisionAnalysisNum ratioNotSymbol(){

            this.lowVisionRatio = getRatioNotSymbol(lowVisionNum,validScreeningNum);
            this.myopiaRatio =getRatioNotSymbol(myopiaNum,validScreeningNum);
            this.nightWearingOrthokeratologyLensesRatio = getRatioNotSymbol(nightWearingOrthokeratologyLensesNum,validScreeningNum);
            this.myopiaLevelEarlyRatio = getRatioNotSymbol(myopiaLevelEarlyNum,validScreeningNum);
            this.lowMyopiaRatio =getRatioNotSymbol(lowMyopiaNum,validScreeningNum);
            this.highMyopiaRatio = getRatioNotSymbol(highMyopiaNum,validScreeningNum);
            this.astigmatismRatio = getRatioNotSymbol(astigmatismNum,validScreeningNum);
            this.myopiaEnoughCorrectedRatio = getRatioNotSymbol(myopiaEnoughCorrectedNum,validScreeningNum);
            this.myopiaUncorrectedRatio = getRatioNotSymbol(myopiaUncorrectedNum,validScreeningNum);
            this.myopiaUnderCorrectedRatio = getRatioNotSymbol(myopiaUnderCorrectedNum,validScreeningNum);

            return this;
        }


    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    private static class CommonDiseasesNum extends EntityFunction{
        /**
         * 筛查人数
         */
        private Integer validScreeningNum;
        /**
         * 龋失补牙数
         */
        private Integer dmftNum;

        /**
         * 龋均
         */
        private BigDecimal dmftRatio;

        /**
         * 有龋人数
         */
        private Integer saprodontiaNum;

        /**
         * 龋失人数
         */
        private Integer saprodontiaLossNum;

        /**
         * 龋补人数
         */
        private Integer saprodontiaRepairNum;

        /**
         * 龋患（失、补）人数
         */
        private Integer saprodontiaLossAndRepairNum;

        /**
         * 龋患（失、补）牙数
         */
        private Integer saprodontiaLossAndRepairTeethNum;

        /**
         * 超重人数
         */
        private Integer overweightNum;

        /**
         * 肥胖人数
         */
        private Integer obeseNum;

        /**
         * 血压偏高人数
         */
        private Integer highBloodPressureNum;

        /**
         * 脊柱弯曲异常人数
         */
        private Integer abnormalSpineCurvatureNum;

        // ============ 不带% ==============
        /**
         * 龋患率
         */
        private BigDecimal saprodontiaRatio;
        /**
         * 龋失率
         */
        private BigDecimal saprodontiaLossRatio;

        /**
         * 龋补率
         */
        private BigDecimal saprodontiaRepairRatio;
        /**
         * 龋患（失、补）率
         */
        private BigDecimal saprodontiaLossAndRepairRatio;

        /**
         * 龋患（失、补）构成比
         */
        private BigDecimal saprodontiaLossAndRepairTeethRatio;

        /**
         * 超重率
         */
        private BigDecimal overweightRatio;
        /**
         * 肥胖率
         */
        private BigDecimal obeseRatio;

        /**
         * 血压偏高率
         */
        private BigDecimal highBloodPressureRatio;

        /**
         * 脊柱弯曲异常率
         */
        private BigDecimal abnormalSpineCurvatureRatio;

        public CommonDiseasesNum build(List<StatConclusion> statConclusionList){

            this.validScreeningNum = statConclusionList.size();

            Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
            ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(0);
            this.dmftNum = statConclusionList.stream()
                    .filter(Objects::nonNull)
                    .filter(predicateTrue).mapToInt(totalFunction).sum();

            Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
            ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0);

            this.saprodontiaLossAndRepairTeethNum = statConclusionList.stream()
                    .filter(Objects::nonNull)
                    .filter(lossAndRepairPredicateTrue).mapToInt(lossAndRepairTotalFunction).sum();
            this.saprodontiaNum = getCount(statConclusionList, StatConclusion::getIsSaprodontia);
            this.saprodontiaLossNum = getCount(statConclusionList, StatConclusion::getIsSaprodontiaLoss);
            this.saprodontiaRepairNum =  getCount(statConclusionList, StatConclusion::getIsSaprodontiaRepair);
            this.saprodontiaLossAndRepairNum = (int)statConclusionList.stream().filter(lossAndRepairPredicateTrue).count();
            this.overweightNum = getCount(statConclusionList, StatConclusion::getIsOverweight);
            this.obeseNum = getCount(statConclusionList, StatConclusion::getIsObesity);
            this.abnormalSpineCurvatureNum = getCount(statConclusionList, StatConclusion::getIsObesity);
            this.highBloodPressureNum = (int)statConclusionList.stream().filter(sc->Objects.equals(Boolean.FALSE,sc.getIsNormalBloodPressure())).count();
            return this;
        }


        /**
         * 不带%
         */
        public CommonDiseasesNum ratioNotSymbol(){
            this.dmftRatio = Optional.ofNullable(MathUtil.numNotSymbol(dmftNum,validScreeningNum)).orElse(ReportConst.ZERO_BIG_DECIMAL);
            this.saprodontiaRatio = getRatioNotSymbol(saprodontiaNum,validScreeningNum);
            this.saprodontiaRepairRatio = getRatioNotSymbol(saprodontiaRepairNum,validScreeningNum);
            this.saprodontiaLossAndRepairRatio = getRatioNotSymbol(saprodontiaLossAndRepairNum,validScreeningNum);
            this.saprodontiaLossAndRepairTeethRatio =getRatioNotSymbol(saprodontiaLossAndRepairTeethNum,dmftNum);
            this.overweightRatio = getRatioNotSymbol(overweightNum,validScreeningNum);
            this.obeseRatio = getRatioNotSymbol(obeseNum,validScreeningNum);
            this.abnormalSpineCurvatureRatio = getRatioNotSymbol(abnormalSpineCurvatureNum,validScreeningNum);
            this.highBloodPressureRatio = getRatioNotSymbol(highBloodPressureNum,validScreeningNum);
            return this;
        }

    }

}
