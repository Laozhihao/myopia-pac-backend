package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.SchoolMonitorStatisticMapper;
import com.wupol.myopia.business.management.domain.model.SchoolMonitorStatistic;
import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021-02-24
 */
@Service
public class SchoolMonitorStatisticService extends BaseService<SchoolMonitorStatisticMapper, SchoolMonitorStatistic> {

    @Autowired
    private ScreeningPlanService screeningPlanService;

    public List<SchoolMonitorStatistic> getStatisticDtoByNoticeIdAndOrgId(Integer noticeId, CurrentUser user, Integer districtId) throws IOException {
        if (ObjectsUtil.hasNull(noticeId, user)) {
            return Collections.emptyList();
        }
        if (user.isScreeningUser()) {
            LambdaQueryWrapper<SchoolMonitorStatistic> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SchoolMonitorStatistic::getScreeningNoticeId, noticeId);
            queryWrapper.eq(SchoolMonitorStatistic::getScreeningOrgId, user.getOrgId());
            return baseMapper.selectList(queryWrapper);
        }
        Set<Integer> noticeIds = new HashSet<>();
        noticeIds.add(noticeId);
        List<ScreeningPlan> screeningPlans = screeningPlanService.getScreeningPlanByNoticeIdsAndUser(noticeIds, user);
        List<Integer> screeningOrgIds = screeningPlans.stream().map(ScreeningPlan::getScreeningOrgId).distinct().collect(Collectors.toList());//todo @jacob?
        if (CollectionUtils.isEmpty(screeningOrgIds)) {
            return new ArrayList<>();
        }
        List<SchoolMonitorStatistic> statistics = new ArrayList<>();
        Lists.partition(screeningOrgIds, 100).forEach(screeningOrgIdList -> {
            LambdaQueryWrapper<SchoolMonitorStatistic> query = new LambdaQueryWrapper<>();
            query.eq(SchoolMonitorStatistic::getScreeningNoticeId, noticeId);
            query.eq(SchoolMonitorStatistic::getDistrictId, districtId);
            query.in(SchoolMonitorStatistic::getScreeningOrgId, screeningOrgIdList);
            statistics.addAll(baseMapper.selectList(query));
        });
        return statistics;
    }

    /**
     * 根据唯一索引批量新增或更新
     *
     * @param schoolMonitorStatistics
     */
    public void batchSaveOrUpdate(List<SchoolMonitorStatistic> schoolMonitorStatistics) {
        if (CollectionUtils.isEmpty(schoolMonitorStatistics)) {
            return;
        }
        Lists.partition(schoolMonitorStatistics, 20).forEach(statistics -> baseMapper.batchSaveOrUpdate(statistics));
    }
}
