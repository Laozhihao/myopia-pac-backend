package com.wupol.myopia.business.api.management.schedule;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.CompareUtil;
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
import org.springframework.beans.BeanUtils;
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
        statistic(statConclusions,visionScreeningResultStatisticList,null);
        return visionScreeningResultStatisticList;
    }

    /**
     * 常见病筛查结果统计
     */
    private List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatistic(Map<Integer, List<StatConclusion>> screeningTypeStatConclusionMap){
        List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList =Lists.newArrayList();
        List<StatConclusion> statConclusions = screeningTypeStatConclusionMap.get(1);
        statistic(statConclusions,null,commonDiseaseScreeningResultStatisticList);
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
                .setIsTotal(Boolean.TRUE);

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
                .setIsTotal(Boolean.FALSE);

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

        //复测数据
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));

        //纳入统计数据
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(Boolean.TRUE, Collections.emptyList());
        Integer realScreeningStudentNum = isRescreenTotalMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();

        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        List<StatConclusion> firstScreening = isRescreenMap.getOrDefault(Boolean.FALSE, Collections.emptyList());
        int validScreeningNum = firstScreening.size();
        List<StatConclusion> rescreening = isRescreenMap.getOrDefault(Boolean.TRUE, Collections.emptyList());
        int validRescreenNum = rescreening.size();
        Integer rescreenNum = (int) statConclusions.stream().filter(StatConclusion::getIsRescreen).count();

        Integer lowVisionNum = (int) statConclusions.stream().filter(StatConclusion::getIsLowVision).count();
        Integer ametropiaNum = (int) statConclusions.stream().filter(StatConclusion::getIsRefractiveError).count();
        Integer anisometropiaNum = (int) statConclusions.stream().filter(StatConclusion::getIsAnisometropia).count();
        double avgLeftVision = statConclusions.stream().mapToDouble(sc->sc.getVisionL().doubleValue()).average().orElse(0);
        double avgRightVision = statConclusions.stream().mapToDouble(sc->sc.getVisionR().doubleValue()).average().orElse(0);

        Map<Integer, Long> visionLabelNumberMap = statConclusions.stream().filter(stat -> Objects.nonNull(stat.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));

        Integer visionLabelZeroSpNum = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO_SP.code, 0L).intValue();
        Integer wearingGlassNum = (int) statConclusions.stream().filter(sc -> sc.getGlassesType() > 0).count();
        Integer treatmentAdviceNum = (int) statConclusions.stream().filter(StatConclusion::getIsRecommendVisit).count();


        Integer wearingGlassRescreenNum = (int) statConclusions.stream().filter(sc->sc.getIsRescreen() && sc.getGlassesType() > 0).count();
        Integer validWearingGlassRescreenNum = (int) statConclusions.stream().filter(sc->sc.getIsRescreen() && sc.getGlassesType() > 0 && sc.getIsValid()).count();

        Integer withoutGlassRescreenNum = (int) statConclusions.stream().filter(sc->sc.getIsRescreen() && sc.getGlassesType() == 0).count();
        Integer validWithoutGlassRescreenNum = (int) statConclusions.stream().filter(sc->sc.getIsRescreen() && sc.getGlassesType() == 0).count();

        int schoolNum = (int)statConclusions.stream().map(StatConclusion::getSchoolId).count();

        VisionScreeningResultStatistic statistic = new VisionScreeningResultStatistic();
        BeanUtils.copyProperties(totalStatistic,statistic);
        Integer planScreeningNum = totalStatistic.getPlanStudentCount();

        if (Objects.equals(totalStatistic.getSchoolType(),5)){
            KindergartenVisionAnalysisDO visionAnalysisDO =new KindergartenVisionAnalysisDO();
            visionAnalysisDO.setLowVisionNum(lowVisionNum)
                    .setLowVisionRatio(ratio(lowVisionNum,validScreeningNum))
                    .setAvgLeftVision(BigDecimal.valueOf(avgLeftVision)).setAvgRightVision(BigDecimal.valueOf(avgRightVision))
                    .setAmetropiaNum(ametropiaNum).setAmetropiaRatio(ratio(ametropiaNum,validScreeningNum))
                    .setAnisometropiaNum(anisometropiaNum).setAnisometropiaRatio(ratio(anisometropiaNum,validScreeningNum))
                    .setMyopiaLevelInsufficientNum(visionLabelZeroSpNum).setMyopiaLevelInsufficientNumRatio(ratio(visionLabelZeroSpNum,validScreeningNum))
                    .setWearingGlassesNum(wearingGlassNum).setWearingGlassesRatio(ratio(wearingGlassNum,validScreeningNum))
                    .setTreatmentAdviceNum(treatmentAdviceNum).setTreatmentAdviceRatio(ratio(treatmentAdviceNum,validScreeningNum))
                    .setSchoolType(8);
            statistic.setVisionAnalysis(visionAnalysisDO);
        }else {
            PrimarySchoolAndAboveVisionAnalysisDO visionAnalysisDO = new PrimarySchoolAndAboveVisionAnalysisDO();
            statistic.setVisionAnalysis(visionAnalysisDO);
        }

        setVisionWarning(validScreeningNum,visionLabelNumberMap, statistic);

        setRescreenSituation(validRescreenNum, rescreenNum, wearingGlassRescreenNum, validWearingGlassRescreenNum,
                withoutGlassRescreenNum, validWithoutGlassRescreenNum,statistic);

        //基础数据
        statistic.setScreeningNoticeId(totalStatistic.getScreeningNotice().getScreeningType())
                .setScreeningTaskId(totalStatistic.getScreeningTaskId())
                .setScreeningPlanId(totalStatistic.getScreeningPlanId())
                .setScreeningType(totalStatistic.getScreeningNotice().getScreeningType())
                .setIsTotal(totalStatistic.getIsTotal())
                .setSchoolId(totalStatistic.getSchoolId())
                .setSchoolType(totalStatistic.getSchoolType())
                .setDistrictId(totalStatistic.getDistrictId())
                .setCreateTime(new Date())
                .setUpdateTime(statistic.getCreateTime());

        statistic.setSchoolNum(schoolNum)
                .setPlanScreeningNum(planScreeningNum)
                .setRealScreeningNum(realScreeningStudentNum)
                .setFinishRatio(ratio(realScreeningStudentNum,planScreeningNum))
                .setValidScreeningNum(validScreeningNum)
                .setValidScreeningRatio(ratio(validScreeningNum,realScreeningStudentNum));

        visionScreeningResultStatisticList.add(statistic);

    }

    public String ratio(Integer numerator, Integer denominator){
        return MathUtil.divide(numerator, denominator).toString()+"%";
    }

    private Integer getKey(Integer schoolAge){
        return Objects.equals(schoolAge,5)?schoolAge:0;
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

        //复测数据
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));

        //纳入统计数据
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(Boolean.TRUE, Collections.emptyList());
        Integer realScreeningStudentNum = isRescreenTotalMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();

        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        List<StatConclusion> firstScreening = isRescreenMap.getOrDefault(Boolean.FALSE, Collections.emptyList());
        int validScreeningNum = firstScreening.size();
        List<StatConclusion> rescreening = isRescreenMap.getOrDefault(Boolean.TRUE, Collections.emptyList());
        int validRescreenNum = rescreening.size();
        Integer rescreenNum = (int) statConclusions.stream().filter(StatConclusion::getIsRescreen).count();

        Integer lowVisionNum = (int) statConclusions.stream().filter(StatConclusion::getIsLowVision).count();
        Integer ametropiaNum = (int) statConclusions.stream().filter(StatConclusion::getIsRefractiveError).count();
        Integer anisometropiaNum = (int) statConclusions.stream().filter(StatConclusion::getIsAnisometropia).count();
        double avgLeftVision = statConclusions.stream().mapToDouble(sc->sc.getVisionL().doubleValue()).average().orElse(0);
        double avgRightVision = statConclusions.stream().mapToDouble(sc->sc.getVisionR().doubleValue()).average().orElse(0);

        Map<Integer, Long> visionLabelNumberMap = statConclusions.stream().filter(stat -> Objects.nonNull(stat.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        Integer visionLabelZeroSpNum = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO_SP.code, 0L).intValue();
        Integer wearingGlassNum = (int) statConclusions.stream().filter(sc -> sc.getGlassesType() > 0).count();
        Integer treatmentAdviceNum = (int) statConclusions.stream().filter(StatConclusion::getIsRecommendVisit).count();


        Integer wearingGlassRescreenNum = (int) statConclusions.stream().filter(sc->sc.getIsRescreen() && sc.getGlassesType() > 0).count();
        Integer validWearingGlassRescreenNum = (int) statConclusions.stream().filter(sc->sc.getIsRescreen() && sc.getGlassesType() > 0 && sc.getIsValid()).count();

        Integer withoutGlassRescreenNum = (int) statConclusions.stream().filter(sc->sc.getIsRescreen() && sc.getGlassesType() == 0).count();
        Integer validWithoutGlassRescreenNum = (int) statConclusions.stream().filter(sc->sc.getIsRescreen() && sc.getGlassesType() == 0).count();

        int schoolNum = (int)statConclusions.stream().map(StatConclusion::getSchoolId).count();

        CommonDiseaseScreeningResultStatistic statistic = new CommonDiseaseScreeningResultStatistic();
        BeanUtils.copyProperties(totalStatistic,statistic);
        Integer planScreeningNum = totalStatistic.getPlanStudentCount();

        if (Objects.equals(totalStatistic.getSchoolType(),5)){
            setKindergartenVisionAnalysis(validScreeningNum, lowVisionNum, ametropiaNum, anisometropiaNum, avgLeftVision, avgRightVision, visionLabelZeroSpNum, wearingGlassNum, treatmentAdviceNum, statistic);
        }else {
            PrimarySchoolAndAboveVisionAnalysisDO visionAnalysisDO = new PrimarySchoolAndAboveVisionAnalysisDO();
            statistic.setVisionAnalysis(visionAnalysisDO);
        }

        setVisionWarning(validScreeningNum,visionLabelNumberMap, statistic);

        setRescreenSituation(validRescreenNum, rescreenNum, wearingGlassRescreenNum,
                validWearingGlassRescreenNum, withoutGlassRescreenNum, validWithoutGlassRescreenNum,statistic);

        setSaprodontia(statConclusions, realScreeningStudentNum, statistic);

        setCommonDisease(statConclusions, realScreeningStudentNum, statistic);

        setQuestionnaire(statistic);

        //基础数据
        statistic.setScreeningNoticeId(totalStatistic.getScreeningNotice().getScreeningType())
                .setScreeningTaskId(totalStatistic.getScreeningTaskId())
                .setScreeningPlanId(totalStatistic.getScreeningPlanId())
                .setScreeningType(totalStatistic.getScreeningNotice().getScreeningType())
                .setIsTotal(totalStatistic.getIsTotal())
                .setSchoolId(totalStatistic.getSchoolId())
                .setSchoolType(totalStatistic.getSchoolType())
                .setDistrictId(totalStatistic.getDistrictId())
                .setCreateTime(new Date())
                .setUpdateTime(statistic.getCreateTime());

        statistic.setSchoolNum(schoolNum)
                .setPlanScreeningNum(planScreeningNum)
                .setRealScreeningNum(realScreeningStudentNum)
                .setFinishRatio(ratio(realScreeningStudentNum,planScreeningNum))
                .setValidScreeningNum(validScreeningNum)
                .setValidScreeningRatio(ratio(validScreeningNum,realScreeningStudentNum))
                ;

        commonDiseaseScreeningResultStatisticList.add(statistic);

    }

    private void setKindergartenVisionAnalysis(int validScreeningNum, Integer lowVisionNum, Integer ametropiaNum, Integer anisometropiaNum, double avgLeftVision, double avgRightVision, Integer visionLabelZeroSpNum, Integer wearingGlassNum, Integer treatmentAdviceNum, CommonDiseaseScreeningResultStatistic statistic) {
        KindergartenVisionAnalysisDO visionAnalysisDO =new KindergartenVisionAnalysisDO();
        visionAnalysisDO.setLowVisionNum(lowVisionNum)
                .setLowVisionRatio(ratio(lowVisionNum,validScreeningNum))
                .setAvgLeftVision(BigDecimal.valueOf(avgLeftVision)).setAvgRightVision(BigDecimal.valueOf(avgRightVision))
                .setAmetropiaNum(ametropiaNum).setAmetropiaRatio(ratio(ametropiaNum,validScreeningNum))
                .setAnisometropiaNum(anisometropiaNum).setAnisometropiaRatio(ratio(anisometropiaNum,validScreeningNum))
                .setMyopiaLevelInsufficientNum(visionLabelZeroSpNum).setMyopiaLevelInsufficientNumRatio(ratio(visionLabelZeroSpNum,validScreeningNum))
                .setWearingGlassesNum(wearingGlassNum).setWearingGlassesRatio(ratio(wearingGlassNum,validScreeningNum))
                .setTreatmentAdviceNum(treatmentAdviceNum).setTreatmentAdviceRatio(ratio(treatmentAdviceNum,validScreeningNum))
                .setSchoolType(8);
        statistic.setVisionAnalysis(visionAnalysisDO);
    }

    private void setVisionWarning(int validScreeningNum,Map<Integer, Long> visionLabelNumberMap,
                                  VisionScreeningResultStatistic statistic) {
        VisionWarningDO visionWarningDO = new VisionWarningDO();
        Integer visionLabel0Num = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO.code, 0L).intValue();
        Integer visionLabel1Num = visionLabelNumberMap.getOrDefault(WarningLevel.ONE.code, 0L).intValue();
        Integer visionLabel2Num = visionLabelNumberMap.getOrDefault(WarningLevel.TWO.code, 0L).intValue();
        Integer visionLabel3Num = visionLabelNumberMap.getOrDefault(WarningLevel.THREE.code, 0L).intValue();
        Integer visionWarningNum =visionLabel0Num+visionLabel1Num+visionLabel2Num+visionLabel3Num;

        visionWarningDO.setVisionWarningNum(visionWarningNum)
                .setVisionLabel0Num(visionLabel0Num).setVisionLabel0Ratio(ratio(visionLabel0Num,validScreeningNum))
                .setVisionLabel1Num(visionLabel1Num).setVisionLabel1Ratio(ratio(visionLabel1Num,validScreeningNum))
                .setVisionLabel2Num(visionLabel2Num).setVisionLabel2Ratio(ratio(visionLabel2Num,validScreeningNum))
                .setVisionLabel3Num(visionLabel3Num).setVisionLabel3Ratio(ratio(visionLabel3Num,validScreeningNum));
        statistic.setVisionWarning(visionWarningDO);
    }

    private RescreenSituationDO setRescreenSituation(int validRescreenNum, Integer rescreenNum, Integer wearingGlassRescreenNum,
                                                     Integer validWearingGlassRescreenNum, Integer withoutGlassRescreenNum,
                                                     Integer validWithoutGlassRescreenNum,VisionScreeningResultStatistic statistic) {
        RescreenSituationDO rescreenSituationDO = new RescreenSituationDO();
        rescreenSituationDO.setRetestNum(validRescreenNum).setRetestRatio(ratio(validRescreenNum, rescreenNum))
                .setWearingGlassRetestNum(validWearingGlassRescreenNum).setWearingGlassRetestRatio(ratio(validWearingGlassRescreenNum, wearingGlassRescreenNum))
                .setWithoutGlassRetestNum(validWithoutGlassRescreenNum).setWithoutGlassRetestRatio(ratio(validWithoutGlassRescreenNum, withoutGlassRescreenNum))
                .setRescreeningItemNum(0).setErrorItemNum(0).setIncidence("");
        statistic.setRescreenSituation(rescreenSituationDO);
        return rescreenSituationDO;
    }

    private void setSaprodontia(List<StatConclusion> statConclusions, Integer realScreeningStudentNum, CommonDiseaseScreeningResultStatistic statistic) {
        SaprodontiaDO saprodontiaDO = new SaprodontiaDO();
        int saprodontiaFreeNum = (int)statConclusions.stream().filter(sc -> !sc.getIsSaprodontia() && !sc.getIsSaprodontiaLoss() && !sc.getIsSaprodontiaRepair()).count();
        int saprodontiaNum = (int)statConclusions.stream().filter(StatConclusion::getIsSaprodontia).count();
        int saprodontiaLossNum = (int)statConclusions.stream().filter(StatConclusion::getIsSaprodontiaLoss).count();
        int saprodontiaRepairNum = (int)statConclusions.stream().filter(StatConclusion::getIsSaprodontiaRepair).count();
        int saprodontiaLossAndRepairNum = (int)statConclusions.stream().filter(sc -> sc.getIsSaprodontiaLoss() && sc.getIsSaprodontiaRepair()).count();

        int dmftNum = statConclusions.stream()
                .filter(sc -> sc.getIsSaprodontiaLoss() && sc.getIsSaprodontiaRepair())
                .mapToInt(sc -> sc.getSaprodontiaLossTeeth() + sc.getSaprodontiaRepairTeeth()).sum();

        int sumTeeth = statConclusions.stream()
                .filter(sc -> sc.getIsSaprodontiaLoss() && sc.getIsSaprodontiaRepair() && sc.getIsSaprodontia())
                .mapToInt(sc -> sc.getSaprodontiaLossTeeth() + sc.getSaprodontiaRepairTeeth() + sc.getSaprodontiaTeeth()).sum();

        saprodontiaDO
                .setSaprodontiaFreeNum(saprodontiaFreeNum).setSaprodontiaFreeRatio(ratio(saprodontiaFreeNum,realScreeningStudentNum))
                .setDmftNum(dmftNum).setDmftRatio(ratio(dmftNum,realScreeningStudentNum))
        .setSaprodontiaNum(saprodontiaNum).setSaprodontiaRatio(ratio(saprodontiaNum,realScreeningStudentNum))
        .setSaprodontiaLossNum(saprodontiaLossNum).setSaprodontiaLossRatio(ratio(saprodontiaLossNum,realScreeningStudentNum))
        .setSaprodontiaRepairNum(saprodontiaRepairNum).setSaprodontiaRepairRatio(ratio(saprodontiaRepairNum,realScreeningStudentNum))
        .setSaprodontiaLossAndRepairNum(saprodontiaLossAndRepairNum).setSaprodontiaLossAndRepairRatio(ratio(saprodontiaLossAndRepairNum,realScreeningStudentNum))
        .setSaprodontiaLossAndRepairTeethNum(dmftNum).setSaprodontiaLossAndRepairTeethRatio(ratio(dmftNum,sumTeeth));
        statistic.setSaprodontia(saprodontiaDO);
    }

    private void setCommonDisease(List<StatConclusion> statConclusions, Integer realScreeningStudentNum, CommonDiseaseScreeningResultStatistic statistic) {
        CommonDiseaseDO commonDiseaseDO = new CommonDiseaseDO();
        int overweightNum = (int)statConclusions.stream().filter(StatConclusion::getIsOverweight).count();
        int obeseNum = (int)statConclusions.stream().filter(StatConclusion::getIsObesity).count();
        int malnourishedNum = (int)statConclusions.stream().filter(StatConclusion::getIsMalnutrition).count();
        int stuntingNum = (int)statConclusions.stream().filter(StatConclusion::getIsStunting).count();
        int abnormalSpineCurvatureNum = (int)statConclusions.stream().filter(StatConclusion::getIsSpinalCurvature).count();
        int highBloodPressureNum = (int)statConclusions.stream().filter(sc->!sc.getIsNormalBloodPressure()).count();
        int reviewStudentNum = (int)statConclusions.stream().filter(StatConclusion::getIsRescreen).count();
        commonDiseaseDO.setOverweightNum(overweightNum).setOverweightRatio(ratio(overweightNum,realScreeningStudentNum))
        .setObeseNum(obeseNum).setObeseRatio(ratio(obeseNum,realScreeningStudentNum))
        .setMalnourishedNum(malnourishedNum).setMalnourishedRatio(ratio(malnourishedNum,realScreeningStudentNum))
        .setStuntingNum(stuntingNum).setStuntingRatio(ratio(stuntingNum,realScreeningStudentNum))
        .setAbnormalSpineCurvatureNum(abnormalSpineCurvatureNum).setAbnormalSpineCurvatureRatio(ratio(abnormalSpineCurvatureNum,realScreeningStudentNum))
        .setHighBloodPressureNum(highBloodPressureNum).setHighBloodPressureRatio(ratio(highBloodPressureNum,realScreeningStudentNum))
        .setReviewStudentNum(reviewStudentNum).setReviewStudentRatio(ratio(reviewStudentNum,realScreeningStudentNum));

        statistic.setCommonDisease(commonDiseaseDO);
    }

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
