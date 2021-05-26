package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.stat.domain.model.SchoolVisionStatistic;
import com.wupol.myopia.business.core.stat.service.SchoolVisionStatisticService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2021/4/26 17:37
 */
@Service
public class SchoolVisionStatisticBizService {

    @Autowired
    private SchoolVisionStatisticService schoolVisionStatisticService;

    @Autowired
    private ManagementScreeningPlanBizService managementScreeningPlanBizService;

    /**
     * 根据条件查找所有数据
     *
     * @param noticeId 通知ID
     * @param user     用户
     * @return List<SchoolVisionStatistic>
     */
    public List<SchoolVisionStatistic> getStatisticDtoByNoticeIdAndOrgId(Integer noticeId, CurrentUser user, Integer districtId) {
        if (noticeId == null || user == null) {
            return new ArrayList<>();
        }
        if (user.isScreeningUser()) {
            LambdaQueryWrapper<SchoolVisionStatistic> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SchoolVisionStatistic::getScreeningNoticeId, noticeId);
            queryWrapper.eq(SchoolVisionStatistic::getScreeningOrgId, user.getOrgId());
            return schoolVisionStatisticService.list(queryWrapper);
        }
        Set<Integer> noticeIds = new HashSet<>();
        noticeIds.add(noticeId);
        List<ScreeningPlan> screeningPlans = managementScreeningPlanBizService.getScreeningPlanByNoticeIdsAndUser(noticeIds, user);
        return getStatisticDtoByPlanIdsAndOrgId(screeningPlans, districtId);
    }

    public List<SchoolVisionStatistic> getStatisticDtoByPlanIdsAndOrgId(List<ScreeningPlan> screeningPlans, Integer districtId) {
        List<Integer> screeningOrgIds = screeningPlans.stream().map(ScreeningPlan::getScreeningOrgId).distinct().collect(Collectors.toList());
        List<Integer> planIds = screeningPlans.stream().map(ScreeningPlan::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(screeningOrgIds)) {
            return new ArrayList<>();
        }
        List<SchoolVisionStatistic> statistics = new ArrayList<>();
        Lists.partition(screeningOrgIds, 100).forEach(screeningOrgIdList -> {
            LambdaQueryWrapper<SchoolVisionStatistic> query = new LambdaQueryWrapper<>();
            query.eq(SchoolVisionStatistic::getDistrictId, districtId)
                    .in(SchoolVisionStatistic::getScreeningPlanId, planIds)
                    .in(SchoolVisionStatistic::getScreeningOrgId, screeningOrgIdList);
            statistics.addAll(schoolVisionStatisticService.list(query));
        });
        return statistics;
    }

}
