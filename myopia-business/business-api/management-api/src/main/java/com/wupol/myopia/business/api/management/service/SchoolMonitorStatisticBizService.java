package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.stat.domain.model.SchoolMonitorStatistic;
import com.wupol.myopia.business.core.stat.service.SchoolMonitorStatisticService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2021/4/26 17:47
 */
@Service
public class SchoolMonitorStatisticBizService {

    @Autowired
    private SchoolMonitorStatisticService schoolMonitorStatisticService;

    @Autowired
    private ManagementScreeningPlanBizService managementScreeningPlanBizService;

    /**
     * 通过通知id和机构id获取统计数据
     * @param noticeId
     * @param user
     * @param districtIds
     * @return
     */
    public List<SchoolMonitorStatistic> getStatisticDtoByNoticeIdAndOrgId(Integer noticeId, CurrentUser user, List<Integer> districtIds) {
        if (ObjectsUtil.hasNull(noticeId, user)) {
            return Collections.emptyList();
        }
        if (user.isScreeningUser()) {
            LambdaQueryWrapper<SchoolMonitorStatistic> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SchoolMonitorStatistic::getScreeningNoticeId, noticeId);
            queryWrapper.eq(SchoolMonitorStatistic::getScreeningOrgId, user.getOrgId());
            return schoolMonitorStatisticService.list(queryWrapper);
        }
        Set<Integer> noticeIds = new HashSet<>();
        noticeIds.add(noticeId);
        List<ScreeningPlan> screeningPlans = managementScreeningPlanBizService.getScreeningPlanByNoticeIdsAndUser(noticeIds, user);
        return this.getStatisticDtoByPlansAndOrgId(screeningPlans, districtIds);
    }

    /**
     * 通过计划和机构id查找统计数据
     * @param plans
     * @param districtIds
     * @return
     */
    public List<SchoolMonitorStatistic> getStatisticDtoByPlansAndOrgId(List<ScreeningPlan> plans, List<Integer> districtIds) {
        if (CollectionUtils.isEmpty(plans)) {
            return new ArrayList<>();
        }
        List<Integer> screeningOrgIds = plans.stream().map(ScreeningPlan::getScreeningOrgId).distinct().collect(Collectors.toList());
        List<Integer> planIds = plans.stream().map(ScreeningPlan::getId).distinct().collect(Collectors.toList());
        List<SchoolMonitorStatistic> statistics = new ArrayList<>();
        Lists.partition(screeningOrgIds, 100).forEach(screeningOrgIdList -> {
            LambdaQueryWrapper<SchoolMonitorStatistic> query = new LambdaQueryWrapper<>();
            query.in(CollectionUtils.isNotEmpty(districtIds), SchoolMonitorStatistic::getDistrictId, districtIds);
            query.in(SchoolMonitorStatistic::getScreeningPlanId, planIds);
            query.in(SchoolMonitorStatistic::getScreeningOrgId, screeningOrgIdList);
            statistics.addAll(schoolMonitorStatisticService.list(query));
        });
        return statistics;
    }

}
