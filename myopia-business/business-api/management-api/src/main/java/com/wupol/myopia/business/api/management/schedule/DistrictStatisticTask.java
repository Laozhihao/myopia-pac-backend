package com.wupol.myopia.business.api.management.schedule;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.CompareUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.api.management.domain.bo.StatisticResultBO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.stat.domain.dos.*;
import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 按区域统计
 *
 * @author hang.yuan 2022/4/14 22:03
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Component
public class DistrictStatisticTask {

    private final ScreeningPlanService screeningPlanService;
    private final StatConclusionService statConclusionService;
    private final ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    private final ScreeningNoticeService screeningNoticeService;
    private final DistrictService districtService;
    private final ScreeningResultStatisticService screeningResultStatisticService;

    /**
     * 按区域统计
     */
    public void districtStatistics(List<Integer> yesterdayScreeningPlanIds) {
        //筛查计划ID 查找筛查通知ID
        List<Integer> screeningNoticeIds = screeningPlanService.getSrcScreeningNoticeIdsByIds(yesterdayScreeningPlanIds);
        if(CollectionUtil.isEmpty(screeningNoticeIds)){
            log.error("未找到筛查通知数据，planIds:{}",CollectionUtil.join(yesterdayScreeningPlanIds,","));
            return;
        }

        // 单点筛查（自己创建的筛查）机构创建的数据不需要统计
        screeningNoticeIds = screeningNoticeIds.stream().filter(id-> !CommonConst.DEFAULT_ID.equals(id)).collect(Collectors.toList());

        //筛查通知ID 查出筛查数据结论
        List<StatConclusion> statConclusionList = statConclusionService.getBySrcScreeningNoticeIds(screeningNoticeIds);
        if (CollectionUtil.isEmpty(statConclusionList)){return; }

        //筛查数据结论 根据筛查类型分组 分别统计
        Map<Integer, List<StatConclusion>> screeningTypeStatConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getScreeningType));


        //视力筛查
        List<VisionScreeningResultStatistic> visionScreeningResultStatisticList = visionScreeningResultStatistic(screeningTypeStatConclusionMap);
        if (CollectionUtil.isNotEmpty(visionScreeningResultStatisticList)){
            for (VisionScreeningResultStatistic visionScreeningResultStatistic : visionScreeningResultStatisticList) {
                screeningResultStatisticService.saveVisionScreeningResultStatistic(visionScreeningResultStatistic);
            }
        }
        //常见病筛查
        List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList = commonDiseaseScreeningResultStatistic(screeningTypeStatConclusionMap);
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningResultStatisticList)){
            for ( CommonDiseaseScreeningResultStatistic commonDiseaseScreeningResultStatistic : commonDiseaseScreeningResultStatisticList) {
                screeningResultStatisticService.saveCommonDiseaseScreeningResultStatistic(commonDiseaseScreeningResultStatistic);
            }
        }

    }

    /**
     * 视力筛查结果统计
     */
    private List<VisionScreeningResultStatistic> visionScreeningResultStatistic(Map<Integer, List<StatConclusion>> screeningTypeStatConclusionMap){
        List<VisionScreeningResultStatistic> visionScreeningResultStatisticList= Lists.newArrayList();
        List<StatConclusion> statConclusions = screeningTypeStatConclusionMap.get(0);
        if (CollectionUtil.isNotEmpty(statConclusions)){
            statistic(statConclusions,visionScreeningResultStatisticList,null);
        }
        return visionScreeningResultStatisticList;
    }

    /**
     * 常见病筛查结果统计
     */
    private List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatistic(Map<Integer, List<StatConclusion>> screeningTypeStatConclusionMap){
        List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList =Lists.newArrayList();
        List<StatConclusion> statConclusions = screeningTypeStatConclusionMap.get(1);
        if (CollectionUtil.isNotEmpty(statConclusions)){
            statistic(statConclusions,null,commonDiseaseScreeningResultStatisticList);
        }
        return commonDiseaseScreeningResultStatisticList;
    }

    /**
     * 统计逻辑
     */
    private void statistic(List<StatConclusion> statConclusionList,
                           List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                           List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList){
        //根据筛查通知ID分组
        Map<Integer, List<StatConclusion>> statConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSrcScreeningNoticeId));

        statConclusionMap.forEach((screeningNoticeId,statConclusions)->{

            // 筛查通知中的学校所在地区层级的计划学生总数
            Map<Integer, Long> districtPlanStudentCountMap = screeningPlanSchoolStudentService.getDistrictPlanStudentCountBySrcScreeningNoticeId(screeningNoticeId);

            //查出通知对应的地区顶级层级：从任务所在省级开始（因为筛查计划可选全省学校）
            ScreeningNotice screeningNotice = screeningNoticeService.getById(screeningNoticeId);
            Integer provinceDistrictId = districtService.getProvinceId(screeningNotice.getDistrictId());

            //同一个筛查通知下不同地区筛查数据结论 ,根据地区分组
            Map<Integer, List<StatConclusion>> districtStatConclusionMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getDistrictId));

            if(Objects.equals(0,screeningNotice.getScreeningType())){
                //根据地区生成视力筛查统计
                genVisionStatisticsByDistrictId(screeningNotice, provinceDistrictId, districtPlanStudentCountMap, visionScreeningResultStatisticList, districtStatConclusionMap);
            }else {
                //根据地区生成常见病筛查统计
                genCommonDiseaseStatisticsByDistrictId(screeningNotice, provinceDistrictId, districtPlanStudentCountMap, commonDiseaseScreeningResultStatisticList, districtStatConclusionMap);
            }

        });
    }


    /**
     * 根据地区生成视力筛查统计
     */
    private void genVisionStatisticsByDistrictId(ScreeningNotice screeningNotice, Integer districtId, Map<Integer, Long> districtPlanStudentCountMap,
                                           List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                           Map<Integer, List<StatConclusion>> districtStatConclusions) {
        List<District> childDistricts = new ArrayList<>();
        List<Integer> childDistrictIds = new ArrayList<>();
        try {
            // 合计的要包括自己层级的筛查数据
            childDistricts = districtService.getChildDistrictByParentIdPriorityCache(districtId);
            childDistrictIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        } catch (IOException e) {
            log.error("获取区域层级失败", e);
        }
        //2.4 层级循环处理并添加到对应的统计中
        //获取两集合的交集
        List<Integer> haveStatConclusionsChildDistrictIds = CompareUtil.getRetain(childDistrictIds, districtStatConclusions.keySet());
        List<Integer> haveStudentDistrictIds = CompareUtil.getRetain(childDistrictIds, districtPlanStudentCountMap.keySet());

        // 层级合计数据
        List<StatConclusion> totalStatConclusions = haveStatConclusionsChildDistrictIds.stream().map(districtStatConclusions::get).flatMap(Collection::stream).collect(Collectors.toList());
        Integer totalPlanStudentCount = (int) haveStudentDistrictIds.stream().mapToLong(districtPlanStudentCountMap::get).sum();

        // 层级自身数据
        List<StatConclusion> selfStatConclusions = districtStatConclusions.getOrDefault(districtId, Collections.emptyList());
        Integer selfPlanStudentCount = districtPlanStudentCountMap.getOrDefault(districtId, 0L).intValue();

        StatisticResultBO totalStatistic = new StatisticResultBO()
                .setScreeningNotice(screeningNotice)
                .setDistrictId(districtId)
                .setPlanStudentCount(totalPlanStudentCount)
                .setStatConclusions(totalStatConclusions);

        genTotalStatistics(totalStatistic, visionScreeningResultStatisticList,null);

        StatisticResultBO selfStatistic = new StatisticResultBO()
                .setScreeningNotice(screeningNotice)
                .setDistrictId(districtId)
                .setPlanStudentCount(selfPlanStudentCount)
                .setStatConclusions(selfStatConclusions);
        genSelfStatistics(selfStatistic, visionScreeningResultStatisticList,null);
        if (totalStatConclusions.size() != selfStatConclusions.size()) {
            //递归统计下层级数据
            childDistricts.forEach(childDistrict -> genVisionStatisticsByDistrictId(screeningNotice, childDistrict.getId(), districtPlanStudentCountMap, visionScreeningResultStatisticList, districtStatConclusions));
        }
    }

    /**
     * 根据地区生成常见病筛查统计
     */
    private void genCommonDiseaseStatisticsByDistrictId(ScreeningNotice screeningNotice, Integer districtId, Map<Integer, Long> districtPlanStudentCountMap,
                                                 List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList,
                                                 Map<Integer, List<StatConclusion>> districtStatConclusions) {
        List<District> childDistricts = new ArrayList<>();
        List<Integer> childDistrictIds = new ArrayList<>();
        try {
            // 合计的要包括自己层级的筛查数据
            childDistricts = districtService.getChildDistrictByParentIdPriorityCache(districtId);
            childDistrictIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        } catch (IOException e) {
            log.error("获取区域层级失败", e);
        }
        //2.4 层级循环处理并添加到对应的统计中
        List<Integer> haveStatConclusionsChildDistrictIds = CompareUtil.getRetain(childDistrictIds, districtStatConclusions.keySet());
        List<Integer> haveStudentDistrictIds = CompareUtil.getRetain(childDistrictIds, districtPlanStudentCountMap.keySet());
        // 层级合计数据
        List<StatConclusion> totalStatConclusions = haveStatConclusionsChildDistrictIds.stream().map(districtStatConclusions::get).flatMap(Collection::stream).collect(Collectors.toList());
        Integer totalPlanStudentCount = (int) haveStudentDistrictIds.stream().mapToLong(districtPlanStudentCountMap::get).sum();
        // 层级自身数据
        List<StatConclusion> selfStatConclusions = districtStatConclusions.getOrDefault(districtId, Collections.emptyList());
        Integer selfPlanStudentCount = districtPlanStudentCountMap.getOrDefault(districtId, 0L).intValue();

        StatisticResultBO totalStatistic = new StatisticResultBO()
                .setScreeningNotice(screeningNotice)
                .setDistrictId(districtId)
                .setPlanStudentCount(totalPlanStudentCount)
                .setStatConclusions(totalStatConclusions);

        genTotalStatistics(totalStatistic, null,commonDiseaseScreeningResultStatisticList);

        StatisticResultBO selfStatistic = new StatisticResultBO()
                .setScreeningNotice(screeningNotice)
                .setDistrictId(districtId)
                .setPlanStudentCount(selfPlanStudentCount)
                .setStatConclusions(selfStatConclusions);

        genSelfStatistics(selfStatistic,null,commonDiseaseScreeningResultStatisticList);
        if (totalStatConclusions.size() != selfStatConclusions.size()) {
            //递归统计下层级数据
            childDistricts.forEach(childDistrict -> genCommonDiseaseStatisticsByDistrictId(screeningNotice, childDistrict.getId(), districtPlanStudentCountMap, commonDiseaseScreeningResultStatisticList, districtStatConclusions));
        }
    }

    /**
     * 合计统计
     */
    private void genTotalStatistics(StatisticResultBO totalStatistic,
                                    List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                    List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList) {

        if (CollectionUtils.isEmpty(totalStatistic.getStatConclusions()) && totalStatistic.getPlanStudentCount() == 0) {
            // 计划筛查学生不为0时，即使还没有筛查数据，也要新增统计
            return;
        }
        // 层级总的筛查数据不一定属于同一个任务，所以取默认0
        totalStatistic.setScreeningTaskId(CommonConst.DEFAULT_ID)
                .setScreeningPlanId(CommonConst.DEFAULT_ID)
                .setIsTotal(Boolean.TRUE).setSchoolId(-1).setScreeningOrgId(-1);

        if (Objects.nonNull(visionScreeningResultStatisticList)){
            visionScreeningResultStatisticList.addAll(buildVisionScreening(totalStatistic));
        }
        if (Objects.nonNull(commonDiseaseScreeningResultStatisticList)){
            commonDiseaseScreeningResultStatisticList.addAll(buildCommonDiseaseScreening(totalStatistic));
        }
    }

    /**
     * 单条统计
     */
    private void genSelfStatistics(StatisticResultBO selfStatistic,
                                   List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                   List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList) {
        List<StatConclusion> statConclusions = selfStatistic.getStatConclusions();
        if (CollectionUtils.isEmpty(statConclusions) && selfStatistic.getPlanStudentCount() == 0) {
            // 计划筛查学生不为0时，即使还没有筛查数据，也要新增统计
            return;
        }

        // 层级自己的筛查数据肯定属于同一个任务，所以只取第一个的就可以
        Integer screeningTaskId = CollectionUtils.isEmpty(statConclusions) ? CommonConst.DEFAULT_ID : statConclusions.get(0).getTaskId();
        Integer screeningPlanId = CollectionUtils.isEmpty(statConclusions) ? CommonConst.DEFAULT_ID : statConclusions.get(0).getPlanId();
        selfStatistic.setScreeningTaskId(screeningTaskId)
                .setScreeningPlanId(screeningPlanId)
                .setIsTotal(Boolean.FALSE).setSchoolId(-1).setScreeningOrgId(-1);

        if (Objects.nonNull(visionScreeningResultStatisticList)){
            visionScreeningResultStatisticList.addAll(buildVisionScreening(selfStatistic));
        }
        if (Objects.nonNull(commonDiseaseScreeningResultStatisticList)){
            commonDiseaseScreeningResultStatisticList.addAll(buildCommonDiseaseScreening(selfStatistic));
        }
    }


    /**
     * 按区域 - 视力筛查数据统计
     */
    private List<VisionScreeningResultStatistic> buildVisionScreening(StatisticResultBO statistic) {

        List<VisionScreeningResultStatistic> visionScreeningResultStatisticList= Lists.newArrayList();
        Map<Integer, List<StatConclusion>> schoolMap = statistic.getStatConclusions().stream().collect(Collectors.groupingBy(sc -> getKey(sc.getSchoolAge())));

        schoolMap.forEach((schoolAge,list)->{
            if (Objects.equals(schoolAge, 5)) {
                statistic.setSchoolType(8);
            } else {
                statistic.setSchoolType(0);
            }
            visionScreening(statistic, list,visionScreeningResultStatisticList);
        });
        return visionScreeningResultStatisticList;
    }

    /**
     * 按区域 - 视力筛查数据统计
     */
    private  void visionScreening(StatisticResultBO totalStatistic,
                                                    List<StatConclusion> statConclusions,
                                                    List<VisionScreeningResultStatistic> visionScreeningResultStatisticList){

        //有效数据（初筛数据完整性判断）
        Map<Boolean, List<StatConclusion>> isValidMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));

        //纳入统计数据
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(Boolean.TRUE, Collections.emptyList());
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        int validScreeningNum = isRescreenMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();

        //复测数据
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        //实际筛查人数
        Integer realScreeningStudentNum = isRescreenTotalMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();


        VisionScreeningResultStatistic statistic = new VisionScreeningResultStatistic();
        //设置基础数据
        setBasicData(statConclusions,totalStatistic,realScreeningStudentNum,validScreeningNum,statistic);

        //设置视力分析数据
        if (Objects.equals(totalStatistic.getSchoolType(),5)){
            setKindergartenVisionAnalysis(totalStatistic, statConclusions, validScreeningNum, statistic);
        }else {
            setPrimarySchoolAndAboveVisionAnalysis(totalStatistic, statConclusions, validScreeningNum, statistic);
        }
        //设置视力预警数据
        setVisionWarning(validScreeningNum,statConclusions, statistic);
        //设置复测情况数据
        setRescreenSituation(statConclusions,isRescreenMap,statistic);

        visionScreeningResultStatisticList.add(statistic);

    }



    private List<CommonDiseaseScreeningResultStatistic> buildCommonDiseaseScreening(StatisticResultBO statistic) {
        List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList= Lists.newArrayList();
        Map<Integer, List<StatConclusion>> schoolMap = statistic.getStatConclusions().stream().collect(Collectors.groupingBy(sc -> getKey(sc.getSchoolAge())));

        schoolMap.forEach((schoolAge,list)->{
            if (Objects.equals(schoolAge, 5)) {
                statistic.setSchoolType(8);
            } else {
                statistic.setSchoolType(0);
            }
            commonDiseaseScreening(statistic, list,commonDiseaseScreeningResultStatisticList);
        });
        return commonDiseaseScreeningResultStatisticList;
    }

    /**
     * 按区域 - 常见病筛查数据统计
     */
    private  void commonDiseaseScreening(StatisticResultBO totalStatistic,
                                  List<StatConclusion> statConclusions,
                                  List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList){

        //有效数据（初筛数据完整性判断）
        Map<Boolean, List<StatConclusion>> isValidMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));

        //纳入统计数据
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(Boolean.TRUE, Collections.emptyList());
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        int validScreeningNum = isRescreenMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();

        //复测数据
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        //实际筛查人数
        Integer realScreeningStudentNum = isRescreenTotalMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();


        CommonDiseaseScreeningResultStatistic statistic = new CommonDiseaseScreeningResultStatistic();
        //设置基础数据
        setBasicData(statConclusions,totalStatistic,realScreeningStudentNum,validScreeningNum,statistic);

        //设置视力分析数据
        if (Objects.equals(totalStatistic.getSchoolType(),5)){
            setKindergartenVisionAnalysis(totalStatistic, statConclusions, validScreeningNum, statistic);
        }else {
            setPrimarySchoolAndAboveVisionAnalysis(totalStatistic, statConclusions, validScreeningNum, statistic);
        }
        //设置视力预警数据
        setVisionWarning(validScreeningNum,statConclusions, statistic);
        //设置复测情况数据
        setRescreenSituation(statConclusions,isRescreenMap,statistic);

        //设置龋齿数据
        setSaprodontia(statConclusions, realScreeningStudentNum, statistic);
        //设置常见病数据
        setCommonDisease(statConclusions, realScreeningStudentNum, statistic);
        //设置问卷调查数据
        setQuestionnaire(statistic);

        commonDiseaseScreeningResultStatisticList.add(statistic);

    }

    /**
     * 基础数据
     */
    private void setBasicData(List<StatConclusion> statConclusions,
                              StatisticResultBO totalStatistic,
                              Integer realScreeningStudentNum,Integer validScreeningNum,
                              VisionScreeningResultStatistic statistic) {
        Integer planScreeningNum = totalStatistic.getPlanStudentCount();
        int schoolNum = (int)statConclusions.stream().filter(Objects::nonNull).map(StatConclusion::getSchoolId).count();
        if (Objects.isNull(statistic.getId())){
            statistic.setCreateTime(new Date());
        }
        statistic.setScreeningNoticeId(totalStatistic.getScreeningNotice().getId())
                .setScreeningTaskId(totalStatistic.getScreeningTaskId())
                .setScreeningPlanId(totalStatistic.getScreeningPlanId())
                .setScreeningType(totalStatistic.getScreeningNotice().getScreeningType())
                .setIsTotal(totalStatistic.getIsTotal())
                .setSchoolId(totalStatistic.getSchoolId())
                .setScreeningOrgId(totalStatistic.getScreeningOrgId())
                .setSchoolType(totalStatistic.getSchoolType())
                .setDistrictId(totalStatistic.getDistrictId())
                .setSchoolNum(schoolNum)
                .setPlanScreeningNum(planScreeningNum)
                .setRealScreeningNum(realScreeningStudentNum)
                .setFinishRatio(MathUtil.ratio(realScreeningStudentNum,planScreeningNum))
                .setValidScreeningNum(validScreeningNum)
                .setValidScreeningRatio(MathUtil.ratio(validScreeningNum,realScreeningStudentNum));
    }

    /**
     * 设置小学及以上视力分析数据
     */
    private void setPrimarySchoolAndAboveVisionAnalysis(StatisticResultBO totalStatistic, List<StatConclusion> statConclusions, int validScreeningNum, VisionScreeningResultStatistic statistic) {
        PrimarySchoolAndAboveVisionAnalysisDO visionAnalysisDO = new PrimarySchoolAndAboveVisionAnalysisDO();
        Integer lowVisionNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsLowVision).count();
        List<BigDecimal> leftList = statConclusions.stream().map(StatConclusion::getVisionL).filter(Objects::nonNull).collect(Collectors.toList());
        double leftSum = leftList.stream().mapToDouble(BigDecimal::doubleValue).sum();
        double avgLeftVision =BigDecimalUtil.divide(String.valueOf(leftSum),String.valueOf(leftList.size()),1).doubleValue();
        List<BigDecimal> rightList = statConclusions.stream().map(StatConclusion::getVisionR).filter(Objects::nonNull).collect(Collectors.toList());
        double rightSum = rightList.stream().mapToDouble(BigDecimal::doubleValue).sum();
        double avgRightVision = BigDecimalUtil.divide(String.valueOf(rightSum),String.valueOf(rightList.size()),1).doubleValue();

        int myopiaNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsMyopia).count();
        int myopiaLevelEarlyNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(sc->Objects.equals(2,sc.getMyopiaLevel())).count();
        int lowMyopiaNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(sc->Objects.equals(3,sc.getMyopiaLevel())).count();
        int highMyopiaNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(sc->Objects.equals(5,sc.getMyopiaLevel())).count();
        int astigmatismNum =(int) statConclusions.stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsAstigmatism())).count();
        Integer wearingGlassNum = (int) statConclusions.stream().filter(sc-> Objects.equals(Boolean.TRUE,sc.getIsWearingGlasses()) && Objects.equals(Boolean.TRUE,sc.getIsValid())).count();
        Integer nightWearingOrthokeratologyLensesNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(sc-> Objects.equals(3,sc.getGlassesType())).count();
        Integer treatmentAdviceNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsRecommendVisit).count();
        visionAnalysisDO.setLowVisionNum(lowVisionNum)
                .setLowVisionRatio(MathUtil.ratio(lowVisionNum,validScreeningNum))
                .setAvgLeftVision(BigDecimal.valueOf(avgLeftVision)).setAvgRightVision(BigDecimal.valueOf(avgRightVision))
                .setMyopiaNum(myopiaNum).setMyopiaRatio(MathUtil.ratio(myopiaNum,validScreeningNum))
                .setMyopiaLevelEarlyNum(myopiaLevelEarlyNum).setMyopiaLevelEarlyRatio(MathUtil.ratio(myopiaLevelEarlyNum,validScreeningNum))
                .setLowMyopiaNum(lowMyopiaNum).setLowMyopiaRatio(MathUtil.ratio(lowMyopiaNum,validScreeningNum))
                .setHighMyopiaNum(highMyopiaNum).setHighMyopiaRatio(MathUtil.ratio(highMyopiaNum,validScreeningNum))
                .setAstigmatismNum(astigmatismNum).setAstigmatismRatio(MathUtil.ratio(astigmatismNum,validScreeningNum))
                .setWearingGlassesNum(wearingGlassNum).setWearingGlassesRatio(MathUtil.ratio(wearingGlassNum,validScreeningNum))
                .setNightWearingOrthokeratologyLensesNum(nightWearingOrthokeratologyLensesNum).setNightWearingOrthokeratologyLensesRatio(MathUtil.ratio(nightWearingOrthokeratologyLensesNum,validScreeningNum))
                .setTreatmentAdviceNum(treatmentAdviceNum).setTreatmentAdviceRatio(MathUtil.ratio(treatmentAdviceNum,validScreeningNum))
                .setSchoolType(totalStatistic.getSchoolType());

        statistic.setVisionAnalysis(visionAnalysisDO);
    }

    /**
     * 设置幼儿园视力分析数据
     */
    private void setKindergartenVisionAnalysis(StatisticResultBO totalStatistic, List<StatConclusion> statConclusions, int validScreeningNum, VisionScreeningResultStatistic statistic) {
        KindergartenVisionAnalysisDO visionAnalysisDO =new KindergartenVisionAnalysisDO();
        Map<Integer, Long> visionLabelNumberMap = statConclusions.stream().filter(Objects::nonNull).filter(stat -> Objects.nonNull(stat.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        Integer lowVisionNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsLowVision).count();
        Integer ametropiaNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsRefractiveError).count();
        Integer anisometropiaNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsAnisometropia).count();
        Integer wearingGlassNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsWearingGlasses).count();
        Integer treatmentAdviceNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsRecommendVisit).count();
        List<BigDecimal> leftList = statConclusions.stream().map(StatConclusion::getVisionL).filter(Objects::nonNull).collect(Collectors.toList());
        double leftSum = leftList.stream().mapToDouble(BigDecimal::doubleValue).sum();
        double avgLeftVision =BigDecimalUtil.divide(String.valueOf(leftSum),String.valueOf(leftList.size()),1).doubleValue();
        List<BigDecimal> rightList = statConclusions.stream().map(StatConclusion::getVisionR).filter(Objects::nonNull).collect(Collectors.toList());
        double rightSum = rightList.stream().mapToDouble(BigDecimal::doubleValue).sum();
        double avgRightVision = BigDecimalUtil.divide(String.valueOf(rightSum),String.valueOf(rightList.size()),1).doubleValue();

        Integer visionLabelZeroSpNum = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO_SP.code, 0L).intValue();
        visionAnalysisDO.setLowVisionNum(lowVisionNum)
                .setLowVisionRatio(MathUtil.ratio(lowVisionNum,validScreeningNum))
                .setAvgLeftVision(BigDecimal.valueOf(avgLeftVision)).setAvgRightVision(BigDecimal.valueOf(avgRightVision))
                .setAmetropiaNum(ametropiaNum).setAmetropiaRatio(MathUtil.ratio(ametropiaNum,validScreeningNum))
                .setAnisometropiaNum(anisometropiaNum).setAnisometropiaRatio(MathUtil.ratio(anisometropiaNum,validScreeningNum))
                .setMyopiaLevelInsufficientNum(visionLabelZeroSpNum).setMyopiaLevelInsufficientNumRatio(MathUtil.ratio(visionLabelZeroSpNum,validScreeningNum))
                .setWearingGlassesNum(wearingGlassNum).setWearingGlassesRatio(MathUtil.ratio(wearingGlassNum,validScreeningNum))
                .setTreatmentAdviceNum(treatmentAdviceNum).setTreatmentAdviceRatio(MathUtil.ratio(treatmentAdviceNum,validScreeningNum))
                .setSchoolType(totalStatistic.getSchoolType());
        statistic.setVisionAnalysis(visionAnalysisDO);
    }

    private Integer getKey(Integer schoolAge){
        return Objects.equals(schoolAge,5)?schoolAge:0;
    }


    /**
     * 设置视力预警数据
     */
    private void setVisionWarning(int validScreeningNum,
                                  List<StatConclusion> statConclusions,
                                  VisionScreeningResultStatistic statistic) {
        VisionWarningDO visionWarningDO = new VisionWarningDO();
        Map<Integer, Long> visionLabelNumberMap = statConclusions.stream().filter(Objects::nonNull).filter(stat -> Objects.nonNull(stat.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        Integer visionLabel0Num = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO.code, 0L).intValue();
        Integer visionLabel1Num = visionLabelNumberMap.getOrDefault(WarningLevel.ONE.code, 0L).intValue();
        Integer visionLabel2Num = visionLabelNumberMap.getOrDefault(WarningLevel.TWO.code, 0L).intValue();
        Integer visionLabel3Num = visionLabelNumberMap.getOrDefault(WarningLevel.THREE.code, 0L).intValue();
        Integer visionWarningNum =visionLabel0Num+visionLabel1Num+visionLabel2Num+visionLabel3Num;

        visionWarningDO.setVisionWarningNum(visionWarningNum)
                .setVisionLabel0Num(visionLabel0Num).setVisionLabel0Ratio(MathUtil.ratio(visionLabel0Num,validScreeningNum))
                .setVisionLabel1Num(visionLabel1Num).setVisionLabel1Ratio(MathUtil.ratio(visionLabel1Num,validScreeningNum))
                .setVisionLabel2Num(visionLabel2Num).setVisionLabel2Ratio(MathUtil.ratio(visionLabel2Num,validScreeningNum))
                .setVisionLabel3Num(visionLabel3Num).setVisionLabel3Ratio(MathUtil.ratio(visionLabel3Num,validScreeningNum));
        statistic.setVisionWarning(visionWarningDO);
    }

    /**
     * 设置复测数据
     */
    private RescreenSituationDO setRescreenSituation(List<StatConclusion> statConclusions,Map<Boolean, List<StatConclusion>> isRescreenMap,
                                                     VisionScreeningResultStatistic statistic) {
        RescreenSituationDO rescreenSituationDO = new RescreenSituationDO();
        Integer wearingGlassRescreenNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(sc->sc.getIsRescreen() && sc.getIsWearingGlasses() && sc.getIsValid()).count();
        Integer withoutGlassRescreenNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(sc->sc.getIsRescreen() && sc.getIsWearingGlasses()&& sc.getIsValid()).count();
        Integer rescreenNum = (int) statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsRescreen).count();
        int validRescreenNum = isRescreenMap.getOrDefault(Boolean.TRUE, Collections.emptyList()).size();
        rescreenSituationDO.setRetestNum(rescreenNum).setRetestRatio(MathUtil.ratio(rescreenNum, validRescreenNum))
                .setWearingGlassRetestNum(wearingGlassRescreenNum).setWearingGlassRetestRatio(MathUtil.ratio(wearingGlassRescreenNum, validRescreenNum))
                .setWithoutGlassRetestNum(withoutGlassRescreenNum).setWithoutGlassRetestRatio(MathUtil.ratio(wearingGlassRescreenNum, validRescreenNum))
                .setRescreeningItemNum(0).setErrorItemNum(0).setIncidence("");
        statistic.setRescreenSituation(rescreenSituationDO);
        return rescreenSituationDO;
    }

    /**
     * 设置龋齿数据
     */
    private void setSaprodontia(List<StatConclusion> statConclusions, Integer realScreeningStudentNum, CommonDiseaseScreeningResultStatistic statistic) {
        SaprodontiaDO saprodontiaDO = new SaprodontiaDO();
        int saprodontiaFreeNum = (int)statConclusions.stream().filter(Objects::nonNull).filter(sc -> !sc.getIsSaprodontia() && !sc.getIsSaprodontiaLoss() && !sc.getIsSaprodontiaRepair()).count();
        int saprodontiaNum = (int)statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsSaprodontia).count();
        int saprodontiaLossNum = (int)statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsSaprodontiaLoss).count();
        int saprodontiaRepairNum = (int)statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsSaprodontiaRepair).count();
        int saprodontiaLossAndRepairNum = (int)statConclusions.stream().filter(Objects::nonNull).filter(sc -> sc.getIsSaprodontiaLoss() && sc.getIsSaprodontiaRepair()).count();

        int dmftNum = statConclusions.stream().filter(Objects::nonNull)
                .filter(sc -> sc.getIsSaprodontiaLoss() && sc.getIsSaprodontiaRepair())
                .mapToInt(sc -> sc.getSaprodontiaLossTeeth() + sc.getSaprodontiaRepairTeeth()).sum();

        int sumTeeth = statConclusions.stream().filter(Objects::nonNull)
                .filter(sc -> sc.getIsSaprodontiaLoss() && sc.getIsSaprodontiaRepair() && sc.getIsSaprodontia())
                .mapToInt(sc -> sc.getSaprodontiaLossTeeth() + sc.getSaprodontiaRepairTeeth() + sc.getSaprodontiaTeeth()).sum();

        saprodontiaDO
                .setSaprodontiaFreeNum(saprodontiaFreeNum).setSaprodontiaFreeRatio(MathUtil.ratio(saprodontiaFreeNum,realScreeningStudentNum))
                .setDmftNum(dmftNum).setDmftRatio(MathUtil.ratio(dmftNum,realScreeningStudentNum))
                .setSaprodontiaNum(saprodontiaNum).setSaprodontiaRatio(MathUtil.ratio(saprodontiaNum,realScreeningStudentNum))
                .setSaprodontiaLossNum(saprodontiaLossNum).setSaprodontiaLossRatio(MathUtil.ratio(saprodontiaLossNum,realScreeningStudentNum))
                .setSaprodontiaRepairNum(saprodontiaRepairNum).setSaprodontiaRepairRatio(MathUtil.ratio(saprodontiaRepairNum,realScreeningStudentNum))
                .setSaprodontiaLossAndRepairNum(saprodontiaLossAndRepairNum).setSaprodontiaLossAndRepairRatio(MathUtil.ratio(saprodontiaLossAndRepairNum,realScreeningStudentNum))
                .setSaprodontiaLossAndRepairTeethNum(dmftNum).setSaprodontiaLossAndRepairTeethRatio(MathUtil.ratio(dmftNum,sumTeeth));
        statistic.setSaprodontia(saprodontiaDO);
    }

    /**
     * 设置常见病数据
     */
    private void setCommonDisease(List<StatConclusion> statConclusions, Integer realScreeningStudentNum, CommonDiseaseScreeningResultStatistic statistic) {
        CommonDiseaseDO commonDiseaseDO = new CommonDiseaseDO();
        int overweightNum = (int)statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsOverweight).count();
        int obeseNum = (int)statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsObesity).count();
        int malnourishedNum = (int)statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsMalnutrition).count();
        int stuntingNum = (int)statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsStunting).count();
        int abnormalSpineCurvatureNum = (int)statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsSpinalCurvature).count();
        int highBloodPressureNum = (int)statConclusions.stream().filter(Objects::nonNull).filter(sc->!sc.getIsNormalBloodPressure()).count();
        int reviewStudentNum = (int)statConclusions.stream().filter(Objects::nonNull).filter(StatConclusion::getIsRescreen).count();

         commonDiseaseDO.setOverweightNum(overweightNum).setOverweightRatio(MathUtil.ratio(overweightNum,realScreeningStudentNum))
            .setObeseNum(obeseNum).setObeseRatio(MathUtil.ratio(obeseNum,realScreeningStudentNum))
            .setMalnourishedNum(malnourishedNum).setMalnourishedRatio(MathUtil.ratio(malnourishedNum,realScreeningStudentNum))
            .setStuntingNum(stuntingNum).setStuntingRatio(MathUtil.ratio(stuntingNum,realScreeningStudentNum))
            .setAbnormalSpineCurvatureNum(abnormalSpineCurvatureNum).setAbnormalSpineCurvatureRatio(MathUtil.ratio(abnormalSpineCurvatureNum,realScreeningStudentNum))
            .setHighBloodPressureNum(highBloodPressureNum).setHighBloodPressureRatio(MathUtil.ratio(highBloodPressureNum,realScreeningStudentNum))
            .setReviewStudentNum(reviewStudentNum).setReviewStudentRatio(MathUtil.ratio(reviewStudentNum,realScreeningStudentNum));

        statistic.setCommonDisease(commonDiseaseDO);
    }
    /**
     * 设置问卷调查数据
     */
    private void setQuestionnaire(CommonDiseaseScreeningResultStatistic statistic) {
        QuestionnaireDO questionnaireDO = new QuestionnaireDO();
        Integer num=0;
        String ratio="0.00%";
        questionnaireDO.setEnvHealthInfluenceQuestionnaireNum(num)
                .setEnvHealthInfluenceQuestionnaireRatio(ratio)
                .setSchoolHealthWorkAdministrativeQuestionnaireNum(num)
                .setSchoolHealthWorkAdministrativeQuestionnaireRatio(ratio)
                .setSchoolHealthWorkQuestionnaireNum(num)
                .setSchoolHealthWorkQuestionnaireRatio(ratio)
                .setPoorVisionAndAbnormalCurvatureSpineQuestionnaireNum(num)
                .setPoorVisionAndAbnormalCurvatureSpineQuestionnaireRatio(ratio)
                .setHealthStateQuestionnaireNum(num)
                .setHealthStateQuestionnaireRatio(ratio);

        statistic.setQuestionnaire(questionnaireDO);
    }
}
