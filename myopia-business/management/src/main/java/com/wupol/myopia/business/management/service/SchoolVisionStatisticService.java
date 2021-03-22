package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
     * @param user 用户
     * @return List<SchoolVisionStatistic>
     */
    public List<SchoolVisionStatistic> getStatisticDtoByNoticeIdAndOrgId(Integer noticeId, CurrentUser user, Integer districtId) {
        if (noticeId == null || user == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<SchoolVisionStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SchoolVisionStatistic::getScreeningNoticeId, noticeId);
        if (user.isScreeningUser()) {
            queryWrapper.eq(SchoolVisionStatistic::getScreeningOrgId, user.getOrgId());
        } else {
            Set<Integer> noticeIds = new HashSet<>();
            noticeIds.add(noticeId);
            List<ScreeningPlan> screeningPlans = screeningPlanService.getScreeningPlanByNoticeIdsAndUser(noticeIds, user);
            Set<Integer> screeningOrgIds = screeningPlans.stream().map(ScreeningPlan::getScreeningOrgId).collect(Collectors.toSet());//todo
            if (CollectionUtils.isEmpty(screeningOrgIds)) {
                return new ArrayList<>();
            }
            queryWrapper.in(SchoolVisionStatistic::getScreeningOrgId,screeningOrgIds);
            queryWrapper.eq(SchoolVisionStatistic::getDistrictId, districtId);
        }
        return baseMapper.selectList(queryWrapper);
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
        baseMapper.batchSaveOrUpdate(schoolVisionStatistics);
    }
}
