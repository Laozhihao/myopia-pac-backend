package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wupol.framework.core.util.CompareUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.CommonDiseasesAnalysisVariableVO;
import com.wupol.myopia.business.api.management.domain.vo.report.CommonDiseasesNum;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseaseReportVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * 区域常见病报告
     *
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    public DistrictCommonDiseaseReportVO districtCommonDiseaseReport(Integer districtId, Integer noticeId) {
        DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO = new DistrictCommonDiseaseReportVO();

        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        if (!districtIds.contains(districtId)){
            districtIds.add(districtId);
        }

        List<StatConclusion> statConclusionList = getStatConclusionList(noticeId, Lists.newArrayList(districtIds), Boolean.FALSE);
        statConclusionList = statConclusionList.stream().filter(sc -> !Objects.equals(sc.getIsCooperative(),1)).collect(Collectors.toList());

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
     * @param districtId 当前区域ID
     * @param districtIds 符合的区域ID集合
     * @param noticeId 通知ID
     * @param districtCommonDiseaseReportVO 按区域常见病报告实体
     */
    private void getGlobalVariableVO(Integer districtId,List<Integer> districtIds, Integer noticeId, DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        DistrictCommonDiseaseReportVO.GlobalVariableVO globalVariableVO = new DistrictCommonDiseaseReportVO.GlobalVariableVO();
        String format = "YYYY";

        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        if (Objects.isNull(screeningNotice)) {
            throw new BusinessException(String.format("不存在筛查通知: noticeId=%s", noticeId));
        }
        List<ScreeningPlan> screeningPlanList = screeningPlanService.getAllReleasePlanByNoticeId(noticeId);
        if (CollUtil.isEmpty(screeningPlanList)) {
            throw new BusinessException(String.format("该筛查通知不存在筛查计划: noticeId=%s", noticeId));
        }

        Set<Integer> planIds = screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByPlanIds(Lists.newArrayList(planIds));
        if (CollUtil.isEmpty(screeningResults)) {
            districtCommonDiseaseReportVO.setGlobalVariableVO(globalVariableVO);
            return;
        }

        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByScreeningPlanIds(Lists.newArrayList(planIds));
        if (CollUtil.isEmpty(planSchoolStudentList)) {
            districtCommonDiseaseReportVO.setGlobalVariableVO(globalVariableVO);
            return;
        }

        Map<Integer, List<ScreeningPlanSchoolStudent>> planSchoolStudentDistrictMap = planSchoolStudentList.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolDistrictId));
        List<Integer> haveStudentDistrictIds = CompareUtil.getRetain(districtIds, planSchoolStudentDistrictMap.keySet());
        List<ScreeningPlanSchoolStudent> planStudentCountList = getScreeningStudentList(haveStudentDistrictIds,planSchoolStudentDistrictMap);

        long totalSum = planStudentCountList.stream().map(ScreeningPlanSchoolStudent::getSchoolId).distinct().count();

        setSchoolItemData(planStudentCountList, globalVariableVO);

        //获取行政区域
        Date startTime = screeningNotice.getStartTime();
        Date endTime = screeningNotice.getEndTime();

        String districtName = districtService.getDistrictNameByDistrictId(districtId);
        Set<String> years = Sets.newHashSet(DateUtil.format(startTime, format),DateUtil.format(endTime, format));
        List<String> sort = CollUtil.sort(years, Comparator.comparing(String::toString));
        String year = CollUtil.join(sort, StrUtil.DASHED);
        List<String> yearPeriod;
        if (years.size() == 1) {
            yearPeriod = Lists.newArrayList(getDateStr(startTime),DateUtil.format(endTime, "MM月dd日"));
            globalVariableVO.setDataYear(year);
        } else {
            yearPeriod = Lists.newArrayList(getDateStr(startTime),getDateStr(endTime));
            VisionScreeningResult visionScreeningResult = screeningResults.get(0);
            globalVariableVO.setDataYear(DateUtil.format(visionScreeningResult.getCreateTime(), format));
        }

        String screeningTimePeriod = CollUtil.join(yearPeriod, StrUtil.DASHED);
        globalVariableVO.setScreeningTimePeriod(screeningTimePeriod);

        globalVariableVO.setAreaName(districtName);
        globalVariableVO.setYear(year);
        globalVariableVO.setTotalSchoolNum((int) totalSum);
        districtCommonDiseaseReportVO.setGlobalVariableVO(globalVariableVO);
    }

    /**
     * 日期转字符串
     *
     * @param date 日期
     */
    private static String getDateStr(Date date){
        return DateUtil.format(date, DatePattern.CHINESE_DATE_PATTERN);
    }


    /**
     * 学龄及学校数
     *
     * @param planStudentCountList 参与筛查计划的学生集合
     * @param globalVariableVO 全局变量
     */
    private void setSchoolItemData(List<ScreeningPlanSchoolStudent> planStudentCountList, DistrictCommonDiseaseReportVO.GlobalVariableVO globalVariableVO) {
        List<Integer> schoolAgeList = planStudentCountList.stream().map(planSchoolStudent -> {
            Integer gradeType = planSchoolStudent.getGradeType();
            if (Objects.equals(gradeType,SchoolAge.UNKNOWN.code)){
                return null;
            }
            if (Objects.equals(gradeType,SchoolAge.HIGH.code) ||Objects.equals(gradeType,SchoolAge.VOCATIONAL_HIGH.code) ){
                return SchoolAge.HIGH.code;
            }
            return gradeType;
        }).filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList());
        LinkedList<String> itemList = Lists.newLinkedList();
        for (Integer type : schoolAgeList) {
            String desc = SchoolAge.get(type).desc;
            if (Objects.equals(type,SchoolAge.KINDERGARTEN.code)){
                itemList.addFirst(desc);
                continue;
            }
            if (Objects.equals(type,SchoolAge.HIGH.code)){
                itemList.add(ReportConst.HIGH);
                continue;
            }
            if (Objects.equals(type,SchoolAge.UNIVERSITY.code)){
                itemList.add("综合性大学");
                continue;
            }
            itemList.add(desc);
        }
        globalVariableVO.setSchoolItem(itemList);
    }

    /**
     * 获取参与筛查计划的学生集合
     *
     * @param haveStudentDistrictIds 有学生区域Id集合
     * @param planStudentCountMap 计划学生数据集合
     */
    private List<ScreeningPlanSchoolStudent> getScreeningStudentList(List<Integer> haveStudentDistrictIds,Map<Integer, List<ScreeningPlanSchoolStudent>> planStudentCountMap){
        return haveStudentDistrictIds.stream().distinct().flatMap(id -> {
            List<ScreeningPlanSchoolStudent> planSchoolStudentList = planStudentCountMap.get(id);
            if (CollUtil.isNotEmpty(planSchoolStudentList)) {
                return planSchoolStudentList.stream();
            } else {
                return Stream.empty();
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 筛查人数和实际筛查人数
     *
     * @param noticeId 通知ID
     * @param districtIds 区域ID集合
     * @param districtCommonDiseaseReportVO 按区域常见病报告实体
     */
    private void setNum(Integer noticeId,List<Integer> districtIds, DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        Map<Integer, List<ScreeningPlanSchoolStudent>> planStudentCountMap = screeningPlanSchoolStudentService.getPlanStudentCountBySrcScreeningNoticeId(noticeId);

        List<Integer> haveStudentDistrictIds = CompareUtil.getRetain(districtIds, planStudentCountMap.keySet());

        List<ScreeningPlanSchoolStudent> planStudentCountList = getScreeningStudentList(haveStudentDistrictIds,planStudentCountMap);

        List<Integer> planSchoolStudentIds = planStudentCountList.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toList());
        Set<Integer> planIds = planStudentCountList.stream().map(ScreeningPlanSchoolStudent::getScreeningPlanId).collect(Collectors.toSet());

        districtCommonDiseaseReportVO.setScreeningStudentNum(planStudentCountList.size());

        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByPlanIds(Lists.newArrayList(planIds));
        if (CollUtil.isEmpty(screeningResults)) {
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
     *
     * @param statConclusionList 筛查数据结论集合
     * @param districtCommonDiseaseReportVO 按区域常见病报告实体
     */
    private void getVisionAnalysisVO(List<StatConclusion> statConclusionList, DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        List<StatConclusion> validList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getIsValid(), Boolean.TRUE)).collect(Collectors.toList());

        DistrictCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO = new DistrictCommonDiseaseReportVO.VisionAnalysisVO();
        int validScreeningNum = validList.size();
        visionAnalysisVO.setValidScreeningNum(validScreeningNum);
        List<StatConclusion> kindergartenList = validList.stream().filter(statConclusion -> Objects.equals(statConclusion.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());
        List<StatConclusion> primarySchoolAndAboveList = validList.stream().filter(statConclusion -> !Objects.equals(statConclusion.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());
        getKindergartenVO(kindergartenList, validScreeningNum, visionAnalysisVO);
        getPrimarySchoolAndAboveVO(primarySchoolAndAboveList, validScreeningNum, visionAnalysisVO);

        districtCommonDiseaseReportVO.setVisionAnalysisVO(visionAnalysisVO);
    }

    /**
     * 获取幼儿园分析
     *
     * @param kindergartenList 幼儿园筛查数据结论集合
     * @param validScreeningNum 有效筛查数据
     * @param visionAnalysisVO 视力分析对象
     */
    private void getKindergartenVO(List<StatConclusion> kindergartenList, int validScreeningNum, DistrictCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO) {
        if (CollUtil.isEmpty(kindergartenList)) {
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

        kindergartenVO.setAvgVision(StatUtil.averageVision(kindergartenList));
        kindergartenVO.setLowVisionRatio(MathUtil.ratioNotSymbol(lowVisionNum, validScreeningNum));
        kindergartenVO.setMyopiaLevelInsufficientRatio(MathUtil.ratioNotSymbol(visionLabelZeroSpNum, validScreeningNum));
        kindergartenVO.setAnisometropiaRatio(MathUtil.ratioNotSymbol(anisometropiaNum, validScreeningNum));

        visionAnalysisVO.setKindergartenVO(kindergartenVO);
    }


    /**
     * 获取小学及以上
     *
     * @param primarySchoolAndAboveList 小学及以上筛查数据结论集合
     * @param validScreeningNum 有效筛查数据
     * @param visionAnalysisVO 视力分析对象
     */
    private void getPrimarySchoolAndAboveVO(List<StatConclusion> primarySchoolAndAboveList, int validScreeningNum, DistrictCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO) {
        if (CollUtil.isEmpty(primarySchoolAndAboveList)) {
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

        primarySchoolAndAboveVO.setAvgVision(StatUtil.averageVision(primarySchoolAndAboveList));
        primarySchoolAndAboveVO.setLowVisionRatio(MathUtil.ratioNotSymbol(lowVisionNum, validScreeningNum));
        primarySchoolAndAboveVO.setMyopiaRatio(MathUtil.ratioNotSymbol(myopiaNum, validScreeningNum));

        setSchoolAge(validScreeningNum, primarySchoolAndAboveVO, schoolMap);

        visionAnalysisVO.setPrimarySchoolAndAboveVO(primarySchoolAndAboveVO);
    }

    /**
     * 设置学龄
     * @param validScreeningNum 有效数据
     * @param primarySchoolAndAboveVO 小学及以上
     * @param schoolMap 学校集合
     */
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

        if (CollUtil.isNotEmpty(vocationalHighList)) {

            List<StatConclusion> highList = Lists.newArrayList();
            highList.addAll(vocationalHighList);
            if (CollUtil.isNotEmpty(normalHighList)) {
                highList.addAll(normalHighList);
                getMyopiaItemVO(highList, ReportConst.HIGH, validScreeningNum, primarySchoolAndAboveVO::setHighSchool);
                getMyopiaItemVO(normalHighList, SchoolAge.HIGH.desc, validScreeningNum, primarySchoolAndAboveVO::setNormalHighSchool);
                getMyopiaItemVO(vocationalHighList, SchoolAge.VOCATIONAL_HIGH.desc, validScreeningNum, primarySchoolAndAboveVO::setVocationalHighSchool);
            }else {
                getMyopiaItemVO(highList, ReportConst.HIGH, validScreeningNum, primarySchoolAndAboveVO::setHighSchool);
            }

        } else {
            if (CollUtil.isNotEmpty(normalHighList)){
                getMyopiaItemVO(normalHighList, ReportConst.HIGH, validScreeningNum, primarySchoolAndAboveVO::setHighSchool);
            }
        }

        //大学
        List<StatConclusion> universityList = schoolMap.get(SchoolAge.UNIVERSITY.code);
        getMyopiaItemVO(universityList, SchoolAge.UNIVERSITY.desc, validScreeningNum, primarySchoolAndAboveVO::setUniversity);

    }

    /**
     * 设置近视项目
     * @param statConclusionList 筛查数据结论集合
     * @param schoolAge 学龄
     * @param validScreeningNum 有效筛查数
     * @param consumer 消费
     */
    private void getMyopiaItemVO(List<StatConclusion> statConclusionList, String schoolAge, Integer validScreeningNum,
                                 Consumer<DistrictCommonDiseaseReportVO.MyopiaItemVO> consumer) {
        if (CollUtil.isEmpty(statConclusionList)) {
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

    /**
     * 获取筛查数据结论集合
     *
     * @param noticeId 通知ID
     * @param districtIds 区域ID集合
     * @param isRescreen 是否复测
     */
    private List<StatConclusion> getStatConclusionList(Integer noticeId, List<Integer> districtIds, Boolean isRescreen) {
        StatConclusionQueryDTO query = new StatConclusionQueryDTO();
        query.setDistrictIds(districtIds)
                .setSrcScreeningNoticeId(noticeId)
                .setIsRescreen(isRescreen);
        return statConclusionService.listOfReleasePlanByQuery(query);
    }

    /**
     * 常见病分析
     */
    private void getDistrictCommonDiseasesAnalysisVO(List<StatConclusion> statConclusionList, DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }

        List<StatConclusion> primaryAndAboveStatConclusionList = statConclusionList.stream()
                .filter(sc -> {
                    GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(sc.getSchoolGradeCode());
                    return !Objects.equals(gradeCodeEnum.getType(), SchoolAge.KINDERGARTEN.code);
                }).collect(Collectors.toList());
        Map<Integer, String> schoolMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(primaryAndAboveStatConclusionList)) {
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

        CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO = new CommonDiseasesNum()
                .build(statConclusionList)
                .ratioNotSymbol()
                .buidCommonDiseasesAnalysisVariableVO();
        districtCommonDiseasesAnalysisVO.setCommonDiseasesAnalysisVariableVO(commonDiseasesAnalysisVariableVO);

    }

}
