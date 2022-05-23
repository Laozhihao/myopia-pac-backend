package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseaseReportVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolCommonDiseaseReportVO;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * 按区域常见病报告
 *
 * @author hang.yuan 2022/5/19 14:09
 */
@Service
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

    public DistrictCommonDiseaseReportVO districtCommonDiseaseReport(Integer districtId,Integer noticeId){
        DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO = new DistrictCommonDiseaseReportVO();

        //全局变量
        getGlobalVariableVO(districtId, noticeId,districtCommonDiseaseReportVO);
        setNum(noticeId,districtCommonDiseaseReportVO);
        getVisionAnalysisVO(districtId,noticeId,districtCommonDiseaseReportVO);

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
            Set<String> yearPeriod= Sets.newHashSet(DateUtil.format(screeningNotice.getStartTime(), DatePattern.CHINESE_DATE_PATTERN),
                    DateUtil.format(screeningNotice.getEndTime(), "MM月dd日"));
            String screeningTimePeriod = CollectionUtil.join(yearPeriod, StrUtil.DASHED);
            globalVariableVO.setScreeningTimePeriod(screeningTimePeriod);
            globalVariableVO.setDataYear(year);
        }else {
            Set<String> yearPeriod= Sets.newHashSet(DateUtil.format(screeningNotice.getStartTime(), DatePattern.CHINESE_DATE_PATTERN),
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
            itemList.add(String.format(format,primary.size(),SchoolAge.PRIMARY.desc));
        }
        List<ScreeningPlanSchoolStudent> junior = planSchoolStudentMap.get(SchoolAge.JUNIOR.code);
        if (CollectionUtil.isNotEmpty(junior)){
            itemList.add(String.format(format,junior.size(),SchoolAge.JUNIOR.desc));
        }
        List<ScreeningPlanSchoolStudent> high = planSchoolStudentMap.get(SchoolAge.HIGH.code);
        if (CollectionUtil.isNotEmpty(high)){
            itemList.add(String.format(format,high.size(),SchoolAge.HIGH.desc));
        }
        List<ScreeningPlanSchoolStudent> vocationalHigh = planSchoolStudentMap.get(SchoolAge.VOCATIONAL_HIGH.code);
        if (CollectionUtil.isNotEmpty(vocationalHigh)){
            itemList.add(String.format(format,vocationalHigh.size(),SchoolAge.VOCATIONAL_HIGH.desc));
        }
        List<ScreeningPlanSchoolStudent> university = planSchoolStudentMap.get(SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(university)){
            itemList.add(String.format(format,university.size(),SchoolAge.UNIVERSITY.desc));
        }
        List<ScreeningPlanSchoolStudent> kindergarten = planSchoolStudentMap.get(SchoolAge.KINDERGARTEN.code);
        if (CollectionUtil.isNotEmpty(kindergarten)){
            itemList.add(String.format(format,kindergarten.size(),SchoolAge.KINDERGARTEN.desc));
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

        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByScreeningPlanIds(Lists.newArrayList(planIds));
        if (CollectionUtil.isEmpty(planSchoolStudentList)){
            districtCommonDiseaseReportVO.setScreeningStudentNum(0);
        }else {
            districtCommonDiseaseReportVO.setScreeningStudentNum(planSchoolStudentList.size());
        }

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
    private void getVisionAnalysisVO(Integer districtId, Integer noticeId,DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO){
        DistrictCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO = new DistrictCommonDiseaseReportVO.VisionAnalysisVO();
        StatConclusionQueryDTO statConclusionQueryDTO = new StatConclusionQueryDTO();
        statConclusionQueryDTO.setSrcScreeningNoticeId(noticeId);
        statConclusionQueryDTO.setIsValid(Boolean.TRUE);
        statConclusionQueryDTO.setIsRescreen(Boolean.FALSE);
        List<StatConclusion> statConclusionList = statConclusionService.listByQuery(statConclusionQueryDTO);
        if(CollectionUtil.isNotEmpty(statConclusionList)){
            int validScreeningNum = statConclusionList.size();
            visionAnalysisVO.setValidScreeningNum(validScreeningNum);
            List<StatConclusion> kindergartenList = statConclusionList.stream().filter(statConclusion -> Objects.equals(statConclusion.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());
            List<StatConclusion> primarySchoolAndAboveList = statConclusionList.stream().filter(statConclusion -> !Objects.equals(statConclusion.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());
            getKindergartenVO(kindergartenList,validScreeningNum,visionAnalysisVO);
            getPrimarySchoolAndAboveVO(primarySchoolAndAboveList,validScreeningNum,visionAnalysisVO);
        }else {
            districtCommonDiseaseReportVO.setVisionAnalysisVO(visionAnalysisVO);
        }

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

        kindergartenVO.setAvgVision(MathUtil.ratioNotSymbol(averageVision,new BigDecimal("6")));
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
        primarySchoolAndAboveVO.setAvgVision(MathUtil.ratioNotSymbol(averageVision,new BigDecimal("6")));
        primarySchoolAndAboveVO.setLowVisionRatio(MathUtil.ratioNotSymbol(lowVisionNum,validScreeningNum));
        primarySchoolAndAboveVO.setMyopiaRatio(MathUtil.ratioNotSymbol(myopiaNum,validScreeningNum));

        setSchoolAge(validScreeningNum, primarySchoolAndAboveVO, schoolMap);

        visionAnalysisVO.setPrimarySchoolAndAboveVO(primarySchoolAndAboveVO);
    }

    private void setSchoolAge(int validScreeningNum, DistrictCommonDiseaseReportVO.PrimarySchoolAndAboveVO primarySchoolAndAboveVO, Map<Integer, List<StatConclusion>> schoolMap) {
        //小学
        List<StatConclusion> primaryList = schoolMap.get(SchoolAge.PRIMARY.code);
        if (CollectionUtil.isNotEmpty(primaryList)){
            DistrictCommonDiseaseReportVO.MyopiaItemVO myopiaItemVO=new DistrictCommonDiseaseReportVO.MyopiaItemVO();
            myopiaItemVO.setSchoolAge(SchoolAge.PRIMARY.desc);
            myopiaItemVO.setMyopiaRatio(MathUtil.ratioNotSymbol(primaryList.size(),validScreeningNum));
            primarySchoolAndAboveVO.setPrimarySchool(myopiaItemVO);
        }
        //初中
        List<StatConclusion> juniorList = schoolMap.get(SchoolAge.JUNIOR.code);
        if (CollectionUtil.isNotEmpty(juniorList)){
            DistrictCommonDiseaseReportVO.MyopiaItemVO myopiaItemVO=new DistrictCommonDiseaseReportVO.MyopiaItemVO();
            myopiaItemVO.setSchoolAge(SchoolAge.JUNIOR.desc);
            myopiaItemVO.setMyopiaRatio(MathUtil.ratioNotSymbol(juniorList.size(),validScreeningNum));
            primarySchoolAndAboveVO.setJuniorHighSchool(myopiaItemVO);
        }

        //高中 （普高+职高）
        List<StatConclusion> highList = schoolMap.get(SchoolAge.HIGH.code);
        List<StatConclusion> vocationalHighList = schoolMap.get(SchoolAge.VOCATIONAL_HIGH.code);

        if (CollectionUtil.isNotEmpty(highList) && CollectionUtil.isNotEmpty(vocationalHighList)){
            DistrictCommonDiseaseReportVO.MyopiaItemVO high=new DistrictCommonDiseaseReportVO.MyopiaItemVO();
            high.setSchoolAge(SchoolAge.HIGH.desc);
            high.setMyopiaRatio(MathUtil.ratioNotSymbol(highList.size()+vocationalHighList.size(),validScreeningNum));
            primarySchoolAndAboveVO.setHighSchool(high);

            DistrictCommonDiseaseReportVO.MyopiaItemVO normalHigh=new DistrictCommonDiseaseReportVO.MyopiaItemVO();
            normalHigh.setSchoolAge("普通高中");
            normalHigh.setMyopiaRatio(MathUtil.ratioNotSymbol(highList.size(),validScreeningNum));
            primarySchoolAndAboveVO.setNormalHighSchool(normalHigh);

            DistrictCommonDiseaseReportVO.MyopiaItemVO vocationalHigh=new DistrictCommonDiseaseReportVO.MyopiaItemVO();
            vocationalHigh.setSchoolAge(SchoolAge.VOCATIONAL_HIGH.desc);
            vocationalHigh.setMyopiaRatio(MathUtil.ratioNotSymbol(vocationalHighList.size(),validScreeningNum));
            primarySchoolAndAboveVO.setVocationalHighSchool(vocationalHigh);
        }else if (CollectionUtil.isNotEmpty(highList) && CollectionUtil.isEmpty(vocationalHighList)){
            DistrictCommonDiseaseReportVO.MyopiaItemVO high=new DistrictCommonDiseaseReportVO.MyopiaItemVO();
            high.setSchoolAge(SchoolAge.HIGH.desc);
            high.setMyopiaRatio(MathUtil.ratioNotSymbol(highList.size(),validScreeningNum));
            primarySchoolAndAboveVO.setHighSchool(high);
        }else if(CollectionUtil.isEmpty(highList) && CollectionUtil.isNotEmpty(vocationalHighList)) {
            DistrictCommonDiseaseReportVO.MyopiaItemVO high=new DistrictCommonDiseaseReportVO.MyopiaItemVO();
            high.setSchoolAge(SchoolAge.HIGH.desc);
            high.setMyopiaRatio(MathUtil.ratioNotSymbol(vocationalHighList.size(),validScreeningNum));
            primarySchoolAndAboveVO.setHighSchool(high);

            DistrictCommonDiseaseReportVO.MyopiaItemVO vocationalHigh=new DistrictCommonDiseaseReportVO.MyopiaItemVO();
            vocationalHigh.setSchoolAge(SchoolAge.VOCATIONAL_HIGH.desc);
            vocationalHigh.setMyopiaRatio(MathUtil.ratioNotSymbol(vocationalHighList.size(),validScreeningNum));
            primarySchoolAndAboveVO.setVocationalHighSchool(vocationalHigh);
        }
        //大学
        List<StatConclusion> universityList = schoolMap.get(SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(universityList)){
            DistrictCommonDiseaseReportVO.MyopiaItemVO myopiaItemVO=new DistrictCommonDiseaseReportVO.MyopiaItemVO();
            myopiaItemVO.setSchoolAge(SchoolAge.UNIVERSITY.desc);
            myopiaItemVO.setMyopiaRatio(MathUtil.ratioNotSymbol(universityList.size(),validScreeningNum));
            primarySchoolAndAboveVO.setUniversity(myopiaItemVO);
        }
    }

    /**
     * 常见病分析
     */
    private void getDistrictCommonDiseasesAnalysisVO(Integer districtId, Integer noticeId,DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO){
        StatConclusionQueryDTO statConclusionQueryDTO = new StatConclusionQueryDTO();
        statConclusionQueryDTO.setSrcScreeningNoticeId(noticeId);
        statConclusionQueryDTO.setIsValid(Boolean.TRUE);
        statConclusionQueryDTO.setIsRescreen(Boolean.FALSE);
        List<StatConclusion> statConclusionList = statConclusionService.listByQuery(statConclusionQueryDTO);

        DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO = new DistrictCommonDiseasesAnalysisVO();
        getCommonDiseasesAnalysisVariableVO(statConclusionList,districtCommonDiseasesAnalysisVO);
        getDistrictDiseaseMonitorVO(statConclusionList,districtCommonDiseasesAnalysisVO);
        getDistrictSaprodontiaMonitorVO(statConclusionList,districtCommonDiseasesAnalysisVO);
        getDistrictHeightAndWeightMonitorVO(statConclusionList,districtCommonDiseasesAnalysisVO);
        getDistrictBloodPressureAndSpinalCurvatureMonitorVO(statConclusionList,districtCommonDiseasesAnalysisVO);
        getDistrictSchoolScreeningMonitorVO(statConclusionList,districtCommonDiseasesAnalysisVO);

        districtCommonDiseaseReportVO.setDistrictCommonDiseasesAnalysisVO(districtCommonDiseasesAnalysisVO);

    }

    /**
     * 各学校筛查情况
     */
    private void getDistrictSchoolScreeningMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

    }

    /**
     * 血压与脊柱弯曲异常监测结果
     */
    private void getDistrictBloodPressureAndSpinalCurvatureMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

    }

    /**
     * 体重身高监测结果
     */
    private void getDistrictHeightAndWeightMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

    }

    /**
     * 龋齿监测结果
     */
    private void getDistrictSaprodontiaMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

    }

    /**
     * 疾病监测情况
     */
    private void getDistrictDiseaseMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

    }

    /**
     * 常见病分析变量
     */
    private void getCommonDiseasesAnalysisVariableVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictCommonDiseasesAnalysisVO.CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO = new DistrictCommonDiseasesAnalysisVO.CommonDiseasesAnalysisVariableVO();
        List<StatConclusion> primaryAndAboveStatConclusionList = statConclusionList.stream().filter(sc -> !Objects.equals(sc.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());

        getSaprodontiaVO(primaryAndAboveStatConclusionList,commonDiseasesAnalysisVariableVO);
        getHeightAndWeightVO(primaryAndAboveStatConclusionList,commonDiseasesAnalysisVariableVO);
        getBloodPressureAndSpinalCurvatureVO(primaryAndAboveStatConclusionList,commonDiseasesAnalysisVariableVO);
        commonDiseasesAnalysisVariableVO.setValidScreeningNum(primaryAndAboveStatConclusionList.size());

        districtCommonDiseasesAnalysisVO.setCommonDiseasesAnalysisVariableVO(commonDiseasesAnalysisVariableVO);

    }
    private void getSaprodontiaVO(List<StatConclusion> primaryAndAboveStatConclusionList, DistrictCommonDiseasesAnalysisVO.CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO) {
        if (CollectionUtil.isEmpty(primaryAndAboveStatConclusionList)){
            return;
        }
        int validScreeningNum = primaryAndAboveStatConclusionList.size();
        DistrictCommonDiseasesAnalysisVO.SaprodontiaVO saprodontiaVO = new DistrictCommonDiseasesAnalysisVO.SaprodontiaVO();

        Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
        ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(0);

        int dmftNum = primaryAndAboveStatConclusionList.stream().filter(Objects::nonNull)
                .filter(predicateTrue)
                .mapToInt(totalFunction).sum();

        int saprodontiaNum = (int)primaryAndAboveStatConclusionList.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)primaryAndAboveStatConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaLoss)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)primaryAndAboveStatConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossAndRepairNum = (int)primaryAndAboveStatConclusionList.stream()
                .filter(sc ->Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair())).count();

        Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
        ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0);

        int lossAndRepairTeethNum = primaryAndAboveStatConclusionList.stream().filter(Objects::nonNull)
                .filter(lossAndRepairPredicateTrue)
                .mapToInt(lossAndRepairTotalFunction).sum();

        saprodontiaVO.setDmftNum(dmftNum);
        saprodontiaVO.setDmftRatio(MathUtil.num(dmftNum,validScreeningNum));
        saprodontiaVO.setSaprodontiaNum(saprodontiaNum);
        saprodontiaVO.setSaprodontiaRatio(MathUtil.ratio(saprodontiaNum,validScreeningNum));
        saprodontiaVO.setSaprodontiaLossNum(saprodontiaLossNum);
        saprodontiaVO.setSaprodontiaLossRatio(MathUtil.ratio(saprodontiaLossNum,validScreeningNum));
        saprodontiaVO.setSaprodontiaRepairNum(saprodontiaRepairNum);
        saprodontiaVO.setSaprodontiaRepairRatio(MathUtil.ratio(saprodontiaRepairNum,validScreeningNum));
        saprodontiaVO.setSaprodontiaLossAndRepairNum(saprodontiaLossAndRepairNum);
        saprodontiaVO.setSaprodontiaLossAndRepairRatio(MathUtil.ratio(saprodontiaLossAndRepairNum,validScreeningNum));
        saprodontiaVO.setSaprodontiaLossAndRepairTeethNum(lossAndRepairTeethNum);
        saprodontiaVO.setSaprodontiaLossAndRepairTeethRatio(MathUtil.ratio(lossAndRepairTeethNum,dmftNum));
        commonDiseasesAnalysisVariableVO.setSaprodontiaVO(saprodontiaVO);
    }

    private void getHeightAndWeightVO(List<StatConclusion> primaryAndAboveStatConclusionList, DistrictCommonDiseasesAnalysisVO.CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO) {
        if (CollectionUtil.isEmpty(primaryAndAboveStatConclusionList)){
            return;
        }
        int validScreeningNum = primaryAndAboveStatConclusionList.size();
        DistrictCommonDiseasesAnalysisVO.HeightAndWeightVO heightAndWeightVO = new DistrictCommonDiseasesAnalysisVO.HeightAndWeightVO();
        int overweightNum = (int)primaryAndAboveStatConclusionList.stream()
                .map(StatConclusion::getIsOverweight)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int obeseNum = (int)primaryAndAboveStatConclusionList.stream()
                .map(StatConclusion::getIsObesity)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

        heightAndWeightVO.setOverweightNum(overweightNum);
        heightAndWeightVO.setOverweightRatio(MathUtil.ratio(overweightNum,validScreeningNum));
        heightAndWeightVO.setObeseNum(obeseNum);
        heightAndWeightVO.setObeseRatio(MathUtil.ratio(obeseNum,validScreeningNum));
        commonDiseasesAnalysisVariableVO.setHeightAndWeightVO(heightAndWeightVO);
    }

    private void getBloodPressureAndSpinalCurvatureVO(List<StatConclusion> primaryAndAboveStatConclusionList, DistrictCommonDiseasesAnalysisVO.CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO) {
        if (CollectionUtil.isEmpty(primaryAndAboveStatConclusionList)){
            return;
        }
        int validScreeningNum = primaryAndAboveStatConclusionList.size();
        DistrictCommonDiseasesAnalysisVO.BloodPressureAndSpinalCurvatureVO bloodPressureAndSpinalCurvatureVO = new DistrictCommonDiseasesAnalysisVO.BloodPressureAndSpinalCurvatureVO();
        int abnormalSpineCurvatureNum = (int)primaryAndAboveStatConclusionList.stream()
                .map(StatConclusion::getIsSpinalCurvature)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int highBloodPressureNum = (int)primaryAndAboveStatConclusionList.stream()
                .filter(sc->Objects.equals(Boolean.FALSE,sc.getIsNormalBloodPressure())).count();

        bloodPressureAndSpinalCurvatureVO.setAbnormalSpineCurvatureNum(abnormalSpineCurvatureNum);
        bloodPressureAndSpinalCurvatureVO.setAbnormalSpineCurvatureRatio(MathUtil.ratio(abnormalSpineCurvatureNum,validScreeningNum));
        bloodPressureAndSpinalCurvatureVO.setHighBloodPressureNum(highBloodPressureNum);
        bloodPressureAndSpinalCurvatureVO.setHighBloodPressureRatio(MathUtil.ratio(highBloodPressureNum,validScreeningNum));
        commonDiseasesAnalysisVariableVO.setBloodPressureAndSpinalCurvatureVO(bloodPressureAndSpinalCurvatureVO);
    }



    private ThreeTuple<ScreeningNotice,List<ScreeningPlan>,List<VisionScreeningResult>> validData(Integer noticeId){
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);

        List<ScreeningPlan> screeningPlanList = screeningPlanService.getAllPlanByNoticeId(noticeId);

        List<VisionScreeningResult> screeningResults = Lists.newArrayList();
        if (CollectionUtil.isNotEmpty(screeningPlanList)){
            Set<Integer> planIds = screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
            screeningResults = visionScreeningResultService.getByPlanIds(Lists.newArrayList(planIds));

        }

        return new ThreeTuple<>(screeningNotice,screeningPlanList,screeningResults);
    }

}
