package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseaseReportVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictDiseaseMonitorVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictSaprodontiaMonitorVO;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.DiseaseNumDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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
        //筛查人数和实际筛查人数
        setNum(noticeId,districtCommonDiseaseReportVO);
        //视力分析
        getVisionAnalysisVO(districtId,noticeId,districtCommonDiseaseReportVO);
        //常见病分析
        getDistrictCommonDiseasesAnalysisVO(districtId,noticeId,districtCommonDiseaseReportVO);

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

        if (CollectionUtil.isNotEmpty(vocationalHighList)){

            DistrictCommonDiseaseReportVO.MyopiaItemVO high=new DistrictCommonDiseaseReportVO.MyopiaItemVO();
            high.setSchoolAge(SchoolAge.HIGH.desc);
            high.setMyopiaRatio(MathUtil.ratioNotSymbol(Optional.ofNullable(highList).map(List::size).orElse(0)+vocationalHighList.size(),validScreeningNum));
            primarySchoolAndAboveVO.setHighSchool(high);

            DistrictCommonDiseaseReportVO.MyopiaItemVO normalHigh=new DistrictCommonDiseaseReportVO.MyopiaItemVO();
            normalHigh.setSchoolAge("普通高中");
            if (Objects.nonNull(highList)){
                normalHigh.setMyopiaRatio(MathUtil.ratioNotSymbol(highList.size(),validScreeningNum));
            }
            primarySchoolAndAboveVO.setNormalHighSchool(normalHigh);

            DistrictCommonDiseaseReportVO.MyopiaItemVO vocationalHigh=new DistrictCommonDiseaseReportVO.MyopiaItemVO();
            vocationalHigh.setSchoolAge(SchoolAge.VOCATIONAL_HIGH.desc);
            vocationalHigh.setMyopiaRatio(MathUtil.ratioNotSymbol(vocationalHighList.size(),validScreeningNum));
            primarySchoolAndAboveVO.setVocationalHighSchool(vocationalHigh);


        }else {
            DistrictCommonDiseaseReportVO.MyopiaItemVO high=new DistrictCommonDiseaseReportVO.MyopiaItemVO();
            high.setSchoolAge(SchoolAge.HIGH.desc);
            high.setMyopiaRatio(MathUtil.ratioNotSymbol(highList.size(),validScreeningNum));
            primarySchoolAndAboveVO.setHighSchool(high);
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
        List<StatConclusion> primaryAndAboveStatConclusionList = statConclusionList.stream().filter(sc -> !Objects.equals(sc.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());

        DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO = new DistrictCommonDiseasesAnalysisVO();
        getCommonDiseasesAnalysisVariableVO(primaryAndAboveStatConclusionList,districtCommonDiseasesAnalysisVO);
        getDistrictDiseaseMonitorVO(primaryAndAboveStatConclusionList,districtCommonDiseasesAnalysisVO);
        getDistrictSaprodontiaMonitorVO(primaryAndAboveStatConclusionList,districtCommonDiseasesAnalysisVO);
        getDistrictHeightAndWeightMonitorVO(primaryAndAboveStatConclusionList,districtCommonDiseasesAnalysisVO);
        getDistrictBloodPressureAndSpinalCurvatureMonitorVO(primaryAndAboveStatConclusionList,districtCommonDiseasesAnalysisVO);
        getDistrictSchoolScreeningMonitorVO(primaryAndAboveStatConclusionList,districtCommonDiseasesAnalysisVO);

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
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO = new DistrictSaprodontiaMonitorVO();

        //说明变量
        getSaprodontiaMonitorVariableVO(statConclusionList,districtSaprodontiaMonitorVO);
        //不同性别
        getSaprodontiaSexVO(statConclusionList,districtSaprodontiaMonitorVO);
        //不同学龄段
        getSaprodontiaSchoolAgeVO(statConclusionList,districtSaprodontiaMonitorVO);
        //不同年龄段
        getSaprodontiaAgeVO(statConclusionList,districtSaprodontiaMonitorVO);

        districtCommonDiseasesAnalysisVO.setDistrictSaprodontiaMonitorVO(districtSaprodontiaMonitorVO);
    }

    /**
     * 龋齿监测结果-说明变量
     */
    private void getSaprodontiaMonitorVariableVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        int validScreeningNum = statConclusionList.size();

        int saprodontiaNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossAndRepairNum = (int)statConclusionList.stream()
                .filter(sc ->Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair())).count();

        Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
        ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0);

        int lossAndRepairTeethNum = statConclusionList.stream().filter(Objects::nonNull)
                .filter(lossAndRepairPredicateTrue)
                .mapToInt(lossAndRepairTotalFunction).sum();

        Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
        ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(0);

        int dmftNum = statConclusionList.stream().filter(Objects::nonNull)
                .filter(predicateTrue)
                .mapToInt(totalFunction).sum();

        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO();
        saprodontiaMonitorVariableVO.setDmftRatio(MathUtil.ratio(dmftNum,validScreeningNum));
        saprodontiaMonitorVariableVO.setSaprodontiaRatio(MathUtil.ratio(saprodontiaNum,validScreeningNum));
        saprodontiaMonitorVariableVO.setSaprodontiaRepairRatio(MathUtil.ratio(saprodontiaRepairNum,validScreeningNum));
        saprodontiaMonitorVariableVO.setSaprodontiaLossAndRepairRatio(MathUtil.ratio(saprodontiaLossAndRepairNum,validScreeningNum));
        saprodontiaMonitorVariableVO.setSaprodontiaLossAndRepairTeethRatio(MathUtil.ratio(lossAndRepairTeethNum,dmftNum));

        districtSaprodontiaMonitorVO.setSaprodontiaMonitorVariableVO(saprodontiaMonitorVariableVO);

    }

    /**
     * 龋齿监测结果-不同性别
     */
    private void getSaprodontiaSexVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaSexVO saprodontiaSexVO = new DistrictSaprodontiaMonitorVO.SaprodontiaSexVO();
        getSaprodontiaSexVariableVO(statConclusionList,saprodontiaSexVO);
        getSaprodontiaSexMonitorTableList(statConclusionList,saprodontiaSexVO);

        districtSaprodontiaMonitorVO.setSaprodontiaSex(saprodontiaSexVO);

    }

    /**
     * 龋齿监测结果-不同性别-说明变量
     */
    private void getSaprodontiaSexVariableVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaSexVO saprodontiaSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        int validScreeningNum = statConclusionList.size();
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));

        List<DistrictSaprodontiaMonitorVO.SaprodontiaSexNum> saprodontiaSexList=Lists.newArrayList();
        genderMap.forEach((gender,list)-> saprodontiaSexList.add(getSaprodontiaSexNum(gender,list)));

        DistrictSaprodontiaMonitorVO.SaprodontiaSex saprodontiaRatioCompare = getRatioCompare(saprodontiaSexList,validScreeningNum, DistrictSaprodontiaMonitorVO.SaprodontiaSexNum::getSaprodontia);
        DistrictSaprodontiaMonitorVO.SaprodontiaSex saprodontiaLossRatioCompare = getRatioCompare(saprodontiaSexList,validScreeningNum, DistrictSaprodontiaMonitorVO.SaprodontiaSexNum::getSaprodontiaLoss);
        DistrictSaprodontiaMonitorVO.SaprodontiaSex saprodontiaRepairRatioCompare = getRatioCompare(saprodontiaSexList,validScreeningNum, DistrictSaprodontiaMonitorVO.SaprodontiaSexNum::getSaprodontiaRepair);

        DistrictSaprodontiaMonitorVO.SaprodontiaSexVariableVO saprodontiaSexVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaSexVariableVO();
        saprodontiaSexVariableVO.setSaprodontiaRatioCompare(saprodontiaRatioCompare);
        saprodontiaSexVariableVO.setSaprodontiaLossRatioCompare(saprodontiaLossRatioCompare);
        saprodontiaSexVariableVO.setSaprodontiaRepairRatioCompare(saprodontiaRepairRatioCompare);

        saprodontiaSexVO.setSaprodontiaSexVariableVO(saprodontiaSexVariableVO);
    }


    private DistrictSaprodontiaMonitorVO.SaprodontiaSex getRatioCompare(List<DistrictSaprodontiaMonitorVO.SaprodontiaSexNum> saprodontiaSexList,Integer validScreeningNum,Function<DistrictSaprodontiaMonitorVO.SaprodontiaSexNum,Integer> function) {
        if (CollectionUtil.isEmpty(saprodontiaSexList)){
            return null;
        }
        CollectionUtil.sort(saprodontiaSexList,Comparator.comparing(function));
        DistrictSaprodontiaMonitorVO.SaprodontiaSex saprodontiaSex = new DistrictSaprodontiaMonitorVO.SaprodontiaSex();
        for (int i = 0; i < saprodontiaSexList.size(); i++) {
            DistrictSaprodontiaMonitorVO.SaprodontiaSexNum saprodontiaSexNum = saprodontiaSexList.get(i);
            if (i==0){
                saprodontiaSex.setForwardSex(GenderEnum.getName(saprodontiaSexNum.getGender()));
                saprodontiaSex.setForwardRatio(MathUtil.ratio(function,saprodontiaSexNum,validScreeningNum));
            }
            if (i==1){
                saprodontiaSex.setBackSex(GenderEnum.getName(saprodontiaSexNum.getGender()));
                saprodontiaSex.setBackRatio(MathUtil.ratio(function,saprodontiaSexNum,validScreeningNum));
            }
        }
        return saprodontiaSex;
    }

    private DistrictSaprodontiaMonitorVO.SaprodontiaSexNum getSaprodontiaSexNum(Integer gender,List<StatConclusion> statConclusionList){
        int saprodontiaNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaLoss)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        DistrictSaprodontiaMonitorVO.SaprodontiaSexNum saprodontiaSexNum = new DistrictSaprodontiaMonitorVO.SaprodontiaSexNum();
        saprodontiaSexNum.setGender(gender);
        saprodontiaSexNum.setSaprodontia(saprodontiaNum);
        saprodontiaSexNum.setSaprodontiaLoss(saprodontiaLossNum);
        saprodontiaSexNum.setSaprodontiaRepair(saprodontiaRepairNum);
        return saprodontiaSexNum;
    }



    /**
     * 龋齿监测结果-不同性别-表格数据
     */
    private void getSaprodontiaSexMonitorTableList(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaSexVO saprodontiaSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList =Lists.newArrayList();
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable maleTable = getSaprodontiaSexTable(statConclusionList,GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)){
            tableList.add(maleTable);
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable femaleTable = getSaprodontiaSexTable(statConclusionList,GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)){
            tableList.add(femaleTable);
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable totalTable = getSaprodontiaSexTable(statConclusionList,10);
        if (Objects.nonNull(totalTable)){
            tableList.add(totalTable);
        }
        saprodontiaSexVO.setSaprodontiaMonitorTableList(tableList);

    }

    private DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable getSaprodontiaSexTable(List<StatConclusion> statConclusionList,Integer gender) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        List<StatConclusion> conclusionlist;

        if (Objects.equals(10,gender)){
            conclusionlist = statConclusionList;
        }else {
            conclusionlist = statConclusionList.stream().filter(sc -> Objects.equals(sc.getGender(), gender)).collect(Collectors.toList());
        }

        int validScreeningNum = conclusionlist.size();

        int saprodontiaNum = (int)conclusionlist.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)conclusionlist.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)conclusionlist.stream()
                .map(StatConclusion::getIsSaprodontiaLoss)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossAndRepairNum = (int)conclusionlist.stream()
                .filter(sc ->Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair())).count();

        Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
        ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0);
        int lossAndRepairTeethNum = conclusionlist.stream().filter(Objects::nonNull)
                .filter(lossAndRepairPredicateTrue)
                .mapToInt(lossAndRepairTotalFunction).sum();

        Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
        ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(0);
        int dmftNum = conclusionlist.stream().filter(Objects::nonNull)
                .filter(predicateTrue)
                .mapToInt(totalFunction).sum();

        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaMonitorTable= new DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable();
        if (Objects.equals(10,gender)){
            saprodontiaMonitorTable.setItemName("合计");
        }else {
            saprodontiaMonitorTable.setItemName(GenderEnum.getName(gender));
        }
        saprodontiaMonitorTable.setValidScreeningNum(validScreeningNum);
        saprodontiaMonitorTable.setDmftRatio(MathUtil.num(dmftNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaNum(saprodontiaNum);
        saprodontiaMonitorTable.setSaprodontiaRatio(MathUtil.ratio(saprodontiaNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaLossNum(saprodontiaLossNum);
        saprodontiaMonitorTable.setSaprodontiaLossRatio(MathUtil.ratio(saprodontiaLossNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaRepairNum(saprodontiaRepairNum);
        saprodontiaMonitorTable.setSaprodontiaRepairRatio(MathUtil.ratio(saprodontiaRepairNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairNum(saprodontiaLossAndRepairNum);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairRatio(MathUtil.ratio(saprodontiaLossAndRepairNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairTeethNum(lossAndRepairTeethNum);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairTeethRatio(MathUtil.ratio(lossAndRepairTeethNum,dmftNum));
        return saprodontiaMonitorTable;
    }

    /**
     * 龋齿监测结果-不同学龄段
     */
    private void getSaprodontiaSchoolAgeVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO = new DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVO();
        getSaprodontiaSchoolAgeVariableVO(statConclusionList,saprodontiaSchoolAgeVO);
        getSaprodontiaSchoolAgeMonitorTableList(statConclusionList,saprodontiaSchoolAgeVO);

        districtSaprodontiaMonitorVO.setSaprodontiaSchoolAge(saprodontiaSchoolAgeVO);

    }

    /**
     * 龋齿监测结果-不同学龄段-说明变量
     */
    private void getSaprodontiaSchoolAgeVariableVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVariableVO saprodontiaSchoolAgeVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVariableVO();

        int validScreeningNum = statConclusionList.size();
        Map<Integer, List<StatConclusion>> conclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge primary = getSaprodontiaSchoolAge(conclusionMap,SchoolAge.PRIMARY.code,validScreeningNum);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge junior = getSaprodontiaSchoolAge(conclusionMap,SchoolAge.JUNIOR.code,validScreeningNum);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge high = getSaprodontiaSchoolAge(conclusionMap,10,validScreeningNum);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge normalHigh = getSaprodontiaSchoolAge(conclusionMap,SchoolAge.HIGH.code,validScreeningNum);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge vocationalHigh = getSaprodontiaSchoolAge(conclusionMap,SchoolAge.VOCATIONAL_HIGH.code,validScreeningNum);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge university = getSaprodontiaSchoolAge(conclusionMap,SchoolAge.UNIVERSITY.code,validScreeningNum);

        saprodontiaSchoolAgeVariableVO.setPrimarySchool(primary);
        saprodontiaSchoolAgeVariableVO.setJuniorHighSchool(junior);
        saprodontiaSchoolAgeVariableVO.setUniversity(university);
        if (Objects.nonNull(vocationalHigh)){
            saprodontiaSchoolAgeVariableVO.setHighSchool(high);
            saprodontiaSchoolAgeVariableVO.setNormalHighSchool(normalHigh);
            saprodontiaSchoolAgeVariableVO.setVocationalHighSchool(vocationalHigh);
        }else {
            saprodontiaSchoolAgeVariableVO.setHighSchool(high);
        }
        saprodontiaSchoolAgeVO.setSaprodontiaSchoolAgeVariableVO(saprodontiaSchoolAgeVariableVO);
    }

    private DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge getSaprodontiaSchoolAge(Map<Integer, List<StatConclusion>> conclusionMap, Integer schoolAge,Integer validScreeningNum) {
        if (CollectionUtil.isEmpty(conclusionMap)){
            return null;
        }
        if (Objects.equals(schoolAge,10)){
            List<StatConclusion> mergeList=Lists.newArrayList();
            List<StatConclusion> normalHigh = conclusionMap.get(SchoolAge.HIGH.code);
            if (CollectionUtil.isNotEmpty(normalHigh)){
                mergeList.addAll(normalHigh);
            }
            List<StatConclusion> vocationalHigh = conclusionMap.get(SchoolAge.VOCATIONAL_HIGH.code);
            if (CollectionUtil.isNotEmpty(vocationalHigh)){
                mergeList.addAll(vocationalHigh);
            }
            return getSaprodontiaSchoolAge(mergeList,validScreeningNum);
        }
        List<StatConclusion> statConclusionList = conclusionMap.get(schoolAge);

        return getSaprodontiaSchoolAge(statConclusionList,validScreeningNum);

    }

    private DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge getSaprodontiaSchoolAge(List<StatConclusion> statConclusionList,Integer validScreeningNum){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge saprodontiaSchoolAge = new DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge();
        int saprodontiaNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaLoss)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

        saprodontiaSchoolAge.setSaprodontiaRatio(MathUtil.ratio(saprodontiaNum,validScreeningNum));
        saprodontiaSchoolAge.setSaprodontiaLossRatio(MathUtil.ratio(saprodontiaLossNum,validScreeningNum));
        saprodontiaSchoolAge.setSaprodontiaRepairRatio(MathUtil.ratio(saprodontiaRepairNum,validScreeningNum));

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, DistrictSaprodontiaMonitorVO.SaprodontiaNum> saprodontiaNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode,list)->saprodontiaNumMap.put(gradeCode,getSaprodontiaNum(list)));

        TwoTuple<String, Integer> saprodontia = getMaxMap(saprodontiaNumMap, DistrictSaprodontiaMonitorVO.SaprodontiaNum::getSaprodontia);
        TwoTuple<String, Integer> saprodontiaLoss = getMaxMap(saprodontiaNumMap, DistrictSaprodontiaMonitorVO.SaprodontiaNum::getSaprodontiaLoss);
        TwoTuple<String, Integer> saprodontiaRepair = getMaxMap(saprodontiaNumMap, DistrictSaprodontiaMonitorVO.SaprodontiaNum::getSaprodontiaRepair);

        saprodontiaSchoolAge.setMaxSaprodontiaRatio(new DistrictSaprodontiaMonitorVO.GradeRatio(saprodontia.getFirst(),MathUtil.ratio(saprodontia.getSecond(),validScreeningNum)));
        saprodontiaSchoolAge.setMaxSaprodontiaLossRatio(new DistrictSaprodontiaMonitorVO.GradeRatio(saprodontiaLoss.getFirst(),MathUtil.ratio(saprodontiaLoss.getSecond(),validScreeningNum)));
        saprodontiaSchoolAge.setMaxSaprodontiaRepairRatio(new DistrictSaprodontiaMonitorVO.GradeRatio(saprodontiaRepair.getFirst(),MathUtil.ratio(saprodontiaRepair.getSecond(),validScreeningNum)));

        return saprodontiaSchoolAge;
    }

    /**
     * 获取map中Value最大值及对应的Key
     */
    private <T,K>TwoTuple<K,Integer> getMaxMap(Map<K, T> map, Function<T,Integer> function){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,((o1, o2) -> Optional.ofNullable(o2.getValue()).map(function).orElse(0)- Optional.ofNullable(o1.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(function).orElse(0));
    }

    /**
     * 获取map中Value最小值及对应的Key
     */
    private <T,K>TwoTuple<K,Integer> getMinMap(Map<K, T> map, Function<T,Integer> function){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,Comparator.comparingInt(o -> Optional.ofNullable(o.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(function).orElse(0));
    }

    private DistrictSaprodontiaMonitorVO.SaprodontiaNum getSaprodontiaNum(List<StatConclusion> statConclusionList) {
        int saprodontiaNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaLoss)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

        return new DistrictSaprodontiaMonitorVO.SaprodontiaNum()
                .setSaprodontia(saprodontiaNum)
                .setSaprodontiaLoss(saprodontiaLossNum)
                .setSaprodontiaRepair(saprodontiaRepairNum);
    }

    /**
     * 龋齿监测结果-不同学龄段-表格数据
     */
    private void getSaprodontiaSchoolAgeMonitorTableList(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList =Lists.newArrayList();
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> primaryList = getSaprodontiaSchoolAgeTable(statConclusionList,SchoolAge.PRIMARY.code);
        if (CollectionUtil.isNotEmpty(primaryList)){
            tableList.addAll(primaryList);
        }
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> juniorList = getSaprodontiaSchoolAgeTable(statConclusionList,SchoolAge.JUNIOR.code);
        if (CollectionUtil.isNotEmpty(juniorList)){
            tableList.addAll(juniorList);
        }

        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> highList = getSaprodontiaSchoolAgeTable(statConclusionList,10);
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> normalHighList = getSaprodontiaSchoolAgeTable(statConclusionList,SchoolAge.HIGH.code);
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> vocationalHighList = getSaprodontiaSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        if (CollectionUtil.isNotEmpty(vocationalHighList)){
            tableList.addAll(highList);
            tableList.addAll(normalHighList);
            tableList.addAll(vocationalHighList);
        }else {
            tableList.addAll(highList);
        }

        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> universityList = getSaprodontiaSchoolAgeTable(statConclusionList,SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(universityList)){
            tableList.addAll(universityList);
        }

        saprodontiaSchoolAgeVO.setSaprodontiaMonitorTableList(tableList);
    }


    private List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> getSaprodontiaSchoolAgeTable(List<StatConclusion> statConclusionList,Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        if (Objects.equals(schoolAge,10)){
            List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> mergeList=Lists.newArrayList();
            List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> normalHighList = getSaprodontiaGrade(statConclusionList, SchoolAge.HIGH.code);
            if (CollectionUtil.isNotEmpty(normalHighList)){
                mergeList.addAll(normalHighList);
            }
            List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> vocationalHighList = getSaprodontiaGrade(statConclusionList, SchoolAge.HIGH.code);
            if (CollectionUtil.isNotEmpty(vocationalHighList)){
                mergeList.addAll(vocationalHighList);
            }
            return mergeList;
        }

        return getSaprodontiaGrade(statConclusionList,schoolAge);
    }

    private List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> getSaprodontiaGrade(List<StatConclusion> statConclusionList,Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        int validScreeningNum = statConclusionList.size();
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        Map<String, List<StatConclusion>> gradeCodeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        MapUtil.sort(gradeCodeMap);
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> gradeList = Lists.newArrayList();
        gradeCodeMap.forEach((grade,list)-> getSaprodontiaSchoolAgeTable(list,grade,validScreeningNum,gradeList));
        getSaprodontiaSchoolAgeTable(conclusionList,SchoolAge.get(schoolAge).desc,validScreeningNum,gradeList);

        return gradeList;
    }
    private void getSaprodontiaSchoolAgeTable(List<StatConclusion> statConclusionList,String grade,int validScreeningNum,List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        int saprodontiaNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaLoss)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossAndRepairNum = (int)statConclusionList.stream()
                .filter(sc ->Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair())).count();

        Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
        ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0);
        int lossAndRepairTeethNum = statConclusionList.stream().filter(Objects::nonNull)
                .filter(lossAndRepairPredicateTrue)
                .mapToInt(lossAndRepairTotalFunction).sum();

        Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
        ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(0);
        int dmftNum = statConclusionList.stream().filter(Objects::nonNull)
                .filter(predicateTrue)
                .mapToInt(totalFunction).sum();

        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaMonitorTable= new DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable();
        saprodontiaMonitorTable.setItemName(grade);
        saprodontiaMonitorTable.setValidScreeningNum(validScreeningNum);
        saprodontiaMonitorTable.setDmftRatio(MathUtil.num(dmftNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaNum(saprodontiaNum);
        saprodontiaMonitorTable.setSaprodontiaRatio(MathUtil.ratio(saprodontiaNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaLossNum(saprodontiaLossNum);
        saprodontiaMonitorTable.setSaprodontiaLossRatio(MathUtil.ratio(saprodontiaLossNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaRepairNum(saprodontiaRepairNum);
        saprodontiaMonitorTable.setSaprodontiaRepairRatio(MathUtil.ratio(saprodontiaRepairNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairNum(saprodontiaLossAndRepairNum);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairRatio(MathUtil.ratio(saprodontiaLossAndRepairNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairTeethNum(lossAndRepairTeethNum);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairTeethRatio(MathUtil.ratio(lossAndRepairTeethNum,dmftNum));

        gradeList.add(saprodontiaMonitorTable);
    }

    /**
     * 龋齿监测结果-不同年龄
     */
    private void getSaprodontiaAgeVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaAgeVO saprodontiaAgeVO = new DistrictSaprodontiaMonitorVO.SaprodontiaAgeVO();
        getSaprodontiaAgeVariableVO(statConclusionList,saprodontiaAgeVO);
        getSaprodontiaAgeMonitorTableList(statConclusionList,saprodontiaAgeVO);

        districtSaprodontiaMonitorVO.setSaprodontiaAgeVO(saprodontiaAgeVO);
    }

    /**
     * 龋齿监测结果-不同年龄-说明变量
     */
    private void getSaprodontiaAgeVariableVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaAgeVO saprodontiaAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        int validScreeningNum = statConclusionList.size();

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> getLessAge(sc.getAge())));
        Map<Integer, DistrictSaprodontiaMonitorVO.SaprodontiaNum> saprodontiaNumMap = Maps.newHashMap();
        ageMap.forEach((age,list)->saprodontiaNumMap.put(age,getSaprodontiaNum(list)));

        DistrictSaprodontiaMonitorVO.AgeRatio saprodontia = getAgeRatio(saprodontiaNumMap, DistrictSaprodontiaMonitorVO.SaprodontiaNum::getSaprodontia,validScreeningNum);
        DistrictSaprodontiaMonitorVO.AgeRatio saprodontiaLoss = getAgeRatio(saprodontiaNumMap, DistrictSaprodontiaMonitorVO.SaprodontiaNum::getSaprodontiaLoss,validScreeningNum);
        DistrictSaprodontiaMonitorVO.AgeRatio saprodontiaRepair = getAgeRatio(saprodontiaNumMap, DistrictSaprodontiaMonitorVO.SaprodontiaNum::getSaprodontiaRepair,validScreeningNum);

        DistrictSaprodontiaMonitorVO.SaprodontiaAgeVariableVO saprodontiaAgeVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaAgeVariableVO();
        saprodontiaAgeVariableVO.setSaprodontiaRatio(saprodontia);
        saprodontiaAgeVariableVO.setSaprodontiaLossRatio(saprodontiaLoss);
        saprodontiaAgeVariableVO.setSaprodontiaRepairRatio(saprodontiaRepair);

        saprodontiaAgeVO.setSaprodontiaAgeVariableVO(saprodontiaAgeVariableVO);
    }

    private DistrictSaprodontiaMonitorVO.AgeRatio getAgeRatio(Map<Integer, DistrictSaprodontiaMonitorVO.SaprodontiaNum> saprodontiaNumMap, Function<DistrictSaprodontiaMonitorVO.SaprodontiaNum,Integer> function,int validScreeningNum) {
        TwoTuple<Integer, Integer> maxTuple = getMaxMap(saprodontiaNumMap, function);
        TwoTuple<Integer, Integer> minTuple = getMinMap(saprodontiaNumMap, function);
        DistrictSaprodontiaMonitorVO.AgeRatio ageRatio = new DistrictSaprodontiaMonitorVO.AgeRatio();
        ageRatio.setMaxAge(AgeSegmentEnum.get(maxTuple.getFirst()).getDesc());
        ageRatio.setMinAge(AgeSegmentEnum.get(minTuple.getFirst()).getDesc());
        ageRatio.setMaxRatio(MathUtil.ratio(maxTuple.getSecond(),validScreeningNum));
        ageRatio.setMinRatio(MathUtil.ratio(minTuple.getSecond(),validScreeningNum));
        return ageRatio;
    }

    private Integer getLessAge(Integer age){
        if (age < 6){
            return 6;
        }else if (age < 8){
            return 8;
        }else if (age < 10){
           return 10;
        }else if (age < 12){
            return 12;
        }else if (age < 14){
            return 14;
        }else if (age < 16){
            return 16;
        }else if (age < 18){
            return 18;
        }else {
            return 19;
        }
    }


    /**
     * 龋齿监测结果-不同年龄-表格数据
     */
    private void getSaprodontiaAgeMonitorTableList(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaAgeVO saprodontiaAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> getLessAge(sc.getAge()),TreeMap::new,Collectors.toList()));
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList = Lists.newArrayList();
        ageMap.forEach((age,list)->getSaprodontiaAgeTable(age,list,tableList));
        getSaprodontiaAgeTable(1000,statConclusionList,tableList);
        saprodontiaAgeVO.setSaprodontiaMonitorTableList(tableList);

    }

    private void getSaprodontiaAgeTable(Integer age, List<StatConclusion> conclusionlist, List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList) {
        if (CollectionUtil.isEmpty(conclusionlist)){
            return;
        }

        String itemName;
        if (age == 1000){
            itemName = "合计";
        }else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }

        int validScreeningNum = conclusionlist.size();

        int saprodontiaNum = (int)conclusionlist.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)conclusionlist.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)conclusionlist.stream()
                .map(StatConclusion::getIsSaprodontiaLoss)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossAndRepairNum = (int)conclusionlist.stream()
                .filter(sc ->Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair())).count();

        Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
        ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0);
        int lossAndRepairTeethNum = conclusionlist.stream().filter(Objects::nonNull)
                .filter(lossAndRepairPredicateTrue)
                .mapToInt(lossAndRepairTotalFunction).sum();

        Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
        ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(0);
        int dmftNum = conclusionlist.stream().filter(Objects::nonNull)
                .filter(predicateTrue)
                .mapToInt(totalFunction).sum();


        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaAgeMonitorTable = new DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable();
        saprodontiaAgeMonitorTable.setItemName(itemName);
        saprodontiaAgeMonitorTable.setSaprodontiaNum(saprodontiaNum);
        saprodontiaAgeMonitorTable.setSaprodontiaRatio(MathUtil.ratio(saprodontiaNum,validScreeningNum));
        saprodontiaAgeMonitorTable.setSaprodontiaLossNum(saprodontiaLossNum);
        saprodontiaAgeMonitorTable.setSaprodontiaLossRatio(MathUtil.ratio(saprodontiaLossNum,validScreeningNum));
        saprodontiaAgeMonitorTable.setSaprodontiaRepairNum(saprodontiaRepairNum);
        saprodontiaAgeMonitorTable.setSaprodontiaRepairRatio(MathUtil.ratio(saprodontiaRepairNum,validScreeningNum));
        saprodontiaAgeMonitorTable.setSaprodontiaLossAndRepairNum(saprodontiaLossAndRepairNum);
        saprodontiaAgeMonitorTable.setSaprodontiaLossAndRepairRatio(MathUtil.ratio(saprodontiaLossAndRepairNum,validScreeningNum));
        saprodontiaAgeMonitorTable.setSaprodontiaLossAndRepairTeethNum(lossAndRepairTeethNum);
        saprodontiaAgeMonitorTable.setSaprodontiaLossAndRepairTeethRatio(MathUtil.ratio(lossAndRepairTeethNum,dmftNum));
        saprodontiaAgeMonitorTable.setDmftRatio(MathUtil.num(dmftNum,validScreeningNum));

        tableList.add(saprodontiaAgeMonitorTable);
    }

    /**
     * 疾病监测情况
     */
    private void getDistrictDiseaseMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictDiseaseMonitorVO districtDiseaseMonitorVO = new DistrictDiseaseMonitorVO();

        getDiseaseMonitorVariableVO(statConclusionList,districtDiseaseMonitorVO);
        getDiseaseMonitorTableList(statConclusionList,districtDiseaseMonitorVO);

        districtCommonDiseasesAnalysisVO.setDistrictDiseaseMonitorVO(districtDiseaseMonitorVO);

    }

    /**
     * 疾病监测情况-不同学龄段
     */
    private void getDiseaseMonitorTableList(List<StatConclusion> statConclusionList, DistrictDiseaseMonitorVO districtDiseaseMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        List<DistrictDiseaseMonitorVO.DiseaseMonitorTable> tableList = Lists.newArrayList();

        Map<Integer, List<StatConclusion>> conclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));
        DistrictDiseaseMonitorVO.DiseaseMonitorTable primaryTable = getSchoolAgeTable(conclusionMap,SchoolAge.PRIMARY.code);
        DistrictDiseaseMonitorVO.DiseaseMonitorTable juniorTable = getSchoolAgeTable(conclusionMap,SchoolAge.JUNIOR.code);
        DistrictDiseaseMonitorVO.DiseaseMonitorTable highTable = getSchoolAgeTable(conclusionMap,10);
        DistrictDiseaseMonitorVO.DiseaseMonitorTable normalHighTable = getSchoolAgeTable(conclusionMap,SchoolAge.HIGH.code);
        DistrictDiseaseMonitorVO.DiseaseMonitorTable vocationalHighTable = getSchoolAgeTable(conclusionMap,SchoolAge.VOCATIONAL_HIGH.code);
        DistrictDiseaseMonitorVO.DiseaseMonitorTable universityTable = getSchoolAgeTable(conclusionMap,SchoolAge.UNIVERSITY.code);
        if (Objects.nonNull(primaryTable)){
            tableList.add(primaryTable);
        }
        if (Objects.nonNull(juniorTable)){
            tableList.add(juniorTable);
        }
        //高中（普高+职高）
        if (Objects.nonNull(vocationalHighTable)){
            tableList.add(highTable);
            tableList.add(normalHighTable);
            tableList.add(vocationalHighTable);
        }else {
            tableList.add(highTable);
        }

        if (Objects.nonNull(universityTable)){
            tableList.add(universityTable);
        }

        DistrictDiseaseMonitorVO.DiseaseMonitorTable totalTable = getRecord(statConclusionList,"合计");
        if (Objects.nonNull(totalTable)){
            tableList.add(totalTable);
        }
        districtDiseaseMonitorVO.setDiseaseMonitorTableList(tableList);
    }


    private DistrictDiseaseMonitorVO.DiseaseMonitorTable getSchoolAgeTable(Map<Integer, List<StatConclusion>> conclusionMap,Integer schoolAge){
        if (CollectionUtil.isEmpty(conclusionMap)){
            return null;
        }

        if (Objects.equals(schoolAge,10)){
            List<StatConclusion> mergeList=Lists.newArrayList();
            List<StatConclusion> highList = conclusionMap.get(SchoolAge.HIGH.code);
            if(CollectionUtil.isNotEmpty(highList)){
                mergeList.addAll(highList);
            }
            List<StatConclusion> vocationalHighList = conclusionMap.get(SchoolAge.VOCATIONAL_HIGH.code);
            if(CollectionUtil.isNotEmpty(vocationalHighList)){
                mergeList.addAll(vocationalHighList);
            }
            if (CollectionUtil.isEmpty(mergeList)){
                return null;
            }
            return getRecord(mergeList, "高中");
        }

        List<StatConclusion> statConclusionList = conclusionMap.get(schoolAge);
        if(CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        if (Objects.equals(schoolAge,SchoolAge.HIGH.code)){
            DistrictDiseaseMonitorVO.DiseaseMonitorTable normalHighTable = getRecord(statConclusionList, "普高");
            if (Objects.isNull(normalHighTable)){
                normalHighTable = new DistrictDiseaseMonitorVO.DiseaseMonitorTable();
                normalHighTable.setSchoolAge("普高");
            }
            return normalHighTable;
        }
        if (Objects.equals(schoolAge,SchoolAge.VOCATIONAL_HIGH.code)){
            return getRecord(statConclusionList, "职高");
        }

        return getRecord(statConclusionList, SchoolAge.get(schoolAge).desc);
    }

    private DistrictDiseaseMonitorVO.DiseaseMonitorTable getRecord(List<StatConclusion> statConclusionList,String schoolAgeDesc) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        DistrictDiseaseMonitorVO.DiseaseMonitorTable diseaseMonitorTable = new DistrictDiseaseMonitorVO.DiseaseMonitorTable();
        int validScreeningNum = statConclusionList.size();

        DistrictDiseaseMonitorVO.Ratio hypertensionRatio = getRatio(statConclusionList,validScreeningNum,DiseaseNumDO::getHypertension);
        DistrictDiseaseMonitorVO.Ratio anemiaRatio = getRatio(statConclusionList,validScreeningNum,DiseaseNumDO::getAnemia);
        DistrictDiseaseMonitorVO.Ratio diabetesRatio = getRatio(statConclusionList,validScreeningNum,DiseaseNumDO::getDiabetes);
        DistrictDiseaseMonitorVO.Ratio allergicAsthmaRatio = getRatio(statConclusionList,validScreeningNum,DiseaseNumDO::getAllergicAsthma);
        DistrictDiseaseMonitorVO.Ratio physicalDisabilityRatio = getRatio(statConclusionList,validScreeningNum,DiseaseNumDO::getPhysicalDisability);

        diseaseMonitorTable.setSchoolAge(schoolAgeDesc);
        diseaseMonitorTable.setValidScreeningNum(validScreeningNum);
        diseaseMonitorTable.setHypertension(hypertensionRatio);
        diseaseMonitorTable.setAnemia(anemiaRatio);
        diseaseMonitorTable.setDiabetes(diabetesRatio);
        diseaseMonitorTable.setAllergicAsthma(allergicAsthmaRatio);
        diseaseMonitorTable.setPhysicalDisability(physicalDisabilityRatio);

        return diseaseMonitorTable;
    }

    private DistrictDiseaseMonitorVO.Ratio getRatio(List<StatConclusion> primaryAndAboveStatConclusionList, int validScreeningNum,Function<DiseaseNumDO,Integer> function) {
        if (CollectionUtil.isEmpty(primaryAndAboveStatConclusionList)){
            return null;
        }
        DistrictDiseaseMonitorVO.Ratio ratio = new DistrictDiseaseMonitorVO.Ratio();
        int num = primaryAndAboveStatConclusionList.stream()
                .filter(sc -> Objects.nonNull(sc.getDiseaseNum()))
                .mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum()).map(function).orElse(0)).sum();
        ratio.setNum(num);
        ratio.setRatio(MathUtil.ratio(num,validScreeningNum));
        return null;
    }

    /**
     * 疾病监测情况-说明变量
     */
    private void getDiseaseMonitorVariableVO(List<StatConclusion> statConclusionList, DistrictDiseaseMonitorVO districtDiseaseMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictDiseaseMonitorVO.DiseaseMonitorVariableVO diseaseMonitorVariableVO = new DistrictDiseaseMonitorVO.DiseaseMonitorVariableVO();
        int validScreeningNum = statConclusionList.size();
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.nonNull(sc.getDiseaseNum())).collect(Collectors.toList());

        int hypertensionNum = conclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getHypertension()).orElse(0)).sum();
        int anemiaNum = conclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getAnemia()).orElse(0)).sum();
        int diabetesNum = conclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getDiabetes()).orElse(0)).sum();
        int allergicAsthmaNum = conclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getAllergicAsthma()).orElse(0)).sum();
        int physicalDisabilityNum = conclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getPhysicalDisability()).orElse(0)).sum();

        diseaseMonitorVariableVO.setHypertensionRatio(MathUtil.ratio(hypertensionNum,validScreeningNum));
        diseaseMonitorVariableVO.setAnemiaRatio(MathUtil.ratio(anemiaNum,validScreeningNum));
        diseaseMonitorVariableVO.setDiabetesRatio(MathUtil.ratio(diabetesNum,validScreeningNum));
        diseaseMonitorVariableVO.setAllergicAsthmaRatio(MathUtil.ratio(allergicAsthmaNum,validScreeningNum));
        diseaseMonitorVariableVO.setPhysicalDisabilityRatio(MathUtil.ratio(physicalDisabilityNum,validScreeningNum));

        //最高值
        Map<Integer, List<StatConclusion>> schoolAgeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));

        Map<Integer, DiseaseNumDO> diseaseNumDOMap = Maps.newHashMap();
        schoolAgeMap.forEach((schoolAge,list)-> diseaseNumDOMap.put(schoolAge,getDiseaseNumDO(list)));

        TwoTuple<Integer, Integer> hypertensionTuple = getMaxKeyAndValue(diseaseNumDOMap, DiseaseNumDO::getHypertension);
        TwoTuple<Integer, Integer> anemiaTuple = getMaxKeyAndValue(diseaseNumDOMap, DiseaseNumDO::getAnemia);
        TwoTuple<Integer, Integer> diabetesTuple = getMaxKeyAndValue(diseaseNumDOMap, DiseaseNumDO::getDiabetes);
        TwoTuple<Integer, Integer> allergicAsthmaTuple = getMaxKeyAndValue(diseaseNumDOMap, DiseaseNumDO::getAllergicAsthma);
        TwoTuple<Integer, Integer> physicalDisabilityTuple = getMaxKeyAndValue(diseaseNumDOMap, DiseaseNumDO::getPhysicalDisability);

        diseaseMonitorVariableVO.setMaxHypertensionRatio(getSchoolAgeRatio(hypertensionTuple,validScreeningNum));
        diseaseMonitorVariableVO.setMaxAnemiaRatio(getSchoolAgeRatio(anemiaTuple,validScreeningNum));
        diseaseMonitorVariableVO.setMaxDiabetesRatio(getSchoolAgeRatio(diabetesTuple,validScreeningNum));
        diseaseMonitorVariableVO.setMaxAllergicAsthmaRatio(getSchoolAgeRatio(allergicAsthmaTuple,validScreeningNum));
        diseaseMonitorVariableVO.setMaxPhysicalDisabilityRatio(getSchoolAgeRatio(physicalDisabilityTuple,validScreeningNum));

        districtDiseaseMonitorVO.setDiseaseMonitorVariableVO(diseaseMonitorVariableVO);
    }

    private DistrictDiseaseMonitorVO.SchoolAgeRatio getSchoolAgeRatio(TwoTuple<Integer, Integer> tuple,Integer validScreeningNum){
        DistrictDiseaseMonitorVO.SchoolAgeRatio schoolAgeRatio = new DistrictDiseaseMonitorVO.SchoolAgeRatio();
        if (Objects.nonNull(tuple)){
            schoolAgeRatio.setSchoolAge(SchoolAge.get(tuple.getFirst()).desc);
            schoolAgeRatio.setRatio(MathUtil.ratio(tuple.getSecond(),validScreeningNum));
        }
        return schoolAgeRatio;
    }

    /**
     * 疾病监测情况-说明变量-获取map中Value最大值及对应的Key
     */
    private TwoTuple<Integer,Integer> getMaxKeyAndValue(Map<Integer, DiseaseNumDO> map, Function<DiseaseNumDO,Integer> function){
        List<Map.Entry<Integer, DiseaseNumDO>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,((o1, o2) -> Optional.ofNullable(o2.getValue()).map(function).orElse(0)- Optional.ofNullable(o1.getValue()).map(function).orElse(0)));
        Map.Entry<Integer, DiseaseNumDO> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(function).orElse(0));
    }

    /**
     * 获取疾病数统计
     */
    private DiseaseNumDO getDiseaseNumDO(List<StatConclusion> statConclusionList){
        int hypertensionNum = statConclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getHypertension()).orElse(0)).sum();
        int anemiaNum = statConclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getAnemia()).orElse(0)).sum();
        int diabetesNum = statConclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getDiabetes()).orElse(0)).sum();
        int allergicAsthmaNum = statConclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getAllergicAsthma()).orElse(0)).sum();
        int physicalDisabilityNum = statConclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getPhysicalDisability()).orElse(0)).sum();
        return new DiseaseNumDO().setHypertension(hypertensionNum)
                .setAnemia(anemiaNum).setDiabetes(diabetesNum)
                .setAllergicAsthma(allergicAsthmaNum).setPhysicalDisability(physicalDisabilityNum);

    }

    /**
     * 常见病分析变量
     */
    private void getCommonDiseasesAnalysisVariableVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictCommonDiseasesAnalysisVO.CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO = new DistrictCommonDiseasesAnalysisVO.CommonDiseasesAnalysisVariableVO();

        getSaprodontiaVO(statConclusionList,commonDiseasesAnalysisVariableVO);
        getHeightAndWeightVO(statConclusionList,commonDiseasesAnalysisVariableVO);
        getBloodPressureAndSpinalCurvatureVO(statConclusionList,commonDiseasesAnalysisVariableVO);
        commonDiseasesAnalysisVariableVO.setValidScreeningNum(statConclusionList.size());

        districtCommonDiseasesAnalysisVO.setCommonDiseasesAnalysisVariableVO(commonDiseasesAnalysisVariableVO);

    }

    /**
     * 常见病分析变量-龋齿数据
     */
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

    /**
     * 常见病分析变量-身高体重数据
     */
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

    /**
     * 常见病分析变量-血压和脊柱弯曲数据
     */
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

}
