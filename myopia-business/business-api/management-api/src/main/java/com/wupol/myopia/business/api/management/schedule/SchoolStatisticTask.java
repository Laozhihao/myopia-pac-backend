package com.wupol.myopia.business.api.management.schedule;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.aggregation.stat.facade.StatFacade;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 按学校统计
 *
 * @author hang.yuan 2022/4/14 10:49
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Component
public class SchoolStatisticTask {

    private final StatConclusionService statConclusionService;
    private final ScreeningResultStatisticService screeningResultStatisticService;
    private final StatFacade statFacade;


    /**
     * 按学校统计
     * @param yesterdayScreeningPlanIds 筛查计划ID集合
     */
    public void schoolStatistics(List<Integer> yesterdayScreeningPlanIds) {

        //根据筛查计划ID 获取筛查数据结论
        List<StatConclusion> statConclusions = statConclusionService.getByScreeningPlanIds(yesterdayScreeningPlanIds);
        if(CollUtil.isEmpty(statConclusions)){
            log.error("按学校-未找到筛查数据的结论数据，planIds:{}",CollUtil.join(yesterdayScreeningPlanIds,","));
            return;
        }

        List<VisionScreeningResultStatistic> visionScreeningResultStatisticList =Lists.newArrayList();
        List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList =Lists.newArrayList();
        statFacade.screeningResultStatistic(statConclusions,Boolean.TRUE,visionScreeningResultStatisticList,commonDiseaseScreeningResultStatisticList);

        //视力筛查
        for (VisionScreeningResultStatistic visionScreeningResultStatistic : visionScreeningResultStatisticList) {
            screeningResultStatisticService.saveVisionScreeningResultStatistic(visionScreeningResultStatistic);
        }
        //常见病筛查
        for (CommonDiseaseScreeningResultStatistic commonDiseaseScreeningResultStatistic : commonDiseaseScreeningResultStatisticList) {
            screeningResultStatisticService.saveCommonDiseaseScreeningResultStatistic(commonDiseaseScreeningResultStatistic);
        }

    }

}
