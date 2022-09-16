package com.wupol.myopia.business.aggregation.stat.facade;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.screening.facade.ManagementScreeningPlanFacade;
import com.wupol.myopia.business.aggregation.stat.domain.bo.StatisticDetailBO;
import com.wupol.myopia.business.aggregation.stat.domain.vo.SchoolResultDetailVO;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatRescreenService;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 学校统计门面
 *
 * @author hang.yuan 2022/9/16 20:49
 */
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class StatSchoolFacade {

    private final ScreeningNoticeService screeningNoticeService;
    private final SchoolService schoolService;
    private final ScreeningPlanService screeningPlanService;
    private final ScreeningResultStatisticService screeningResultStatisticService;
    private final ManagementScreeningPlanFacade managementScreeningPlanFacade;


    /**
     * 按学校-获取学校统计详情
     * @param statisticDetailBO
     */
    public SchoolResultDetailVO getSchoolStatisticDetail(StatisticDetailBO statisticDetailBO) {
        Integer schoolId = statisticDetailBO.getSchoolId();
        if (Objects.nonNull(schoolId)){
            School school = schoolService.getById(schoolId);
            if (Objects.isNull(school)){
                throw new BusinessException(String.format("学校不存在:%s",schoolId));
            }
            statisticDetailBO.setSchool(school);
            CurrentUser user = CurrentUserUtil.getCurrentUser();
            if(Objects.isNull(user)){
                return null;
            }
            statisticDetailBO.setUser(user);
            if (Objects.nonNull(statisticDetailBO.getScreeningNoticeId())){
                return getSchoolStatisticDetailByNoticeId(statisticDetailBO);
            }
            if (Objects.nonNull(statisticDetailBO.getScreeningPlanId())){
                return getSchoolStatisticDetailByPlanId(statisticDetailBO);
            }
        }
        return null;
    }

    /**
     * 按学校-获取学校统计详情-筛查通知
     */
    private SchoolResultDetailVO getSchoolStatisticDetailByNoticeId(StatisticDetailBO statisticDetailBO){
        ScreeningNotice screeningNotice = screeningNoticeService.getById(statisticDetailBO.getScreeningNoticeId());
        List<ScreeningResultStatistic> screeningResultStatistics = getStatisticByNoticeIdAndSchoolId(statisticDetailBO);
        return getSchoolResultDetailVO(screeningResultStatistics,screeningNotice,statisticDetailBO.getSchool(),null,statisticDetailBO.getType());

    }

    /**
     * 按学校-获取学校统计详情-筛查计划
     */
    private SchoolResultDetailVO getSchoolStatisticDetailByPlanId(StatisticDetailBO statisticDetailBO){
        ScreeningPlan screeningPlan = screeningPlanService.getById(statisticDetailBO.getScreeningPlanId());
        ScreeningNotice screeningNotice = screeningNoticeService.getById(screeningPlan.getSrcScreeningNoticeId());
        List<ScreeningResultStatistic> screeningResultStatistics = getStatisticByPlanIdsAndSchoolId(Lists.newArrayList(screeningPlan), statisticDetailBO.getSchoolId());
        return getSchoolResultDetailVO(screeningResultStatistics,screeningNotice,statisticDetailBO.getSchool(),screeningPlan,statisticDetailBO.getType());
    }


    /**
     * 根据筛查通知ID和学校Id获取筛查结果统计
     */
    private List<ScreeningResultStatistic> getStatisticByNoticeIdAndSchoolId(StatisticDetailBO statisticDetailBO) {
        Integer noticeId = statisticDetailBO.getScreeningNoticeId();
        CurrentUser user = statisticDetailBO.getUser();
        Integer schoolId = statisticDetailBO.getSchoolId();
        if(ObjectsUtil.allNull(noticeId,user)){
            return Lists.newArrayList();
        }
        if(user.isGovDeptUser()){
            List<ScreeningPlan> screeningPlans = screeningPlanService.getAllReleasePlanByNoticeId(noticeId);
            return getStatisticByPlanIdsAndSchoolId(screeningPlans, schoolId);
        }

        if (user.isScreeningUser() || (user.isHospitalUser() && (Objects.nonNull(user.getScreeningOrgId())))) {
            LambdaQueryWrapper<ScreeningResultStatistic> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ScreeningResultStatistic::getScreeningNoticeId, noticeId);
            queryWrapper.eq(ScreeningResultStatistic::getScreeningOrgId, user.getScreeningOrgId());
            return screeningResultStatisticService.list(queryWrapper);
        }

        Set<Integer> noticeIds = new HashSet<>();
        noticeIds.add(noticeId);
        List<ScreeningPlan> screeningPlans = managementScreeningPlanFacade.getScreeningPlanByNoticeIdsOrTaskIdsAndUser(noticeIds, null, user);
        return getStatisticByPlanIdsAndSchoolId(screeningPlans, schoolId);
    }

    /**
     * 根据筛查计划ID和学校Id获取筛查结果统计
     */
    private List<ScreeningResultStatistic> getStatisticByPlanIdsAndSchoolId(List<ScreeningPlan> screeningPlans, Integer schoolId) {
        List<Integer> screeningOrgIds = screeningPlans.stream().map(ScreeningPlan::getScreeningOrgId).distinct().collect(Collectors.toList());
        List<Integer> planIds = screeningPlans.stream().map(ScreeningPlan::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(screeningOrgIds)) {
            return new ArrayList<>();
        }
        List<ScreeningResultStatistic> statistics = new ArrayList<>();
        Lists.partition(screeningOrgIds, 100).forEach(screeningOrgIdList -> {
            LambdaQueryWrapper<ScreeningResultStatistic> query = new LambdaQueryWrapper<>();
            query.eq( ScreeningResultStatistic::getSchoolId, schoolId)
                    .in(ScreeningResultStatistic::getScreeningPlanId, planIds)
                    .in(ScreeningResultStatistic::getScreeningOrgId, screeningOrgIdList);
            statistics.addAll(screeningResultStatisticService.list(query));
        });
        return statistics;
    }

    /**
     * 获取学校筛查结果统计详情
     */
    private SchoolResultDetailVO getSchoolResultDetailVO(List<ScreeningResultStatistic> screeningResultStatistics,
                                                         ScreeningNotice screeningNotice,School school,
                                                         ScreeningPlan screeningPlan ,Integer type){

        SchoolResultDetailVO schoolResultDetailVO = new SchoolResultDetailVO();
        if(Objects.nonNull(screeningNotice)){
            schoolResultDetailVO.setItemData(screeningNotice.getId(),type,screeningNotice.getScreeningType(),school,screeningResultStatistics);
        }else {
            schoolResultDetailVO.setItemData(screeningPlan.getSrcScreeningNoticeId(),type,screeningPlan.getScreeningType(),school,screeningResultStatistics);
        }
        return schoolResultDetailVO;
    }
}
