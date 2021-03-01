package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.SchoolMonitorStatisticMapper;
import com.wupol.myopia.business.management.domain.model.SchoolMonitorStatistic;
import com.wupol.myopia.business.management.domain.model.SchoolVisionStatistic;
import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021-02-24
 */
@Service
public class SchoolMonitorStatisticService extends BaseService<SchoolMonitorStatisticMapper, SchoolMonitorStatistic> {

    @Autowired
    private ScreeningPlanService screeningPlanService;

    public List<SchoolMonitorStatistic> getStatisticDtoByNoticeIdAndOrgId(Integer noticeId, CurrentUser user,Integer districtId) throws IOException {
        if (noticeId == null || user == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<SchoolMonitorStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SchoolMonitorStatistic::getScreeningNoticeId, noticeId);
        if (user.isScreeningUser()) {
            queryWrapper.eq(SchoolMonitorStatistic::getScreeningOrgId,user.getOrgId());
        } else if (user.isGovDeptUser()) {
            Set<Integer> noticeIds = new HashSet<>();
            noticeIds.add(noticeId);
            List<ScreeningPlan> screeningPlans = screeningPlanService.getScreeningPlanByNoticeIdsAndUser(noticeIds, user);
            Set<Integer> screeningOrgIds = screeningPlans.stream().map(ScreeningPlan::getScreeningOrgId).collect(Collectors.toSet());//todo
            if (CollectionUtils.isEmpty(screeningOrgIds)) {
                return new ArrayList<>();
            }
            queryWrapper.in(SchoolMonitorStatistic::getScreeningOrgId,screeningOrgIds);
            queryWrapper.eq(SchoolMonitorStatistic::getDistrictId, districtId);
        }
        List<SchoolMonitorStatistic> SchoolMonitorStatistics = baseMapper.selectList(queryWrapper);
        return SchoolMonitorStatistics;
    }
}
