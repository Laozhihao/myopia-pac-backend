package com.wupol.myopia.business.core.stat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.SchoolVisionStatisticMapper;
import com.wupol.myopia.business.management.domain.model.SchoolVisionStatistic;
import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class SchoolVisionStatisticService extends BaseService<SchoolVisionStatisticMapper, SchoolVisionStatistic> {

    @Autowired
    private ScreeningPlanService screeningPlanService;

    /**
     * 通过planId、学校ID获取列表
     *
     * @param planIds  planIds
     * @param schoolId 学校ID
     * @return List<SchoolVisionStatistic>
     */
    public List<SchoolVisionStatistic> getByPlanIdsAndSchoolId(List<Integer> planIds, Integer schoolId) {
        return baseMapper.getByPlanIdsAndSchoolId(planIds, schoolId);
    }

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
            return baseMapper.selectList(queryWrapper);
        }
        Set<Integer> noticeIds = new HashSet<>();
        noticeIds.add(noticeId);
        List<ScreeningPlan> screeningPlans = screeningPlanService.getScreeningPlanByNoticeIdsAndUser(noticeIds, user);
        List<Integer> screeningOrgIds = screeningPlans.stream().map(ScreeningPlan::getScreeningOrgId).distinct().collect(Collectors.toList());//todo
        if (CollectionUtils.isEmpty(screeningOrgIds)) {
            return new ArrayList<>();
        }
        List<SchoolVisionStatistic> statistics = new ArrayList<>();
        Lists.partition(screeningOrgIds, 100).forEach(screeningOrgIdList -> {
            LambdaQueryWrapper<SchoolVisionStatistic> query = new LambdaQueryWrapper<>();
            query.eq(SchoolVisionStatistic::getScreeningNoticeId, noticeId);
            query.eq(SchoolVisionStatistic::getDistrictId, districtId);
            query.in(SchoolVisionStatistic::getScreeningOrgId, screeningOrgIdList);
            statistics.addAll(baseMapper.selectList(query));
        });
        return statistics;
    }

    /**
     * 根据唯一索引批量新增或更新
     *
     * @param schoolVisionStatistics 学校某次筛查计划统计视力情况
     */
    public void batchSaveOrUpdate(List<SchoolVisionStatistic> schoolVisionStatistics) {
        if (CollectionUtils.isEmpty(schoolVisionStatistics)) {
            return;
        }
        Lists.partition(schoolVisionStatistics, 20).forEach(statistics -> baseMapper.batchSaveOrUpdate(statistics));
    }
}
