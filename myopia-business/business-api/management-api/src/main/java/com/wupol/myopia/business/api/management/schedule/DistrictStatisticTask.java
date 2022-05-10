package com.wupol.myopia.business.api.management.schedule;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.CompareUtil;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.business.api.management.domain.bo.StatisticResultBO;
import com.wupol.myopia.business.api.management.domain.builder.ScreeningResultStatisticBuilder;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public void districtStatistics(List<Integer> screeningPlanIds) {
        if (CollectionUtil.isEmpty(screeningPlanIds)){
            return;
        }
        //筛查计划ID 查找筛查通知ID
        List<Integer> screeningNoticeIds = screeningPlanService.getSrcScreeningNoticeIdsByIds(screeningPlanIds);
        if(CollectionUtil.isEmpty(screeningNoticeIds)){
            log.error("按地区-未找到筛查通知数据，planIds:{}",CollectionUtil.join(screeningPlanIds,","));
            return;
        }
        screeningNoticeIds = screeningNoticeIds.stream().filter(id-> !CommonConst.DEFAULT_ID.equals(id)).collect(Collectors.toList());
        if(CollectionUtil.isEmpty(screeningNoticeIds)){
            log.error("按地区-未找到筛查通知数据，planIds:{}",CollectionUtil.join(screeningPlanIds,","));
            return;
        }

        //筛查通知ID 查出筛查数据结论
        List<StatConclusion> statConclusionList = statConclusionService.getBySrcScreeningNoticeIds(screeningNoticeIds);
        if (CollectionUtil.isEmpty(statConclusionList)){
            log.error("未找到筛查数据结论，screeningNoticeIds:{}",CollectionUtil.join(screeningNoticeIds,","));
            return;
        }


        List<VisionScreeningResultStatistic> visionScreeningResultStatisticList = Lists.newArrayList();
        List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList = Lists.newArrayList();
        screeningResultStatistic(statConclusionList,visionScreeningResultStatisticList,commonDiseaseScreeningResultStatisticList);

        //视力筛查
        for (VisionScreeningResultStatistic visionScreeningResultStatistic : visionScreeningResultStatisticList) {
            screeningResultStatisticService.saveVisionScreeningResultStatistic(visionScreeningResultStatistic);
        }
        //常见病筛查
        for ( CommonDiseaseScreeningResultStatistic commonDiseaseScreeningResultStatistic : commonDiseaseScreeningResultStatisticList) {
            screeningResultStatisticService.saveCommonDiseaseScreeningResultStatistic(commonDiseaseScreeningResultStatistic);
        }

    }

    /**
     * 视力筛查结果统计
     */
    private void screeningResultStatistic(List<StatConclusion> statConclusionList,
                                                List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                                List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> screeningTypeStatConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getScreeningType));
        screeningTypeStatConclusionMap.forEach((screeningType,statConclusions)->{
            if(Objects.equals(0,screeningType)){
                statistic(statConclusions,visionScreeningResultStatisticList,null);
            }else {
                statistic(statConclusions,null,commonDiseaseScreeningResultStatisticList);
            }
        });
    }


    /**
     * 统计逻辑
     */
    private void statistic(List<StatConclusion> statConclusionList,
                           List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                           List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList){
        if (CollectionUtil.isEmpty(statConclusionList) ){
            return;
        }
        //根据筛查通知ID分组
        Map<Integer, List<StatConclusion>> statConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSrcScreeningNoticeId));

        statConclusionMap.forEach((screeningNoticeId,statConclusions)->{

            // 筛查通知中的学校所在地区层级的计划学生总数
            Map<Integer, List<ScreeningPlanSchoolStudent>> districtPlanStudentCountMap = screeningPlanSchoolStudentService.getPlanStudentCountBySrcScreeningNoticeId(screeningNoticeId);

            //查出通知对应的地区顶级层级：从任务所在省级开始（因为筛查计划可选全省学校）
            ScreeningNotice screeningNotice = screeningNoticeService.getById(screeningNoticeId);
            if (Objects.isNull(screeningNotice)){
                return;
            }
            Integer provinceDistrictId = districtService.getProvinceId(screeningNotice.getDistrictId());

            //同一个筛查通知下不同地区筛查数据结论 ,根据地区分组
            Map<Integer, List<StatConclusion>> districtStatConclusionMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getDistrictId));

            //根据地区生成筛查统计
            genStatisticsByDistrictId(screeningNotice, provinceDistrictId, districtPlanStudentCountMap,districtStatConclusionMap, visionScreeningResultStatisticList, commonDiseaseScreeningResultStatisticList);

        });
    }


    /**
     * 根据地区生成视力筛查统计
     */
    private void genStatisticsByDistrictId(ScreeningNotice screeningNotice, Integer districtId, Map<Integer, List<ScreeningPlanSchoolStudent>> districtPlanStudentCountMap,
                                           Map<Integer, List<StatConclusion>> districtStatConclusions,
                                           List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                           List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList) {
        if (ObjectsUtil.hasNull(screeningNotice,districtId,districtPlanStudentCountMap,districtStatConclusions)){
            return;
        }

        List<District> childDistricts = Lists.newArrayList();
        List<Integer> childDistrictIds = Lists.newArrayList();
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
        List<ScreeningPlanSchoolStudent> totalPlanStudentCountList = haveStudentDistrictIds.stream().flatMap(id -> {
            List<ScreeningPlanSchoolStudent> planSchoolStudentList = districtPlanStudentCountMap.get(id);
            if (CollectionUtil.isNotEmpty(planSchoolStudentList)) {
                return planSchoolStudentList.stream();
            } else {
                return Stream.empty();
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        // 层级自身数据
        List<StatConclusion> selfStatConclusions = districtStatConclusions.getOrDefault(districtId, Collections.emptyList());
        List<ScreeningPlanSchoolStudent> selfPlanStudentCount = districtPlanStudentCountMap.getOrDefault(districtId, Collections.emptyList());

        StatisticResultBO totalStatistic = new StatisticResultBO()
                .setScreeningNoticeId(screeningNotice.getId())
                .setScreeningType(screeningNotice.getScreeningType())
                .setDistrictId(districtId)
                .setPlanSchoolStudentList(totalPlanStudentCountList)
                .setStatConclusions(totalStatConclusions);

        genTotalStatistics(totalStatistic, visionScreeningResultStatisticList,commonDiseaseScreeningResultStatisticList);

        StatisticResultBO selfStatistic = new StatisticResultBO()
                .setScreeningNoticeId(screeningNotice.getId())
                .setScreeningType(screeningNotice.getScreeningType())
                .setDistrictId(districtId)
                .setPlanSchoolStudentList(selfPlanStudentCount)
                .setStatConclusions(selfStatConclusions);
        genSelfStatistics(selfStatistic, visionScreeningResultStatisticList,commonDiseaseScreeningResultStatisticList);

        if (totalStatConclusions.size() != selfStatConclusions.size()) {
            //递归统计下层级数据
            childDistricts.forEach(childDistrict -> genStatisticsByDistrictId(screeningNotice, childDistrict.getId(), districtPlanStudentCountMap, districtStatConclusions,visionScreeningResultStatisticList,commonDiseaseScreeningResultStatisticList ));
        }
    }


    /**
     * 合计统计
     */
    private void genTotalStatistics(StatisticResultBO totalStatistic,
                                    List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                    List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList) {

        if (CollectionUtils.isEmpty(totalStatistic.getStatConclusions()) ) {
            // 计划筛查学生不为0时，即使还没有筛查数据，也要新增统计
            return;
        }
        // 层级总的筛查数据不一定属于同一个任务，所以取默认0
        totalStatistic.setScreeningTaskId(CommonConst.DEFAULT_ID)
                .setScreeningPlanId(CommonConst.DEFAULT_ID)
                .setIsTotal(Boolean.TRUE).setSchoolId(-1).setScreeningOrgId(-1);

        if (Objects.nonNull(visionScreeningResultStatisticList)){
            List<VisionScreeningResultStatistic> list= Lists.newArrayList();
            buildScreening(totalStatistic,list,null);
            visionScreeningResultStatisticList.addAll(list);
        }

        if (Objects.nonNull(commonDiseaseScreeningResultStatisticList)){
            List<CommonDiseaseScreeningResultStatistic> list= Lists.newArrayList();
            buildScreening(totalStatistic,null,list);
            commonDiseaseScreeningResultStatisticList.addAll(list);
        }


    }

    /**
     * 单条统计
     */
    private void genSelfStatistics(StatisticResultBO selfStatistic,
                                   List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                   List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList) {
        List<StatConclusion> statConclusions = selfStatistic.getStatConclusions();
        if (CollectionUtils.isEmpty(statConclusions)) {
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
            List<VisionScreeningResultStatistic> list= Lists.newArrayList();
            buildScreening(selfStatistic,list,null);
            visionScreeningResultStatisticList.addAll(list);
        }

        if (Objects.nonNull(commonDiseaseScreeningResultStatisticList)){
            List<CommonDiseaseScreeningResultStatistic> list= Lists.newArrayList();
            buildScreening(selfStatistic,null,list);
            commonDiseaseScreeningResultStatisticList.addAll(list);
        }

    }


    /**
     * 按区域 - 视力筛查数据统计
     */
    private void buildScreening(StatisticResultBO statistic,
                                      List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                      List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList) {

        List<StatConclusion> statConclusions = statistic.getStatConclusions();
        if (CollectionUtil.isEmpty(statConclusions)){
            return;
        }
        ScreeningResultStatisticBuilder.screening(visionScreeningResultStatisticList,commonDiseaseScreeningResultStatisticList,statistic,statConclusions);
    }

}
