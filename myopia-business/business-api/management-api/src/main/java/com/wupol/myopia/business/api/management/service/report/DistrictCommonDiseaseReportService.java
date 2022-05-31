package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseaseReportVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.service.report.*;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * 按区域常见病报告
 *
 * @author hang.yuan 2022/5/19 14:09
 */
@Service
@Slf4j
public class DistrictCommonDiseaseReportService {

    @Autowired
    private DistrictService districtService;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private SchoolService schoolService;

    @Autowired
    private DistrictSaprodontiaMonitorService districtSaprodontiaMonitorService;
    @Autowired
    private DistrictHeightAndWeightMonitorService districtHeightAndWeightMonitorService;
    @Autowired
    private DistrictBloodPressureAndSpinalCurvatureMonitorService districtBloodPressureAndSpinalCurvatureMonitorService;
    @Autowired
    private DistrictSchoolScreeningMonitorService districtSchoolScreeningMonitorService;
    @Autowired
    private DistrictDiseaseMonitorService districtDiseaseMonitorService;

    public DistrictCommonDiseaseReportVO districtCommonDiseaseReport(Integer districtId,Integer noticeId){
        DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO = new DistrictCommonDiseaseReportVO();

        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        districtIds.add(districtId);

        List<StatConclusion> statConclusionList = getStatConclusionList(noticeId,Lists.newArrayList(districtIds),Boolean.TRUE,Boolean.FALSE);

        //全局变量
        getGlobalVariableVO(districtId, noticeId,districtCommonDiseaseReportVO);
        //筛查人数和实际筛查人数
        setNum(noticeId,districtCommonDiseaseReportVO);
        //视力分析
        getVisionAnalysisVO(statConclusionList,districtCommonDiseaseReportVO);
        //常见病分析
        getDistrictCommonDiseasesAnalysisVO(statConclusionList,districtCommonDiseaseReportVO);

        return districtCommonDiseaseReportVO;
    }

