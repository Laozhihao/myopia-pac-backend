package com.wupol.myopia.business.api.management.domain.builder;

import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.stat.domain.model.DistrictMonitorStatistic;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/4/27
 **/
public class DistrictMonitorStatisticBuilder {

    public static DistrictMonitorStatistic build(Integer screeningNoticeId, Integer screeningTaskId, Integer districtId, Integer isTotal,
                                                 List<StatConclusion> statConclusions, Integer planScreeningNumbers, Integer realScreeningNumbers) {
        DistrictMonitorStatistic statistic = new DistrictMonitorStatistic();
        Map<Boolean, Long> isWearGlassNumMap = statConclusions.stream().collect(Collectors.groupingBy(statConclusion -> statConclusion.getGlassesType()>0, Collectors.counting()));
        Integer withoutGlassDsn = isWearGlassNumMap.getOrDefault(false, 0L).intValue();
        Integer wearingGlassDsn = isWearGlassNumMap.getOrDefault(true, 0L).intValue();
        Integer rescreeningItemNumbers = withoutGlassDsn * 4 + wearingGlassDsn * 6;
        Integer errorNumbers = statConclusions.stream().mapToInt(StatConclusion::getRescreenErrorNum).sum();
        int dsn = statConclusions.size();
        statistic.setScreeningNoticeId(screeningNoticeId).setScreeningTaskId(screeningTaskId).setDistrictId(districtId).setIsTotal(isTotal)
                .setFinishRatio(MathUtil.divide(realScreeningNumbers, planScreeningNumbers))
                .setWithoutGlassDsn(withoutGlassDsn).setWithoutGlassDsin(4)
                .setWearingGlassDsn(wearingGlassDsn).setWearingGlassDsin(6)
                .setDsn(dsn).setRescreeningItemNumbers(rescreeningItemNumbers)
                .setErrorNumbers(errorNumbers).setErrorRatio(MathUtil.divide(errorNumbers, rescreeningItemNumbers))
                .setPlanScreeningNumbers(planScreeningNumbers).setRealScreeningNumbers(realScreeningNumbers);
        //TODO investigationNumbers暂时处理为0
        statistic.setInvestigationNumbers(0);
        return statistic;
    }
}
