package com.wupol.myopia.business.api.school.management.facade;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.api.school.management.domain.vo.ScreeningPlanVO;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningBizTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 学校筛查统计门面
 *
 * @author hang.yuan 2022/9/16 20:33
 */
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class SchoolScreeningStatisticFacade {

    private final ScreeningPlanService screeningPlanService;
    private final ScreeningOrganizationService screeningOrganizationService;
    private final ScreeningResultStatisticService screeningResultStatisticService;


    public ScreeningPlanVO getPlanInfo(Integer screeningPlanId,CurrentUser currentUser) {
        ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
        List<ScreeningResultStatistic> screeningResultStatisticList = screeningResultStatisticService.listByPlanIdAndSchoolIdAndOrgId(screeningPlanId, currentUser.getOrgId(), screeningPlan.getScreeningOrgId());
        String screeningOrgName;
        if (Objects.equals(screeningPlan.getScreeningOrgId(),currentUser.getOrgId())){
            screeningOrgName = "本校";
        }else {
            screeningOrgName = screeningOrganizationService.getNameById(screeningPlan.getScreeningOrgId());
        }
        return buildScreeningPlanVO(screeningPlan,screeningOrgName,screeningResultStatisticList);
    }

    private ScreeningPlanVO buildScreeningPlanVO(ScreeningPlan screeningPlan,String screeningOrgName,List<ScreeningResultStatistic> screeningResultStatisticList) {
        List<Integer> list = Lists.newArrayList();
        if (CollUtil.isNotEmpty(screeningResultStatisticList)){
            Map<Integer, List<ScreeningResultStatistic>> schoolTypeResultMap = screeningResultStatisticList.stream().collect(Collectors.groupingBy(ScreeningResultStatistic::getSchoolType));
            list.addAll(schoolTypeResultMap.keySet());
            list.sort(Comparator.comparing(Integer::intValue).reversed());
        }

        return new ScreeningPlanVO()
                .setId(screeningPlan.getId())
                .setTitle(screeningPlan.getTitle())
                .setStartTime(screeningPlan.getStartTime())
                .setEndTime(screeningPlan.getEndTime())
                .setScreeningType(screeningPlan.getScreeningType())
                .setScreeningBizType(ScreeningBizTypeEnum.getInstanceByOrgType(screeningPlan.getScreeningOrgType()).getType())
                .setScreeningOrgName(screeningOrgName)
                .setOptionTabs(list);
    }

}
