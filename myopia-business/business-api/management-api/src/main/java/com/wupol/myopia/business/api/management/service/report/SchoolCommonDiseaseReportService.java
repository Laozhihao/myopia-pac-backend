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
        visionAnalysisVO.setLowVision(getItem(visionAnalysisNum, VisionAnalysisNum::getLowMyopiaNum, VisionAnalysisNum::getLowVisionRatio));
        visionAnalysisVO.setAvgVision(avgVisionRatio);
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

    private SchoolCommonDiseaseReportVO.Item getItem(VisionAnalysisNum visionAnalysisNum,Function<VisionAnalysisNum,Integer> function,Function<VisionAnalysisNum,BigDecimal> mapper){
        Integer num = Optional.of(visionAnalysisNum).map(function).orElse(null);
        BigDecimal ratio = Optional.of(visionAnalysisNum).map(mapper).orElse(null);
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

        getSaprodontiaVO(statConclusionList,commonDiseasesAnalysisVariableVO);
        getHeightAndWeightVO(statConclusionList,commonDiseasesAnalysisVariableVO);
        getBloodPressureAndSpinalCurvatureVO(statConclusionList,commonDiseasesAnalysisVariableVO);
        commonDiseasesAnalysisVariableVO.setValidScreeningNum(statConclusionList.size());
        commonDiseasesAnalysisVariableVO.setAbnormalSpineCurvatureNum(abnormalSpineCurvatureNum);

        districtCommonDiseasesAnalysisVO.setCommonDiseasesAnalysisVariableVO(commonDiseasesAnalysisVariableVO);

    }

    /**
     * 常见病分析变量-龋齿数据
     */
    private void getSaprodontiaVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO.CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        CommonDiseasesNum commonDiseasesNum = new CommonDiseasesNum().build(statConclusionList).ratioNotSymbol();
        SchoolCommonDiseasesAnalysisVO.SaprodontiaVO saprodontiaVO = buildSaprodontiaVO(commonDiseasesNum);
        commonDiseasesAnalysisVariableVO.setSaprodontiaVO(saprodontiaVO);
    }

    private SchoolCommonDiseasesAnalysisVO.SaprodontiaVO buildSaprodontiaVO(CommonDiseasesNum commonDiseasesNum){
        SchoolCommonDiseasesAnalysisVO.SaprodontiaVO saprodontiaVO = new SchoolCommonDiseasesAnalysisVO.SaprodontiaVO();
        saprodontiaVO.setDmftNum(commonDiseasesNum.dmftNum);
        saprodontiaVO.setDmftRatio(commonDiseasesNum.dmftRatio);
        saprodontiaVO.setSaprodontiaNum(commonDiseasesNum.saprodontiaNum);
        saprodontiaVO.setSaprodontiaRatio(commonDiseasesNum.saprodontiaRatio);
        saprodontiaVO.setSaprodontiaLossNum(commonDiseasesNum.saprodontiaLossNum);
        saprodontiaVO.setSaprodontiaLossRatio(commonDiseasesNum.saprodontiaLossRatio);
        saprodontiaVO.setSaprodontiaRepairNum(commonDiseasesNum.saprodontiaRepairNum);
        saprodontiaVO.setSaprodontiaRepairRatio(commonDiseasesNum.saprodontiaRepairRatio);
        saprodontiaVO.setSaprodontiaLossAndRepairNum(commonDiseasesNum.saprodontiaLossAndRepairNum);
        saprodontiaVO.setSaprodontiaLossAndRepairRatio(commonDiseasesNum.saprodontiaLossAndRepairRatio);
        saprodontiaVO.setSaprodontiaLossAndRepairTeethNum(commonDiseasesNum.saprodontiaLossAndRepairTeethNum);
        saprodontiaVO.setSaprodontiaLossAndRepairTeethRatio(commonDiseasesNum.saprodontiaLossAndRepairTeethRatio);
        return saprodontiaVO;
    }

    /**
     * 常见病分析变量-身高体重数据
     */
    private void getHeightAndWeightVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO.CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        CommonDiseasesNum commonDiseasesNum = new CommonDiseasesNum().build(statConclusionList).ratioNotSymbol();
        SchoolCommonDiseasesAnalysisVO.HeightAndWeightVO heightAndWeightVO = buildHeightAndWeightVO(commonDiseasesNum);
        commonDiseasesAnalysisVariableVO.setHeightAndWeightVO(heightAndWeightVO);
    }

    private SchoolCommonDiseasesAnalysisVO.HeightAndWeightVO buildHeightAndWeightVO(CommonDiseasesNum commonDiseasesNum){
        SchoolCommonDiseasesAnalysisVO.HeightAndWeightVO heightAndWeightVO = new SchoolCommonDiseasesAnalysisVO.HeightAndWeightVO();
        heightAndWeightVO.setOverweightNum(commonDiseasesNum.overweightNum);
        heightAndWeightVO.setOverweightRatio(commonDiseasesNum.overweightRatio);
        heightAndWeightVO.setObeseNum(commonDiseasesNum.obeseNum);
        heightAndWeightVO.setObeseRatio(commonDiseasesNum.obeseRatio);
        return heightAndWeightVO;
    }

    /**
     * 常见病分析变量-血压和脊柱弯曲数据
     */
    private void getBloodPressureAndSpinalCurvatureVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO.CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        CommonDiseasesNum commonDiseasesNum = new CommonDiseasesNum().build(statConclusionList).ratioNotSymbol();
        SchoolCommonDiseasesAnalysisVO.BloodPressureAndSpinalCurvatureVO bloodPressureAndSpinalCurvatureVO = buildBloodPressureAndSpinalCurvatureVO(commonDiseasesNum);
        commonDiseasesAnalysisVariableVO.setBloodPressureAndSpinalCurvatureVO(bloodPressureAndSpinalCurvatureVO);
    }

    private SchoolCommonDiseasesAnalysisVO.BloodPressureAndSpinalCurvatureVO buildBloodPressureAndSpinalCurvatureVO(CommonDiseasesNum commonDiseasesNum){
        SchoolCommonDiseasesAnalysisVO.BloodPressureAndSpinalCurvatureVO bloodPressureAndSpinalCurvatureVO = new SchoolCommonDiseasesAnalysisVO.BloodPressureAndSpinalCurvatureVO();
        bloodPressureAndSpinalCurvatureVO.setAbnormalSpineCurvatureNum(commonDiseasesNum.abnormalSpineCurvatureNum);
        bloodPressureAndSpinalCurvatureVO.setAbnormalSpineCurvatureRatio(commonDiseasesNum.abnormalSpineCurvatureRatio);
        bloodPressureAndSpinalCurvatureVO.setHighBloodPressureNum(commonDiseasesNum.highBloodPressureNum);
        bloodPressureAndSpinalCurvatureVO.setHighBloodPressureRatio(commonDiseasesNum.highBloodPressureRatio);
        return bloodPressureAndSpinalCurvatureVO;
    }

    @Data
    public static class VisionAnalysisNum{
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

            List<Boolean> lowVisionList = getList(statConclusionList, StatConclusion::getIsLowVision);
            if (CollectionUtil.isNotEmpty(lowVisionList)){
                this.lowVisionNum = lowVisionList.size();
            }

            List<Boolean> myopiaList = getList(statConclusionList, StatConclusion::getIsMyopia);
            if(CollectionUtil.isNotEmpty(myopiaList)){
                this.myopiaNum = myopiaList.size();
            }

            List<StatConclusion> nightWearingOrthokeratologyLensesList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getGlassesType(), GlassesTypeEnum.ORTHOKERATOLOGY.code)).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(nightWearingOrthokeratologyLensesList)){
                this.nightWearingOrthokeratologyLensesNum=nightWearingOrthokeratologyLensesList.size();
            }
            List<StatConclusion> myopiaLevelEarlyList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getMyopiaLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code)).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(myopiaLevelEarlyList)){
                this.myopiaLevelEarlyNum =myopiaLevelEarlyList.size();
            }
            List<StatConclusion> lowMyopiaList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getMyopiaLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code)).collect(Collectors.toList());
            if(CollectionUtil.isNotEmpty(lowMyopiaList)){
                this.lowMyopiaNum = lowMyopiaList.size();
            }
            List<StatConclusion> highMyopiaList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getMyopiaLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.code)).collect(Collectors.toList());
            if(CollectionUtil.isNotEmpty(highMyopiaList)){
                this.highMyopiaNum = highMyopiaList.size();
            }

            List<Boolean> astigmatismList = getList(statConclusionList, StatConclusion::getIsAstigmatism);
            if (CollectionUtil.isNotEmpty(astigmatismList)){
                this.astigmatismNum = astigmatismList.size();
            }

            List<StatConclusion> myopiaEnoughCorrectedList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getVisionCorrection(), VisionCorrection.ENOUGH_CORRECTED.code)).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(myopiaEnoughCorrectedList)){
                this.myopiaEnoughCorrectedNum=myopiaEnoughCorrectedList.size();
            }
            List<StatConclusion> myopiaUncorrectedList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getVisionCorrection(), VisionCorrection.UNCORRECTED.code)).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(myopiaUncorrectedList)){
                this.myopiaUncorrectedNum=myopiaUncorrectedList.size();
            }
            List<StatConclusion> myopiaUnderCorrectedList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getVisionCorrection(), VisionCorrection.UNDER_CORRECTED.code)).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(myopiaUnderCorrectedList)){
                this.myopiaUnderCorrectedNum=myopiaUnderCorrectedList.size();
            }

            return this;
        }

        /**
         * 不带%
         */
        public VisionAnalysisNum ratioNotSymbol(){

            if (Objects.nonNull(lowVisionNum)){
                this.lowVisionRatio = MathUtil.ratioNotSymbol(lowVisionNum,validScreeningNum);
            }
            if (Objects.nonNull(myopiaNum)){
                this.myopiaRatio =MathUtil.ratioNotSymbol(myopiaNum,validScreeningNum);
            }
            if (Objects.nonNull(nightWearingOrthokeratologyLensesNum)){
                this.nightWearingOrthokeratologyLensesRatio = MathUtil.ratioNotSymbol(nightWearingOrthokeratologyLensesNum,validScreeningNum);
            }
            if (Objects.nonNull(myopiaLevelEarlyNum)){
                this.myopiaLevelEarlyRatio = MathUtil.ratioNotSymbol(myopiaLevelEarlyNum,validScreeningNum);
            }
            if (Objects.nonNull(lowMyopiaNum)){
                this.lowMyopiaRatio =MathUtil.ratioNotSymbol(lowMyopiaNum,validScreeningNum);
            }

            if (Objects.nonNull(highMyopiaNum)){
                this.highMyopiaRatio = MathUtil.ratioNotSymbol(highMyopiaNum,validScreeningNum);
            }
            if (Objects.nonNull(astigmatismNum)){
                this.astigmatismRatio = MathUtil.ratioNotSymbol(astigmatismNum,validScreeningNum);
            }

            if (Objects.nonNull(myopiaEnoughCorrectedNum)){
                this.myopiaEnoughCorrectedRatio = MathUtil.ratioNotSymbol(myopiaEnoughCorrectedNum,validScreeningNum);
            }
            if (Objects.nonNull(myopiaUncorrectedNum)){
                this.myopiaUncorrectedRatio = MathUtil.ratioNotSymbol(myopiaUncorrectedNum,validScreeningNum);
            }
            if (Objects.nonNull(myopiaUnderCorrectedNum)){
                this.myopiaUnderCorrectedRatio = MathUtil.ratioNotSymbol(myopiaUnderCorrectedNum,validScreeningNum);
            }

            return this;
        }


    }

    private static class CommonDiseasesNum{
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
        private String dmftRatio;

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

            if (CollectionUtil.isNotEmpty(statConclusionList)){
                this.validScreeningNum = statConclusionList.size();
            }

            Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
            ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(0);

            List<StatConclusion> dmftList = statConclusionList.stream().filter(Objects::nonNull).filter(predicateTrue).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(dmftList)){
                this.dmftNum = dmftList.stream().mapToInt(totalFunction).sum();
            }

            Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
            ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0);

            List<StatConclusion> lossAndRepairTeethList = statConclusionList.stream().filter(Objects::nonNull).filter(lossAndRepairPredicateTrue).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(lossAndRepairTeethList)){
                this.saprodontiaLossAndRepairTeethNum = lossAndRepairTeethList.stream().mapToInt(lossAndRepairTotalFunction).sum();
            }

            List<Boolean> saprodontiaList = getList(statConclusionList, StatConclusion::getIsSaprodontia);
            if (CollectionUtil.isNotEmpty(saprodontiaList)){
                this.saprodontiaNum = (int) saprodontiaList.stream().filter(Boolean::booleanValue).count();
            }

            List<Boolean> saprodontiaLossList = getList(statConclusionList, StatConclusion::getIsSaprodontiaLoss);
            if (CollectionUtil.isNotEmpty(saprodontiaLossList)){
                this.saprodontiaLossNum = (int) saprodontiaLossList.stream().filter(Boolean::booleanValue).count();
            }

            List<Boolean> saprodontiaRepairList = getList(statConclusionList, StatConclusion::getIsSaprodontiaRepair);
            if (CollectionUtil.isNotEmpty(saprodontiaRepairList)){
                this.saprodontiaRepairNum = (int) saprodontiaRepairList.stream().filter(Boolean::booleanValue).count();
            }
            List<StatConclusion> lossAndRepairList = statConclusionList.stream()
                    .filter(sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(lossAndRepairList)){
                this.saprodontiaLossAndRepairNum = lossAndRepairList.size();
            }

            List<Boolean> overweightList = getList(statConclusionList, StatConclusion::getIsOverweight);
            if (CollectionUtil.isNotEmpty(overweightList)){
                this.overweightNum = (int)overweightList.stream().filter(Boolean::booleanValue).count();
            }

            List<Boolean> obeseList = getList(statConclusionList, StatConclusion::getIsObesity);
            if(CollectionUtil.isNotEmpty(obeseList)){
                this.obeseNum = (int)obeseList.stream().filter(Boolean::booleanValue).count();
            }

            List<Boolean> abnormalSpineCurvatureList = getList(statConclusionList, StatConclusion::getIsObesity);
            if(CollectionUtil.isNotEmpty(abnormalSpineCurvatureList)){
                this.abnormalSpineCurvatureNum = (int)abnormalSpineCurvatureList.stream().filter(Boolean::booleanValue).count();
            }

            List<StatConclusion> highBloodPressureList = statConclusionList.stream().filter(sc->Objects.equals(Boolean.FALSE,sc.getIsNormalBloodPressure())).collect(Collectors.toList());
            if(CollectionUtil.isNotEmpty(obeseList)){
                this.highBloodPressureNum = highBloodPressureList.size();
            }

            return this;
        }


        /**
         * 不带%
         */
        public CommonDiseasesNum ratioNotSymbol(){

            if (Objects.nonNull(dmftNum)){
                this.dmftRatio = MathUtil.num(dmftNum,validScreeningNum);
            }

            if (Objects.nonNull(saprodontiaNum)){
                this.saprodontiaRatio = MathUtil.ratioNotSymbol(saprodontiaNum,validScreeningNum);
            }
            if (Objects.nonNull(saprodontiaLossNum)){
                this.saprodontiaLossRatio =MathUtil.ratioNotSymbol(saprodontiaLossNum,validScreeningNum);
            }
            if (Objects.nonNull(saprodontiaRepairNum)){
                this.saprodontiaRepairRatio = MathUtil.ratioNotSymbol(saprodontiaRepairNum,validScreeningNum);
            }
            if (Objects.nonNull(saprodontiaLossAndRepairNum)){
                this.saprodontiaLossAndRepairRatio = MathUtil.ratioNotSymbol(saprodontiaLossAndRepairNum,validScreeningNum);
            }
            if (Objects.nonNull(saprodontiaLossAndRepairTeethNum) && Objects.nonNull(dmftNum)){
                this.saprodontiaLossAndRepairTeethRatio =MathUtil.ratioNotSymbol(saprodontiaLossAndRepairTeethNum,dmftNum);
            }

            if (Objects.nonNull(overweightNum)){
                this.overweightRatio = MathUtil.ratioNotSymbol(overweightNum,validScreeningNum);
            }
            if (Objects.nonNull(obeseNum)){
                this.obeseRatio = MathUtil.ratioNotSymbol(obeseNum,validScreeningNum);
            }

            if (Objects.nonNull(abnormalSpineCurvatureNum)){
                this.abnormalSpineCurvatureRatio = MathUtil.ratioNotSymbol(abnormalSpineCurvatureNum,validScreeningNum);
            }
            if (Objects.nonNull(highBloodPressureNum)){
                this.highBloodPressureRatio = MathUtil.ratioNotSymbol(highBloodPressureNum,validScreeningNum);
            }

            return this;
        }

    }

    private static List<Boolean> getList(List<StatConclusion> statConclusionList, Function<StatConclusion,Boolean> function){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return Lists.newArrayList();
        }
        return statConclusionList.stream().map(function).filter(Objects::nonNull).filter(Boolean::booleanValue).collect(Collectors.toList());
    }
}
