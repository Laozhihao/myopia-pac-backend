package com.wupol.myopia.business.api.management.schedule;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.CompareUtil;
import com.wupol.myopia.business.api.management.domain.builder.ScreeningResultStatisticBuilder;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.service.CommonDiseaseScreeningResultStatisticService;
import com.wupol.myopia.business.core.stat.service.VisionScreeningResultStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
    private final VisionScreeningResultStatisticService visionScreeningResultStatisticService;
    private final CommonDiseaseScreeningResultStatisticService commonDiseaseScreeningResultStatisticService;

    /**
     * 按区域统计
     */
    public void districtStatistics(List<Integer> yesterdayScreeningPlanIds) {
        //筛查计划ID 查找筛查通知ID
        List<Integer> screeningNoticeIds = screeningPlanService.getSrcScreeningNoticeIdsByIds(yesterdayScreeningPlanIds);

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
                visionScreeningResultStatisticService.saveVisionScreeningResultStatistic(visionScreeningResultStatistic);
            }
        }
        //常见病筛查
        List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList = commonDiseaseScreeningResultStatistic(screeningTypeStatConclusionMap);
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningResultStatisticList)){
            for ( CommonDiseaseScreeningResultStatistic commonDiseaseScreeningResultStatistic : commonDiseaseScreeningResultStatisticList) {
                commonDiseaseScreeningResultStatisticService.saveCommonDiseaseScreeningResultStatistic(commonDiseaseScreeningResultStatistic);
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
            Map<Integer, List<StatConclusion>> districtStatConclusions = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getDistrictId));

            if(Objects.equals(0,screeningNotice.getScreeningType())){
                //根据地区生成视力筛查统计
                genVisionStatisticsByDistrictId(screeningNoticeId, provinceDistrictId, districtPlanStudentCountMap, visionScreeningResultStatisticList, districtStatConclusions);
            }else {
                //根据地区生成常见病筛查统计
                genCommonDiseaseStatisticsByDistrictId(screeningNoticeId, provinceDistrictId, districtPlanStudentCountMap, commonDiseaseScreeningResultStatisticList, districtStatConclusions);
            }

        });
    }


    /**
     * 根据地区生成视力筛查统计
     */
    private void genVisionStatisticsByDistrictId(Integer screeningNoticeId, Integer districtId, Map<Integer, Long> districtPlanStudentCountMap,
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

        genTotalStatistics(screeningNoticeId, districtId, totalPlanStudentCount, visionScreeningResultStatisticList,null, totalStatConclusions);
        genSelfStatistics(screeningNoticeId, districtId, selfPlanStudentCount, visionScreeningResultStatisticList,null, selfStatConclusions);
        if (totalStatConclusions.size() != selfStatConclusions.size()) {
            //递归统计下层级数据
            childDistricts.forEach(childDistrict -> genVisionStatisticsByDistrictId(screeningNoticeId, childDistrict.getId(), districtPlanStudentCountMap, visionScreeningResultStatisticList, districtStatConclusions));
        }
    }

    /**
     * 根据地区生成常见病筛查统计
     */
    private void genCommonDiseaseStatisticsByDistrictId(Integer screeningNoticeId, Integer districtId, Map<Integer, Long> districtPlanStudentCountMap,
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

        genTotalStatistics(screeningNoticeId, districtId, totalPlanStudentCount, null,commonDiseaseScreeningResultStatisticList, totalStatConclusions);
        genSelfStatistics(screeningNoticeId, districtId, selfPlanStudentCount,null,commonDiseaseScreeningResultStatisticList, selfStatConclusions);
        if (totalStatConclusions.size() != selfStatConclusions.size()) {
            //递归统计下层级数据
            childDistricts.forEach(childDistrict -> genCommonDiseaseStatisticsByDistrictId(screeningNoticeId, childDistrict.getId(), districtPlanStudentCountMap, commonDiseaseScreeningResultStatisticList, districtStatConclusions));
        }
    }

    /**
     * 合计统计
     */
    private void genTotalStatistics(Integer screeningNoticeId, Integer districtId, Integer planStudentNum,
                                    List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                    List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList,
                                    List<StatConclusion> totalStatConclusions) {
        if (CollectionUtils.isEmpty(totalStatConclusions) && planStudentNum == 0) {
            // 计划筛查学生不为0时，即使还没有筛查数据，也要新增统计
            return;
        }
        // 层级总的筛查数据不一定属于同一个任务，所以取默认0
        Integer screeningTaskId = CommonConst.DEFAULT_ID;

        if (Objects.nonNull(visionScreeningResultStatisticList)){
            visionScreeningResultStatisticList.addAll(ScreeningResultStatisticBuilder.buildVisionScreening(screeningNoticeId, screeningTaskId, districtId, Boolean.TRUE, planStudentNum, totalStatConclusions));
        }
        if (Objects.nonNull(commonDiseaseScreeningResultStatisticList)){
            commonDiseaseScreeningResultStatisticList.add(ScreeningResultStatisticBuilder.buildCommonDiseaseScreening(screeningNoticeId, screeningTaskId, districtId, Boolean.TRUE, planStudentNum, totalStatConclusions));
        }
    }

    /**
     * 单条统计
     */
    private void genSelfStatistics(Integer screeningNoticeId, Integer districtId, Integer planStudentNum,
                                   List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                   List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList,
                                   List<StatConclusion> selfStatConclusions) {
        if (CollectionUtils.isEmpty(selfStatConclusions) && planStudentNum == 0) {
            // 计划筛查学生不为0时，即使还没有筛查数据，也要新增统计
            return;
        }

        // 层级自己的筛查数据肯定属于同一个任务，所以只取第一个的就可以
        Integer screeningTaskId = CollectionUtils.isEmpty(selfStatConclusions) ? CommonConst.DEFAULT_ID : selfStatConclusions.get(0).getTaskId();

        if (Objects.nonNull(visionScreeningResultStatisticList)){
            visionScreeningResultStatisticList.addAll(ScreeningResultStatisticBuilder.buildVisionScreening(screeningNoticeId, screeningTaskId, districtId, Boolean.FALSE, planStudentNum, selfStatConclusions));
        }
        if (Objects.nonNull(commonDiseaseScreeningResultStatisticList)){
            commonDiseaseScreeningResultStatisticList.add(ScreeningResultStatisticBuilder.buildCommonDiseaseScreening(screeningNoticeId, screeningTaskId, districtId, Boolean.FALSE, planStudentNum,selfStatConclusions));
        }
    }
}
