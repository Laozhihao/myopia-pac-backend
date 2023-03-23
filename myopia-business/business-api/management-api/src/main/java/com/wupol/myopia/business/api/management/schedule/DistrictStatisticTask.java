package com.wupol.myopia.business.api.management.schedule;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.aggregation.stat.facade.StatFacade;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
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
    private final ScreeningResultStatisticService screeningResultStatisticService;
    private final StatFacade statFacade;

    /**
     * 按区域统计
     * @param screeningPlanIds 筛查计划ID集合
     * @param excludePlanIds   排除的筛查计划ID集合
     */
    public void districtStatistics(List<Integer> screeningPlanIds, List<Integer> excludePlanIds) {
        if (CollUtil.isEmpty(screeningPlanIds)){
            return;
        }
        //筛查计划ID 查找筛查通知ID
        List<Integer> screeningNoticeIds = screeningPlanService.getSrcScreeningNoticeIdsOfReleasePlanByPlanIds(screeningPlanIds);
        if(CollUtil.isEmpty(screeningNoticeIds)){
            log.error("按地区-未找到关联筛查通知，planIds:{}",CollUtil.join(screeningPlanIds,","));
            return;
        }
        screeningNoticeIds = screeningNoticeIds.stream().filter(id-> !CommonConst.DEFAULT_ID.equals(id)).collect(Collectors.toList());
        if(CollUtil.isEmpty(screeningNoticeIds)){
            log.error("按地区-未找到关联筛查通知，planIds:{}",CollUtil.join(screeningPlanIds,","));
            return;
        }
        districtStatisticsByNoticeIds(screeningNoticeIds, excludePlanIds);
    }

    /**
     * 按区域统计
     *
     * @param screeningNoticeIds    筛查通知ID集
     * @param excludePlanIds        排除的筛查计划ID集
     * @return void
     **/
    public void districtStatisticsByNoticeIds(List<Integer> screeningNoticeIds, List<Integer> excludePlanIds) {

        //筛查通知ID 查出筛查数据结论
        List<StatConclusion> statConclusionList = statConclusionService.listOfReleasePlanByScreeningNoticeIds(screeningNoticeIds, excludePlanIds);
        if (CollUtil.isEmpty(statConclusionList)){
            log.error("未找到筛查数据结论，screeningNoticeIds:{}",CollUtil.join(screeningNoticeIds,","));
            return;
        }

        List<VisionScreeningResultStatistic> visionScreeningResultStatisticList = Lists.newArrayList();
        List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList = Lists.newArrayList();
        statFacade.screeningResultStatistic(statConclusionList,Boolean.FALSE,visionScreeningResultStatisticList,commonDiseaseScreeningResultStatisticList);

        //视力筛查
        for (VisionScreeningResultStatistic visionScreeningResultStatistic : visionScreeningResultStatisticList) {
            screeningResultStatisticService.saveVisionScreeningResultStatistic(visionScreeningResultStatistic);
        }
        //常见病筛查
        for ( CommonDiseaseScreeningResultStatistic commonDiseaseScreeningResultStatistic : commonDiseaseScreeningResultStatisticList) {
            screeningResultStatisticService.saveCommonDiseaseScreeningResultStatistic(commonDiseaseScreeningResultStatistic);
        }

    }

}