    /**
     * 全局变量
     */
    private void getGlobalVariableVO(Integer districtId,Integer noticeId,DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO){
        DistrictCommonDiseaseReportVO.GlobalVariableVO globalVariableVO = new DistrictCommonDiseaseReportVO.GlobalVariableVO();
        String format="YYYY";

        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        if (Objects.isNull(screeningNotice)){
            throw new BusinessException(String.format("不存在筛查通知: noticeId=%s",noticeId));
        }
        List<ScreeningPlan> screeningPlanList = screeningPlanService.getAllPlanByNoticeId(noticeId);
        if (CollectionUtil.isEmpty(screeningPlanList)){
            throw new BusinessException(String.format("该筛查通知不存在筛查计划: noticeId=%s",noticeId));
        }

        Set<Integer> planIds = screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByPlanIds(Lists.newArrayList(planIds));
        if (CollectionUtil.isEmpty(screeningResults)){
            districtCommonDiseaseReportVO.setGlobalVariableVO(globalVariableVO);
            return;
        }

        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByScreeningPlanIds(Lists.newArrayList(planIds));
        if (CollectionUtil.isEmpty(planSchoolStudentList)){
            districtCommonDiseaseReportVO.setGlobalVariableVO(globalVariableVO);
            return;
        }

        long totalSum = planSchoolStudentList.stream().map(ScreeningPlanSchoolStudent::getSchoolId).distinct().count();

        Map<Integer, List<ScreeningPlanSchoolStudent>> planSchoolStudentMap = planSchoolStudentList.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getGradeType));
        setSchoolItemData(planSchoolStudentMap,globalVariableVO);

        //获取行政区域
        String districtName = districtService.getDistrictNameByDistrictId(districtId);
        Set<String> years= Sets.newHashSet(DateUtil.format(screeningNotice.getStartTime(), format),
                DateUtil.format(screeningNotice.getEndTime(), format));
        String year = CollectionUtil.join(years, "-");
        if (years.size()==1){
            List<String> yearPeriod= Lists.newArrayList(DateUtil.format(screeningNotice.getStartTime(), DatePattern.CHINESE_DATE_PATTERN),
                    DateUtil.format(screeningNotice.getEndTime(), "MM月dd日"));
            String screeningTimePeriod = CollectionUtil.join(yearPeriod, StrUtil.DASHED);
            globalVariableVO.setScreeningTimePeriod(screeningTimePeriod);
            globalVariableVO.setDataYear(year);
        }else {
            List<String> yearPeriod= Lists.newArrayList(DateUtil.format(screeningNotice.getStartTime(), DatePattern.CHINESE_DATE_PATTERN),
                    DateUtil.format(screeningNotice.getEndTime(), DatePattern.CHINESE_DATE_PATTERN));
            String screeningTimePeriod = CollectionUtil.join(yearPeriod, StrUtil.DASHED);
            globalVariableVO.setScreeningTimePeriod(screeningTimePeriod);

            VisionScreeningResult visionScreeningResult = screeningResults.get(0);
            String dataYear = DateUtil.format(visionScreeningResult.getCreateTime(),format);
            globalVariableVO.setDataYear(dataYear);
        }

        globalVariableVO.setAreaName(districtName);
        globalVariableVO.setYear(year);
        globalVariableVO.setTotalSchoolNum((int)totalSum);
        districtCommonDiseaseReportVO.setGlobalVariableVO(globalVariableVO);
    }


    /**
     * 学龄及学校数
     */
    private void setSchoolItemData(Map<Integer, List<ScreeningPlanSchoolStudent>> planSchoolStudentMap, DistrictCommonDiseaseReportVO.GlobalVariableVO globalVariableVO) {
        String format = "%s所%s";
        List<String> itemList= Lists.newArrayList();
        List<ScreeningPlanSchoolStudent> primary = planSchoolStudentMap.get(SchoolAge.PRIMARY.code);
        if (CollectionUtil.isNotEmpty(primary)){
            long count = primary.stream().map(ScreeningPlanSchoolStudent::getSchoolId).filter(Objects::nonNull).distinct().count();
            itemList.add(String.format(format,count,SchoolAge.PRIMARY.desc));
        }
        List<ScreeningPlanSchoolStudent> junior = planSchoolStudentMap.get(SchoolAge.JUNIOR.code);
        if (CollectionUtil.isNotEmpty(junior)){
            long count = junior.stream().map(ScreeningPlanSchoolStudent::getSchoolId).filter(Objects::nonNull).distinct().count();
            itemList.add(String.format(format,count,SchoolAge.JUNIOR.desc));
        }
        List<ScreeningPlanSchoolStudent> high = planSchoolStudentMap.get(SchoolAge.HIGH.code);
        if (CollectionUtil.isNotEmpty(high)){
            long count = high.stream().map(ScreeningPlanSchoolStudent::getSchoolId).filter(Objects::nonNull).distinct().count();
            itemList.add(String.format(format,count,SchoolAge.HIGH.desc));
        }
        List<ScreeningPlanSchoolStudent> vocationalHigh = planSchoolStudentMap.get(SchoolAge.VOCATIONAL_HIGH.code);
        if (CollectionUtil.isNotEmpty(vocationalHigh)){
            long count = vocationalHigh.stream().map(ScreeningPlanSchoolStudent::getSchoolId).filter(Objects::nonNull).distinct().count();
            itemList.add(String.format(format,count,"职高"));
        }
        List<ScreeningPlanSchoolStudent> university = planSchoolStudentMap.get(SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(university)){
            long count = university.stream().map(ScreeningPlanSchoolStudent::getSchoolId).filter(Objects::nonNull).distinct().count();
            itemList.add(String.format(format,count,"综合性"+SchoolAge.UNIVERSITY.desc));
        }
        List<ScreeningPlanSchoolStudent> kindergarten = planSchoolStudentMap.get(SchoolAge.KINDERGARTEN.code);
        if (CollectionUtil.isNotEmpty(kindergarten)){
            long count = kindergarten.stream().map(ScreeningPlanSchoolStudent::getSchoolId).filter(Objects::nonNull).distinct().count();
            itemList.add(String.format(format,count,SchoolAge.KINDERGARTEN.desc));
        }

        if (CollectionUtil.isNotEmpty(itemList)){
            globalVariableVO.setSchoolItem(itemList);
        }

    }

    /**
     * 筛查人数和实际筛查人数
     */
    private void setNum(Integer noticeId,DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO){
        List<ScreeningPlan> screeningPlanList = screeningPlanService.getAllPlanByNoticeId(noticeId);
        if (CollectionUtil.isEmpty(screeningPlanList)){
            throw new BusinessException(String.format("该筛查通知不存在筛查计划: noticeId=%s",noticeId));
        }
        Set<Integer> planIds = screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
        int screeningStudentNum = screeningPlanList.stream().mapToInt(sp -> Optional.ofNullable(sp.getStudentNumbers()).orElse(0)).sum();

        districtCommonDiseaseReportVO.setScreeningStudentNum(screeningStudentNum);

        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByPlanIds(Lists.newArrayList(planIds));
        if (CollectionUtil.isEmpty(screeningResults)){
            districtCommonDiseaseReportVO.setActualScreeningNum(0);
        }else {
            districtCommonDiseaseReportVO.setActualScreeningNum(screeningResults.size());
        }

    }

    /**
     * 视力分析
     */
    private void getVisionAnalysisVO(List<StatConclusion> statConclusionList,DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO = new DistrictCommonDiseaseReportVO.VisionAnalysisVO();
        int validScreeningNum = statConclusionList.size();
        visionAnalysisVO.setValidScreeningNum(validScreeningNum);
        List<StatConclusion> kindergartenList = statConclusionList.stream().filter(statConclusion -> Objects.equals(statConclusion.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());
        List<StatConclusion> primarySchoolAndAboveList = statConclusionList.stream().filter(statConclusion -> !Objects.equals(statConclusion.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());
        getKindergartenVO(kindergartenList,validScreeningNum,visionAnalysisVO);
        getPrimarySchoolAndAboveVO(primarySchoolAndAboveList,validScreeningNum,visionAnalysisVO);

        districtCommonDiseaseReportVO.setVisionAnalysisVO(visionAnalysisVO);
    }

    private void getKindergartenVO(List<StatConclusion> kindergartenList,int validScreeningNum,DistrictCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO){
        if (CollectionUtil.isEmpty(kindergartenList)){
            return;
        }
        DistrictCommonDiseaseReportVO.KindergartenVO kindergartenVO = new DistrictCommonDiseaseReportVO.KindergartenVO();
        int lowVisionNum = (int)kindergartenList.stream()
                .map(StatConclusion::getIsLowVision)
                .filter(Objects::nonNull).filter(Boolean::booleanValue)
                .count();

        Map<Integer, Long> visionLabelNumberMap = kindergartenList.stream().filter(stat -> Objects.nonNull(stat.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        Integer visionLabelZeroSpNum = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO_SP.code, 0L).intValue();

        int anisometropiaNum = (int) kindergartenList.stream()
                .map(StatConclusion::getIsAnisometropia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

        TwoTuple<BigDecimal, BigDecimal> averageVisionTuple = StatUtil.calculateAverageVision(kindergartenList);
        BigDecimal add = averageVisionTuple.getFirst().add(averageVisionTuple.getSecond());
        BigDecimal averageVision = BigDecimalUtil.divide(add, new BigDecimal("2"), 1);

        kindergartenVO.setAvgVision(averageVision);
        kindergartenVO.setAvgVisionRatio(MathUtil.ratioNotSymbol(averageVision,new BigDecimal("6")));
        kindergartenVO.setLowVisionRatio(MathUtil.ratioNotSymbol(lowVisionNum,validScreeningNum));
        kindergartenVO.setMyopiaLevelInsufficientRatio(MathUtil.ratioNotSymbol(visionLabelZeroSpNum,validScreeningNum));
        kindergartenVO.setAnisometropiaRatio(MathUtil.ratioNotSymbol(anisometropiaNum,validScreeningNum));

        visionAnalysisVO.setKindergartenVO(kindergartenVO);
    }


    private void getPrimarySchoolAndAboveVO(List<StatConclusion> primarySchoolAndAboveList,int validScreeningNum,DistrictCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO){
        if (CollectionUtil.isEmpty(primarySchoolAndAboveList)){
            return;
        }
        DistrictCommonDiseaseReportVO.PrimarySchoolAndAboveVO primarySchoolAndAboveVO = new DistrictCommonDiseaseReportVO.PrimarySchoolAndAboveVO();
        int lowVisionNum = (int)primarySchoolAndAboveList.stream()
                .map(StatConclusion::getIsLowVision)
                .filter(Objects::nonNull).filter(Boolean::booleanValue)
                .count();
        int myopiaNum = (int)primarySchoolAndAboveList.stream()
                .map(StatConclusion::getIsMyopia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue)
                .count();
        Map<Integer, List<StatConclusion>> schoolMap = primarySchoolAndAboveList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));

        TwoTuple<BigDecimal, BigDecimal> averageVisionTuple = StatUtil.calculateAverageVision(primarySchoolAndAboveList);
        BigDecimal add = averageVisionTuple.getFirst().add(averageVisionTuple.getSecond());
        BigDecimal averageVision = BigDecimalUtil.divide(add, new BigDecimal("2"), 1);
        primarySchoolAndAboveVO.setAvgVision(averageVision);
        primarySchoolAndAboveVO.setAvgVisionRatio(MathUtil.ratioNotSymbol(averageVision,new BigDecimal("6")));
        primarySchoolAndAboveVO.setLowVisionRatio(MathUtil.ratioNotSymbol(lowVisionNum,validScreeningNum));
        primarySchoolAndAboveVO.setMyopiaRatio(MathUtil.ratioNotSymbol(myopiaNum,validScreeningNum));

        setSchoolAge(validScreeningNum, primarySchoolAndAboveVO, schoolMap);

        visionAnalysisVO.setPrimarySchoolAndAboveVO(primarySchoolAndAboveVO);
    }

    private void setSchoolAge(int validScreeningNum, DistrictCommonDiseaseReportVO.PrimarySchoolAndAboveVO primarySchoolAndAboveVO, Map<Integer, List<StatConclusion>> schoolMap) {
        //小学
        List<StatConclusion> primaryList = schoolMap.get(SchoolAge.PRIMARY.code);
        getMyopiaItemVO(primaryList,SchoolAge.PRIMARY.desc,validScreeningNum,primarySchoolAndAboveVO::setPrimarySchool);

        //初中
        List<StatConclusion> juniorList = schoolMap.get(SchoolAge.JUNIOR.code);
        getMyopiaItemVO(juniorList,SchoolAge.JUNIOR.desc,validScreeningNum,primarySchoolAndAboveVO::setJuniorHighSchool);

        //高中 （普高+职高）
        List<StatConclusion> normalHighList = schoolMap.get(SchoolAge.HIGH.code);
        List<StatConclusion> vocationalHighList = schoolMap.get(SchoolAge.VOCATIONAL_HIGH.code);

        if (CollectionUtil.isNotEmpty(vocationalHighList)){
            List<StatConclusion> highList=Lists.newArrayList();
            if (Objects.nonNull(normalHighList)){
                highList.addAll(normalHighList);
            }else {
                normalHighList=Lists.newArrayList();
            }
            highList.addAll(vocationalHighList);

            getMyopiaItemVO(highList,SchoolAge.HIGH.desc,validScreeningNum,primarySchoolAndAboveVO::setHighSchool);
            getMyopiaItemVO(normalHighList,"普通高中",validScreeningNum,primarySchoolAndAboveVO::setNormalHighSchool);
            getMyopiaItemVO(vocationalHighList,SchoolAge.VOCATIONAL_HIGH.desc,validScreeningNum,primarySchoolAndAboveVO::setVocationalHighSchool);

        }else {
            getMyopiaItemVO(normalHighList,SchoolAge.HIGH.desc,validScreeningNum,primarySchoolAndAboveVO::setHighSchool);
        }

        //大学
        List<StatConclusion> universityList = schoolMap.get(SchoolAge.UNIVERSITY.code);
        getMyopiaItemVO(universityList,SchoolAge.UNIVERSITY.desc,validScreeningNum,primarySchoolAndAboveVO::setUniversity);

    }

    private void getMyopiaItemVO(List<StatConclusion> statConclusionList,String schoolAge,Integer validScreeningNum,
                                 Consumer<DistrictCommonDiseaseReportVO.MyopiaItemVO> consumer){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictCommonDiseaseReportVO.MyopiaItemVO myopiaItemVO=new DistrictCommonDiseaseReportVO.MyopiaItemVO();
        myopiaItemVO.setSchoolAge(schoolAge);
        myopiaItemVO.setMyopiaRatio(MathUtil.ratioNotSymbol(statConclusionList.size(),validScreeningNum));
        Optional.of(myopiaItemVO).ifPresent(consumer);
    }

    private List<StatConclusion> getStatConclusionList(Integer noticeId,List<Integer> districtIds,Boolean isValid,Boolean isRescreen){
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatConclusion::getSrcScreeningNoticeId, noticeId);
        queryWrapper.eq(StatConclusion::getIsValid, isValid);
        queryWrapper.eq(StatConclusion::getIsRescreen, isRescreen);
        queryWrapper.in(StatConclusion::getDistrictId,districtIds);
        return statConclusionService.list(queryWrapper);
    }

    /**
     * 常见病分析
     */
    private void getDistrictCommonDiseasesAnalysisVO(List<StatConclusion> statConclusionList,DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        List<StatConclusion> primaryAndAboveStatConclusionList = statConclusionList.stream().filter(sc -> !Objects.equals(sc.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());
        Map<Integer,String> schoolMap = Maps.newHashMap();
        if (CollectionUtil.isNotEmpty(primaryAndAboveStatConclusionList)){
            Set<Integer> schoolIds = primaryAndAboveStatConclusionList.stream().map(StatConclusion::getSchoolId).collect(Collectors.toSet());
            List<School> schoolList = schoolService.getByIds(Lists.newArrayList(schoolIds));
            Map<Integer, String> collect = schoolList.stream().collect(Collectors.toMap(School::getId, School::getName));
            schoolMap.putAll(collect);
        }

        DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO = new DistrictCommonDiseasesAnalysisVO();
        //常见病分析变量
        getCommonDiseasesAnalysisVariableVO(primaryAndAboveStatConclusionList,districtCommonDiseasesAnalysisVO);
        //疾病监测情况
        districtDiseaseMonitorService.getDistrictDiseaseMonitorVO(primaryAndAboveStatConclusionList,districtCommonDiseasesAnalysisVO);
        //龋齿监测结果
        districtSaprodontiaMonitorService.getDistrictSaprodontiaMonitorVO(primaryAndAboveStatConclusionList,districtCommonDiseasesAnalysisVO);
        //体重身高监测结果
        districtHeightAndWeightMonitorService.getDistrictHeightAndWeightMonitorVO(primaryAndAboveStatConclusionList,districtCommonDiseasesAnalysisVO);
        //血压与脊柱弯曲异常监测结果
        districtBloodPressureAndSpinalCurvatureMonitorService.getDistrictBloodPressureAndSpinalCurvatureMonitorVO(primaryAndAboveStatConclusionList,districtCommonDiseasesAnalysisVO);
        //各学校筛查情况
        districtSchoolScreeningMonitorService.getDistrictSchoolScreeningMonitorVO(primaryAndAboveStatConclusionList,schoolMap,districtCommonDiseasesAnalysisVO);

        districtCommonDiseaseReportVO.setDistrictCommonDiseasesAnalysisVO(districtCommonDiseasesAnalysisVO);

    }


    /**
     * 常见病分析变量
     */
    private void getCommonDiseasesAnalysisVariableVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictCommonDiseasesAnalysisVO.CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO = new DistrictCommonDiseasesAnalysisVO.CommonDiseasesAnalysisVariableVO();

        int abnormalSpineCurvatureNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSpinalCurvature)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

        CommonDiseasesNum commonDiseasesNum = new CommonDiseasesNum().build(statConclusionList).ratioNotSymbol();

        commonDiseasesAnalysisVariableVO.setValidScreeningNum(statConclusionList.size());
        commonDiseasesAnalysisVariableVO.setAbnormalSpineCurvatureNum(abnormalSpineCurvatureNum);
        commonDiseasesAnalysisVariableVO.setDmft(getItem(commonDiseasesNum,CommonDiseasesNum::getDmftNum,CommonDiseasesNum::getDmftRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontia(getItem(commonDiseasesNum,CommonDiseasesNum::getSaprodontiaNum,CommonDiseasesNum::getSaprodontiaRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontiaLoss(getItem(commonDiseasesNum,CommonDiseasesNum::getSaprodontiaLossNum,CommonDiseasesNum::getSaprodontiaLossRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontiaRepair(getItem(commonDiseasesNum,CommonDiseasesNum::getSaprodontiaRepairNum,CommonDiseasesNum::getSaprodontiaRepairRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontiaLossAndRepair(getItem(commonDiseasesNum,CommonDiseasesNum::getSaprodontiaLossAndRepairNum,CommonDiseasesNum::getSaprodontiaLossAndRepairRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontiaLossAndRepairTeeth(getItem(commonDiseasesNum,CommonDiseasesNum::getSaprodontiaLossAndRepairTeethNum,CommonDiseasesNum::getSaprodontiaLossAndRepairTeethRatio));
        commonDiseasesAnalysisVariableVO.setOverweight(getItem(commonDiseasesNum,CommonDiseasesNum::getOverweightNum,CommonDiseasesNum::getOverweightRatio));
        commonDiseasesAnalysisVariableVO.setObese(getItem(commonDiseasesNum,CommonDiseasesNum::getObeseNum,CommonDiseasesNum::getObeseRatio));
        commonDiseasesAnalysisVariableVO.setHighBloodPressure(getItem(commonDiseasesNum,CommonDiseasesNum::getHighBloodPressureNum,CommonDiseasesNum::getHighBloodPressureRatio));
        commonDiseasesAnalysisVariableVO.setAbnormalSpineCurvature(getItem(commonDiseasesNum,CommonDiseasesNum::getAbnormalSpineCurvatureNum,CommonDiseasesNum::getAbnormalSpineCurvatureRatio));
        districtCommonDiseasesAnalysisVO.setCommonDiseasesAnalysisVariableVO(commonDiseasesAnalysisVariableVO);

    }

    private DistrictCommonDiseasesAnalysisVO.Item getItem(CommonDiseasesNum commonDiseasesNum,Function<CommonDiseasesNum,Integer> function,Function<CommonDiseasesNum,BigDecimal> mapper){
        Integer num = Optional.of(commonDiseasesNum).map(function).orElse(ReportConst.ZERO);
        BigDecimal ratio = Optional.of(commonDiseasesNum).map(mapper).orElse(ReportConst.ZERO_BIG_DECIMAL);
        if (ObjectsUtil.allNotNull(num,ratio)){
            return new DistrictCommonDiseasesAnalysisVO.Item(num,ratio);
        }
        return null;
    }

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
            ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(ReportConst.ZERO) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(ReportConst.ZERO) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(ReportConst.ZERO);
            this.dmftNum = statConclusionList.stream()
                    .filter(Objects::nonNull)
                    .filter(predicateTrue).mapToInt(totalFunction).sum();

            Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
            ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(ReportConst.ZERO) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(ReportConst.ZERO);
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
