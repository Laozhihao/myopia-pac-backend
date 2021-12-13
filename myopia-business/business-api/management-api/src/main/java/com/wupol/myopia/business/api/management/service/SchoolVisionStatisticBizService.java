package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.stat.domain.model.SchoolVisionStatistic;
import com.wupol.myopia.business.core.stat.service.SchoolVisionStatisticService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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

    @Autowired
    private ScreeningPlanService screeningPlanService;

    /**
     * 根据条件查找所有数据
     *
     * @param noticeId 通知ID
     * @param user     用户
     * @return List<SchoolVisionStatistic>
     */
    public List<SchoolVisionStatistic> getStatisticDtoByNoticeIdAndOrgId(Integer noticeId, CurrentUser user, List<Integer> districtIds) {
        if (noticeId == null || user == null) {
            return new ArrayList<>();
        }
        // 政府人员走新的逻辑
        if (user.isGovDeptUser()) {
            List<ScreeningPlan> screeningPlans = screeningPlanService.getAllPlanByNoticeId(noticeId);
            return getStatisticDtoByPlanIdsAndOrgId(screeningPlans, districtIds);
        }
        if (user.isScreeningUser() || (user.isHospitalUser() && (Objects.nonNull(user.getScreeningOrgId())))) {
            LambdaQueryWrapper<SchoolVisionStatistic> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SchoolVisionStatistic::getScreeningNoticeId, noticeId);
            queryWrapper.eq(SchoolVisionStatistic::getScreeningOrgId, user.getScreeningOrgId());
            return schoolVisionStatisticService.list(queryWrapper);
        }
        Set<Integer> noticeIds = new HashSet<>();
        noticeIds.add(noticeId);
        List<ScreeningPlan> screeningPlans = managementScreeningPlanBizService.getScreeningPlanByNoticeIdsAndUser(noticeIds, user);
        return getStatisticDtoByPlanIdsAndOrgId(screeningPlans, districtIds);
    }

    /**
     * 获取学校的统计数据
     *
     * @param screeningPlans
     * @param districtIds
     * @return
     */
    public List<SchoolVisionStatistic> getStatisticDtoByPlanIdsAndOrgId(List<ScreeningPlan> screeningPlans, List<Integer> districtIds) {
        List<Integer> screeningOrgIds = screeningPlans.stream().map(ScreeningPlan::getScreeningOrgId).distinct().collect(Collectors.toList());
        List<Integer> planIds = screeningPlans.stream().map(ScreeningPlan::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(screeningOrgIds)) {
            return new ArrayList<>();
        }
        List<SchoolVisionStatistic> statistics = new ArrayList<>();
        Lists.partition(screeningOrgIds, 100).forEach(screeningOrgIdList -> {
            LambdaQueryWrapper<SchoolVisionStatistic> query = new LambdaQueryWrapper<>();
            query.in(CollectionUtils.isNotEmpty(districtIds), SchoolVisionStatistic::getDistrictId, districtIds)
                    .in(SchoolVisionStatistic::getScreeningPlanId, planIds)
                    .in(SchoolVisionStatistic::getScreeningOrgId, screeningOrgIdList);
            statistics.addAll(schoolVisionStatisticService.list(query));
        });
        return statistics;
    }

}
