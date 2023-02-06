package com.wupol.myopia.business.api.management.domain.builder;

import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.stat.domain.model.SchoolMonitorStatistic;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/4/27
 **/
public final class SchoolMonitorStatisticBuilder {

    private SchoolMonitorStatisticBuilder() {

    }

    /**
     * 构建参数
     * @param school
     * @param screeningOrg
     * @param screeningNoticeId
     * @param screeningTaskId
     * @param statConclusions
     * @param planScreeningNumbers
     * @param realScreeningNumbers
     * @return
     */
    public static SchoolMonitorStatistic build(School school, ScreeningOrganization screeningOrg,
                                               Integer screeningNoticeId, Integer screeningTaskId, Integer screeningPlanId,
                                               List<StatConclusion> statConclusions, Integer planScreeningNumbers, Integer realScreeningNumbers,
                                               Integer screeningOrgType) {
        SchoolMonitorStatistic statistic = new SchoolMonitorStatistic();
        Map<Boolean, Long> isWearGlassNumMap = statConclusions.stream().collect(Collectors.groupingBy(statConclusion -> statConclusion.getGlassesType()>0, Collectors.counting()));
        Integer withoutGlassDsn = isWearGlassNumMap.getOrDefault(false, 0L).intValue();
        Integer wearingGlassDsn = isWearGlassNumMap.getOrDefault(true, 0L).intValue();
        Integer rescreeningItemNumbers = withoutGlassDsn * 4 + wearingGlassDsn * 6;
        Integer errorNumbers = statConclusions.stream().mapToInt(sc-> Optional.ofNullable(sc.getRescreenErrorNum()).orElse(0)).sum();
        int dsn = statConclusions.size();
        statistic.setSchoolId(school.getId()).setSchoolName(school.getName()).setSchoolType(school.getType())
                .setScreeningNoticeId(screeningNoticeId).setScreeningTaskId(screeningTaskId)
                .setScreeningPlanId(screeningPlanId).setDistrictId(school.getDistrictId())
                .setFinishRatio(MathUtil.divide(realScreeningNumbers, planScreeningNumbers))
                .setWithoutGlassDsn(withoutGlassDsn).setWithoutGlassDsin(4)
                .setWearingGlassDsn(wearingGlassDsn).setWearingGlassDsin(6)
                .setDsn(dsn).setRescreeningItemNumbers(rescreeningItemNumbers)
                .setErrorNumbers(errorNumbers).setErrorRatio(MathUtil.divide(errorNumbers, rescreeningItemNumbers))
                .setPlanScreeningNumbers(planScreeningNumbers).setRealScreeningNumbers(realScreeningNumbers);
        //TODO investigationNumbers暂时处理为0
        statistic.setInvestigationNumbers(0);
        if (Objects.equals(ScreeningOrgTypeEnum.ORG.getType(), screeningOrgType)) {
            statistic.setScreeningOrgId(screeningOrg.getId());
            statistic.setScreeningOrgName(screeningOrg.getName());
        }
        if (Objects.equals(ScreeningOrgTypeEnum.SCHOOL.getType(), screeningOrgType)) {
            statistic.setScreeningOrgId(school.getId());
            statistic.setScreeningOrgName(school.getName());
        }
        return statistic;
    }

}
