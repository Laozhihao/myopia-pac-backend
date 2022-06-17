package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wupol.framework.core.util.CompareUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.CommonDiseasesAnalysisVariableVO;
import com.wupol.myopia.business.api.management.domain.vo.report.CommonDiseasesNum;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseaseReportVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public DistrictCommonDiseaseReportVO districtCommonDiseaseReport(Integer districtId, Integer noticeId) {
        DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO = new DistrictCommonDiseaseReportVO();

        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        if (!districtIds.contains(districtId)){
            districtIds.add(districtId);
        }

        List<StatConclusion> statConclusionList = getStatConclusionList(noticeId, Lists.newArrayList(districtIds), Boolean.TRUE, Boolean.FALSE);

        //全局变量
        getGlobalVariableVO(districtId,districtIds, noticeId, districtCommonDiseaseReportVO);
        //筛查人数和实际筛查人数
        setNum(noticeId,districtIds, districtCommonDiseaseReportVO);
        //视力分析
        getVisionAnalysisVO(statConclusionList, districtCommonDiseaseReportVO);
        //常见病分析
        getDistrictCommonDiseasesAnalysisVO(statConclusionList, districtCommonDiseaseReportVO);

        return districtCommonDiseaseReportVO;
    }

    /**
     * 全局变量
     */
    private void getGlobalVariableVO(Integer districtId,List<Integer> districtIds, Integer noticeId, DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        DistrictCommonDiseaseReportVO.GlobalVariableVO globalVariableVO = new DistrictCommonDiseaseReportVO.GlobalVariableVO();
        String format = "YYYY";

        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        if (Objects.isNull(screeningNotice)) {
            throw new BusinessException(String.format("不存在筛查通知: noticeId=%s", noticeId));
        }
        List<ScreeningPlan> screeningPlanList = screeningPlanService.getAllPlanByNoticeId(noticeId);
        if (CollectionUtil.isEmpty(screeningPlanList)) {
            throw new BusinessException(String.format("该筛查通知不存在筛查计划: noticeId=%s", noticeId));
        }

        Set<Integer> planIds = screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByPlanIds(Lists.newArrayList(planIds));
        if (CollectionUtil.isEmpty(screeningResults)) {
            districtCommonDiseaseReportVO.setGlobalVariableVO(globalVariableVO);
            return;
        }

        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByScreeningPlanIds(Lists.newArrayList(planIds));
        if (CollectionUtil.isEmpty(planSchoolStudentList)) {
            districtCommonDiseaseReportVO.setGlobalVariableVO(globalVariableVO);
            return;
        }

        Map<Integer, List<ScreeningPlanSchoolStudent>> planSchoolStudentDistrictMap = planSchoolStudentList.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolDistrictId));
        List<Integer> haveStudentDistrictIds = CompareUtil.getRetain(districtIds, planSchoolStudentDistrictMap.keySet());
        List<ScreeningPlanSchoolStudent> planStudentCountList = getScreeningStudentList(haveStudentDistrictIds,planSchoolStudentDistrictMap);

        long totalSum = planStudentCountList.stream().map(ScreeningPlanSchoolStudent::getSchoolId).distinct().count();
        Map<Integer, List<ScreeningPlanSchoolStudent>> planSchoolStudentMap = planStudentCountList.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getGradeType));

        setSchoolItemData(planSchoolStudentMap, globalVariableVO);

        //获取行政区域
        String districtName = districtService.getDistrictNameByDistrictId(districtId);
        Set<String> years = Sets.newHashSet(DateUtil.format(screeningNotice.getStartTime(), format),DateUtil.format(screeningNotice.getEndTime(), format));
        String year = CollectionUtil.join(years, StrUtil.DASHED);
        if (years.size() == 1) {
            List<String> yearPeriod = Lists.newArrayList(DateUtil.format(screeningNotice.getStartTime(), DatePattern.CHINESE_DATE_PATTERN),
                    DateUtil.format(screeningNotice.getEndTime(), "MM月dd日"));
            String screeningTimePeriod = CollectionUtil.join(yearPeriod, StrUtil.DASHED);
            globalVariableVO.setScreeningTimePeriod(screeningTimePeriod);
            globalVariableVO.setDataYear(year);
        } else {
            List<String> yearPeriod = Lists.newArrayList(DateUtil.format(screeningNotice.getStartTime(), DatePattern.CHINESE_DATE_PATTERN),
                    DateUtil.format(screeningNotice.getEndTime(), DatePattern.CHINESE_DATE_PATTERN));
            String screeningTimePeriod = CollectionUtil.join(yearPeriod, StrUtil.DASHED);
            globalVariableVO.setScreeningTimePeriod(screeningTimePeriod);

            VisionScreeningResult visionScreeningResult = screeningResults.get(0);
            String dataYear = DateUtil.format(visionScreeningResult.getCreateTime(), format);
            globalVariableVO.setDataYear(dataYear);
        }

        globalVariableVO.setAreaName(districtName);
        globalVariableVO.setYear(year);
        globalVariableVO.setTotalSchoolNum((int) totalSum);
        districtCommonDiseaseReportVO.setGlobalVariableVO(globalVariableVO);
    }


    /**
     * 学龄及学校数
     */
    private void setSchoolItemData(Map<Integer, List<ScreeningPlanSchoolStudent>> planSchoolStudentMap, DistrictCommonDiseaseReportVO.GlobalVariableVO globalVariableVO) {
        String format = "%s所%s";
        List<String> itemList = Lists.newArrayList();
        List<ScreeningPlanSchoolStudent> primary = planSchoolStudentMap.get(SchoolAge.PRIMARY.code);
        if (CollectionUtil.isNotEmpty(primary)) {
            long count = primary.stream().map(ScreeningPlanSchoolStudent::getSchoolId).filter(Objects::nonNull).distinct().count();
            itemList.add(String.format(format, count, SchoolAge.PRIMARY.desc));
        }
        List<ScreeningPlanSchoolStudent> junior = planSchoolStudentMap.get(SchoolAge.JUNIOR.code);
        if (CollectionUtil.isNotEmpty(junior)) {
            long count = junior.stream().map(ScreeningPlanSchoolStudent::getSchoolId).filter(Objects::nonNull).distinct().count();
            itemList.add(String.format(format, count, SchoolAge.JUNIOR.desc));
        }
        List<ScreeningPlanSchoolStudent> normalHigh = planSchoolStudentMap.get(SchoolAge.HIGH.code);
        List<ScreeningPlanSchoolStudent> vocationalHigh = planSchoolStudentMap.get(SchoolAge.VOCATIONAL_HIGH.code);


        if (CollectionUtil.isNotEmpty(normalHigh)) {
            long normalHighCount = normalHigh.stream().map(ScreeningPlanSchoolStudent::getSchoolId).filter(Objects::nonNull).distinct().count();

            if (CollectionUtil.isNotEmpty(vocationalHigh)) {
                long vocationalHighCount = vocationalHigh.stream().map(ScreeningPlanSchoolStudent::getSchoolId).filter(Objects::nonNull).distinct().count();
                String high= String.format(format, normalHighCount+vocationalHighCount, SchoolAge.HIGH.desc);
                String normalHighStr = String.format(format, normalHighCount, SchoolAge.HIGH.desc);
                String vocationalHighStr = String.format(format, vocationalHighCount, SchoolAge.VOCATIONAL_HIGH.desc);
                high =  high+"（"+normalHighStr+"，"+vocationalHighStr +"）";
                itemList.add(high);
            }else {
                String highStr = String.format(format, normalHighCount, ReportConst.HIGH);
                itemList.add(highStr);
            }

        }else {
            if (CollectionUtil.isNotEmpty(vocationalHigh)) {
                long vocationalHighCount = vocationalHigh.stream().map(ScreeningPlanSchoolStudent::getSchoolId).filter(Objects::nonNull).distinct().count();
                String highStr = String.format(format, vocationalHighCount, ReportConst.HIGH);
                itemList.add(highStr);
            }
        }

        List<ScreeningPlanSchoolStudent> university = planSchoolStudentMap.get(SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(university)) {
            long count = university.stream().map(ScreeningPlanSchoolStudent::getSchoolId).filter(Objects::nonNull).distinct().count();
            itemList.add(String.format(format, count, "综合性" + SchoolAge.UNIVERSITY.desc));
        }
        List<ScreeningPlanSchoolStudent> kindergarten = planSchoolStudentMap.get(SchoolAge.KINDERGARTEN.code);
        if (CollectionUtil.isNotEmpty(kindergarten)) {
            long count = kindergarten.stream().map(ScreeningPlanSchoolStudent::getSchoolId).filter(Objects::nonNull).distinct().count();
            itemList.add(String.format(format, count, SchoolAge.KINDERGARTEN.desc));
        }

        if (CollectionUtil.isNotEmpty(itemList)) {
            globalVariableVO.setSchoolItem(itemList);
        }

    }

    private List<ScreeningPlanSchoolStudent> getScreeningStudentList(List<Integer> haveStudentDistrictIds,Map<Integer, List<ScreeningPlanSchoolStudent>> planStudentCountMap){
        return haveStudentDistrictIds.stream().distinct().flatMap(id -> {
            List<ScreeningPlanSchoolStudent> planSchoolStudentList = planStudentCountMap.get(id);
            if (CollectionUtil.isNotEmpty(planSchoolStudentList)) {
                return planSchoolStudentList.stream();
            } else {
                return Stream.empty();
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 筛查人数和实际筛查人数
     */
    private void setNum(Integer noticeId,List<Integer> districtIds, DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        Map<Integer, List<ScreeningPlanSchoolStudent>> planStudentCountMap = screeningPlanSchoolStudentService.getPlanStudentCountBySrcScreeningNoticeId(noticeId);

        List<Integer> haveStudentDistrictIds = CompareUtil.getRetain(districtIds, planStudentCountMap.keySet());

        List<ScreeningPlanSchoolStudent> planStudentCountList = getScreeningStudentList(haveStudentDistrictIds,planStudentCountMap);

        List<Integer> planSchoolStudentIds = planStudentCountList.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toList());
        Set<Integer> planIds = planStudentCountList.stream().map(ScreeningPlanSchoolStudent::getScreeningPlanId).collect(Collectors.toSet());

        districtCommonDiseaseReportVO.setScreeningStudentNum(planStudentCountList.size());

        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByPlanIds(Lists.newArrayList(planIds));
        if (CollectionUtil.isEmpty(screeningResults)) {
            districtCommonDiseaseReportVO.setActualScreeningNum(0);
        } else {
            long count = screeningResults.stream()
                    .filter(sr -> Objects.equals(sr.getIsDoubleScreen(), Boolean.FALSE))
                    .filter(sr->planSchoolStudentIds.contains(sr.getScreeningPlanSchoolStudentId()))
                    .count();
            districtCommonDiseaseReportVO.setActualScreeningNum((int)count);
        }

    }

    /**
     * 视力分析
     */
    private void getVisionAnalysisVO(List<StatConclusion> statConclusionList, DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        DistrictCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO = new DistrictCommonDiseaseReportVO.VisionAnalysisVO();
        int validScreeningNum = statConclusionList.size();
        visionAnalysisVO.setValidScreeningNum(validScreeningNum);
        List<StatConclusion> kindergartenList = statConclusionList.stream().filter(statConclusion -> Objects.equals(statConclusion.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());
        List<StatConclusion> primarySchoolAndAboveList = statConclusionList.stream().filter(statConclusion -> !Objects.equals(statConclusion.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());
        getKindergartenVO(kindergartenList, validScreeningNum, visionAnalysisVO);
        getPrimarySchoolAndAboveVO(primarySchoolAndAboveList, validScreeningNum, visionAnalysisVO);

        districtCommonDiseaseReportVO.setVisionAnalysisVO(visionAnalysisVO);
    }

    private void getKindergartenVO(List<StatConclusion> kindergartenList, int validScreeningNum, DistrictCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO) {
        if (CollectionUtil.isEmpty(kindergartenList)) {
            return;
        }
        DistrictCommonDiseaseReportVO.KindergartenVO kindergartenVO = new DistrictCommonDiseaseReportVO.KindergartenVO();
        int lowVisionNum = (int) kindergartenList.stream()
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
        kindergartenVO.setLowVisionRatio(MathUtil.ratioNotSymbol(lowVisionNum, validScreeningNum));
        kindergartenVO.setMyopiaLevelInsufficientRatio(MathUtil.ratioNotSymbol(visionLabelZeroSpNum, validScreeningNum));
        kindergartenVO.setAnisometropiaRatio(MathUtil.ratioNotSymbol(anisometropiaNum, validScreeningNum));

        visionAnalysisVO.setKindergartenVO(kindergartenVO);
    }


    private void getPrimarySchoolAndAboveVO(List<StatConclusion> primarySchoolAndAboveList, int validScreeningNum, DistrictCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO) {
        if (CollectionUtil.isEmpty(primarySchoolAndAboveList)) {
            return;
        }
        DistrictCommonDiseaseReportVO.PrimarySchoolAndAboveVO primarySchoolAndAboveVO = new DistrictCommonDiseaseReportVO.PrimarySchoolAndAboveVO();
        int lowVisionNum = (int) primarySchoolAndAboveList.stream()
                .map(StatConclusion::getIsLowVision)
                .filter(Objects::nonNull).filter(Boolean::booleanValue)
                .count();
        int myopiaNum = (int) primarySchoolAndAboveList.stream()
                .map(StatConclusion::getIsMyopia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue)
                .count();

        Map<Integer, List<StatConclusion>> schoolMap = primarySchoolAndAboveList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));

        TwoTuple<BigDecimal, BigDecimal> averageVisionTuple = StatUtil.calculateAverageVision(primarySchoolAndAboveList);
        BigDecimal add = averageVisionTuple.getFirst().add(averageVisionTuple.getSecond());
        BigDecimal averageVision = BigDecimalUtil.divide(add, new BigDecimal("2"), 1);
        primarySchoolAndAboveVO.setAvgVision(averageVision);
        primarySchoolAndAboveVO.setLowVisionRatio(MathUtil.ratioNotSymbol(lowVisionNum, validScreeningNum));
        primarySchoolAndAboveVO.setMyopiaRatio(MathUtil.ratioNotSymbol(myopiaNum, validScreeningNum));

        setSchoolAge(validScreeningNum, primarySchoolAndAboveVO, schoolMap);

        visionAnalysisVO.setPrimarySchoolAndAboveVO(primarySchoolAndAboveVO);
    }

    private void setSchoolAge(int validScreeningNum, DistrictCommonDiseaseReportVO.PrimarySchoolAndAboveVO primarySchoolAndAboveVO, Map<Integer, List<StatConclusion>> schoolMap) {
        //小学
        List<StatConclusion> primaryList = schoolMap.get(SchoolAge.PRIMARY.code);
        getMyopiaItemVO(primaryList, SchoolAge.PRIMARY.desc, validScreeningNum, primarySchoolAndAboveVO::setPrimarySchool);

        //初中
        List<StatConclusion> juniorList = schoolMap.get(SchoolAge.JUNIOR.code);
        getMyopiaItemVO(juniorList, SchoolAge.JUNIOR.desc, validScreeningNum, primarySchoolAndAboveVO::setJuniorHighSchool);

        //高中 （普高+职高）
        List<StatConclusion> normalHighList = schoolMap.get(SchoolAge.HIGH.code);
        List<StatConclusion> vocationalHighList = schoolMap.get(SchoolAge.VOCATIONAL_HIGH.code);

        if (CollectionUtil.isNotEmpty(vocationalHighList)) {
            List<StatConclusion> highList = Lists.newArrayList();
            if (Objects.nonNull(normalHighList)) {
                highList.addAll(normalHighList);
            } else {
                normalHighList = Lists.newArrayList();
            }
            highList.addAll(vocationalHighList);

            getMyopiaItemVO(highList, SchoolAge.HIGH.desc, validScreeningNum, primarySchoolAndAboveVO::setHighSchool);
            getMyopiaItemVO(normalHighList, "普通高中", validScreeningNum, primarySchoolAndAboveVO::setNormalHighSchool);
            getMyopiaItemVO(vocationalHighList, SchoolAge.VOCATIONAL_HIGH.desc, validScreeningNum, primarySchoolAndAboveVO::setVocationalHighSchool);

        } else {
            getMyopiaItemVO(normalHighList, SchoolAge.HIGH.desc, validScreeningNum, primarySchoolAndAboveVO::setHighSchool);
        }

        //大学
        List<StatConclusion> universityList = schoolMap.get(SchoolAge.UNIVERSITY.code);
        getMyopiaItemVO(universityList, SchoolAge.UNIVERSITY.desc, validScreeningNum, primarySchoolAndAboveVO::setUniversity);

    }

    private void getMyopiaItemVO(List<StatConclusion> statConclusionList, String schoolAge, Integer validScreeningNum,
                                 Consumer<DistrictCommonDiseaseReportVO.MyopiaItemVO> consumer) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        int myopiaNum = (int) statConclusionList.stream()
                .map(StatConclusion::getIsMyopia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue)
                .count();
        DistrictCommonDiseaseReportVO.MyopiaItemVO myopiaItemVO = new DistrictCommonDiseaseReportVO.MyopiaItemVO();
        myopiaItemVO.setSchoolAge(schoolAge);
        myopiaItemVO.setMyopiaRatio(MathUtil.ratioNotSymbol(myopiaNum, validScreeningNum));
        Optional.of(myopiaItemVO).ifPresent(consumer);
    }

    private List<StatConclusion> getStatConclusionList(Integer noticeId, List<Integer> districtIds, Boolean isValid, Boolean isRescreen) {
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatConclusion::getSrcScreeningNoticeId, noticeId);
        queryWrapper.eq(StatConclusion::getIsValid, isValid);
        queryWrapper.eq(StatConclusion::getIsRescreen, isRescreen);
        queryWrapper.in(StatConclusion::getDistrictId, districtIds);
        return statConclusionService.list(queryWrapper);
    }

    /**
     * 常见病分析
     */
    private void getDistrictCommonDiseasesAnalysisVO(List<StatConclusion> statConclusionList, DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        List<StatConclusion> primaryAndAboveStatConclusionList = statConclusionList.stream().filter(sc -> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(sc.getSchoolGradeCode());
            return !Objects.equals(gradeCodeEnum.getType(), SchoolAge.KINDERGARTEN.code);
        }).collect(Collectors.toList());
        Map<Integer, String> schoolMap = Maps.newHashMap();
        if (CollectionUtil.isNotEmpty(primaryAndAboveStatConclusionList)) {
            Set<Integer> schoolIds = primaryAndAboveStatConclusionList.stream().map(StatConclusion::getSchoolId).collect(Collectors.toSet());
            List<School> schoolList = schoolService.getByIds(Lists.newArrayList(schoolIds));
            Map<Integer, String> collect = schoolList.stream().collect(Collectors.toMap(School::getId, School::getName));
            schoolMap.putAll(collect);
        }

        DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO = new DistrictCommonDiseasesAnalysisVO();
        //常见病分析变量
        getCommonDiseasesAnalysisVariableVO(primaryAndAboveStatConclusionList, districtCommonDiseasesAnalysisVO);
        //疾病监测情况
        districtDiseaseMonitorService.getDistrictDiseaseMonitorVO(primaryAndAboveStatConclusionList, districtCommonDiseasesAnalysisVO);
        //龋齿监测结果
        districtSaprodontiaMonitorService.getDistrictSaprodontiaMonitorVO(primaryAndAboveStatConclusionList, districtCommonDiseasesAnalysisVO);
        //体重身高监测结果
        districtHeightAndWeightMonitorService.getDistrictHeightAndWeightMonitorVO(primaryAndAboveStatConclusionList, districtCommonDiseasesAnalysisVO);
        //血压与脊柱弯曲异常监测结果
        districtBloodPressureAndSpinalCurvatureMonitorService.getDistrictBloodPressureAndSpinalCurvatureMonitorVO(primaryAndAboveStatConclusionList, districtCommonDiseasesAnalysisVO);
        //各学校筛查情况
        districtSchoolScreeningMonitorService.getDistrictSchoolScreeningMonitorVO(primaryAndAboveStatConclusionList, schoolMap, districtCommonDiseasesAnalysisVO);

        districtCommonDiseaseReportVO.setDistrictCommonDiseasesAnalysisVO(districtCommonDiseasesAnalysisVO);

    }


    /**
     * 常见病分析变量
     */
    private void getCommonDiseasesAnalysisVariableVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO = new CommonDiseasesNum()
                .build(statConclusionList)
                .ratioNotSymbol()
                .buidCommonDiseasesAnalysisVariableVO();
        districtCommonDiseasesAnalysisVO.setCommonDiseasesAnalysisVariableVO(commonDiseasesAnalysisVariableVO);

    }

}
