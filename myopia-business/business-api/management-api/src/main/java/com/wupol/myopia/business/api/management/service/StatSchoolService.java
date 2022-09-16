package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.screening.facade.ManagementScreeningPlanFacade;
import com.wupol.myopia.business.api.management.domain.vo.SchoolKindergartenResultVO;
import com.wupol.myopia.business.api.management.domain.vo.SchoolPrimarySchoolAndAboveResultVO;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatRescreen;
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
 * 按学校统计
 *
 * @author hang.yuan 2022/4/15 16:17
 */
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class StatSchoolService {

    private final DistrictService districtService;
    private final SchoolService schoolService;
    private final ScreeningPlanService screeningPlanService;
    private final ScreeningResultStatisticService screeningResultStatisticService;
    private final ManagementScreeningPlanFacade managementScreeningPlanFacade;
    private final StatRescreenService statRescreenService;

    /**
     * 按学校-获取幼儿园数据
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    public SchoolKindergartenResultVO getSchoolKindergartenResult(Integer districtId, Integer noticeId,Integer planId) {
        if (ObjectsUtil.allNull(noticeId,planId)){
            return null;
        }
        if (Objects.nonNull(noticeId)){
           return getSchoolKindergartenResultByNoticeId(districtId,noticeId);
        }
        if (Objects.nonNull(planId)){
            return getSchoolKindergartenResultByPlanId(districtId,planId);
        }
        return null;
    }

    /**
     * 按学校-获取幼儿园数据-筛查通知
     */
    private SchoolKindergartenResultVO getSchoolKindergartenResultByNoticeId(Integer districtId, Integer noticeId){
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if(Objects.isNull(user)){
            return null;
        }
        // 获取当前层级下，所有参与任务的学校
        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        List<ScreeningResultStatistic> screeningResultStatistics = getStatisticByNoticeIdAndDistrictId(noticeId, user, districtIds,true);
        return getSchoolKindergartenResultVO(screeningResultStatistics);
    }

    /**
     * 根据通知ID和区域ID获取筛查结果统计
     */
    private List<ScreeningResultStatistic> getStatisticByNoticeIdAndDistrictId(Integer noticeId, CurrentUser user, List<Integer> districtIds,boolean isKindergarten){
        if(ObjectsUtil.allNull(noticeId,user)){
            return Lists.newArrayList();
        }
        if(user.isGovDeptUser()){
            List<ScreeningPlan> screeningPlans = screeningPlanService.getAllReleasePlanByNoticeId(noticeId);
            return getStatisticByPlanIdsAndDistrictId(screeningPlans, districtIds,isKindergarten);
        }

        if (user.isScreeningUser() || (user.isHospitalUser() && (Objects.nonNull(user.getScreeningOrgId())))) {
            LambdaQueryWrapper<ScreeningResultStatistic> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ScreeningResultStatistic::getScreeningNoticeId, noticeId)
                    .eq(ScreeningResultStatistic::getScreeningOrgId, user.getScreeningOrgId())
                    .in(ScreeningResultStatistic::getSchoolType,getSchoolType(isKindergarten));
            return screeningResultStatisticService.list(queryWrapper);
        }

        Set<Integer> noticeIds = new HashSet<>();
        noticeIds.add(noticeId);
        List<ScreeningPlan> screeningPlans = managementScreeningPlanFacade.getScreeningPlanByNoticeIdsOrTaskIdsAndUser(noticeIds, null, user);
        return getStatisticByPlanIdsAndDistrictId(screeningPlans, districtIds,isKindergarten);
    }
    private List<Integer> getSchoolType(boolean isKindergarten) {
        return isKindergarten?Lists.newArrayList(8):Lists.newArrayList(0,1,2,3,4,5,6,7);
    }


    /**
     * 按学校-获取幼儿园数据-筛查计划
     */
    private SchoolKindergartenResultVO getSchoolKindergartenResultByPlanId(Integer districtId, Integer planId){
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if(Objects.isNull(user)){
            return null;
        }
        ScreeningPlan screeningPlan = screeningPlanService.getById(planId);
        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        List<ScreeningResultStatistic> screeningResultStatistics = getStatisticByPlanIdsAndDistrictId(Lists.newArrayList(screeningPlan), districtIds,true);
        return getSchoolKindergartenResultVO(screeningResultStatistics);
    }

    /**
     * 根据筛查计划ID和区域ID获取筛查结果统计
     */
    private List<ScreeningResultStatistic> getStatisticByPlanIdsAndDistrictId(List<ScreeningPlan> screeningPlans, List<Integer> districtIds,boolean isKindergarten) {
        List<Integer> screeningOrgIds = screeningPlans.stream().map(ScreeningPlan::getScreeningOrgId).distinct().collect(Collectors.toList());
        List<Integer> planIds = screeningPlans.stream().map(ScreeningPlan::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(screeningOrgIds)) {
            return new ArrayList<>();
        }
        List<ScreeningResultStatistic> statistics = new ArrayList<>();
        Lists.partition(screeningOrgIds, 100).forEach(screeningOrgIdList -> {
            LambdaQueryWrapper<ScreeningResultStatistic> query = new LambdaQueryWrapper<>();
            query.in(CollectionUtils.isNotEmpty(districtIds), ScreeningResultStatistic::getDistrictId, districtIds)
                    .in(ScreeningResultStatistic::getScreeningPlanId, planIds)
                    .in(ScreeningResultStatistic::getScreeningOrgId, screeningOrgIdList)
                    .in(ScreeningResultStatistic::getSchoolType,getSchoolType(isKindergarten));
            statistics.addAll(screeningResultStatisticService.list(query));
        });
        return statistics;
    }

    /**
     * 获取幼儿园数据
     */
    private SchoolKindergartenResultVO getSchoolKindergartenResultVO(List<ScreeningResultStatistic> screeningResultStatistics){

        if (CollectionUtils.isEmpty(screeningResultStatistics)) {
            return null;
        }
        //学校id
        Set<Integer> schoolIds = screeningResultStatistics.stream().map(ScreeningResultStatistic::getSchoolId).collect(Collectors.toSet());
        Set<Integer> planIds = screeningResultStatistics.stream().map(ScreeningResultStatistic::getScreeningPlanId).collect(Collectors.toSet());
        //获取学校的名称
        Map<Integer, String> schoolIdDistrictNameMap = schoolService.getByIds(Lists.newArrayList(schoolIds)).stream().collect(Collectors.toMap(School::getId,School::getName));
        Map<String, Boolean> hasRescreenReportMap = hasRescreenReportMap(Lists.newArrayList(planIds), Lists.newArrayList(schoolIds));
        SchoolKindergartenResultVO schoolKindergartenResultVO = new SchoolKindergartenResultVO();
        //获取数据
        schoolKindergartenResultVO.setItemData(screeningResultStatistics,schoolIdDistrictNameMap,hasRescreenReportMap);
        return schoolKindergartenResultVO;
    }

    public Map<String,Boolean> hasRescreenReportMap(List<Integer> planIds,List<Integer> schoolIds) {
        List<StatRescreen> statRescreenList = statRescreenService.getByPlanIdAndSchoolId(planIds, schoolIds);
        if (CollUtil.isNotEmpty(statRescreenList)){
            return statRescreenList.stream().collect(Collectors.toMap(sr->sr.getPlanId()+ StrUtil.UNDERLINE+sr.getSchoolId(), sr->Boolean.TRUE,(r1, r2)->r2));
        }
        return Maps.newHashMap();
    }




    /**
     * 按学校-获取小学及以上数据
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    public SchoolPrimarySchoolAndAboveResultVO getSchoolPrimarySchoolAndAboveResult(Integer districtId, Integer noticeId,Integer planId) {
        if (ObjectsUtil.allNull(noticeId,planId)){
            return null;
        }
        if (Objects.nonNull(noticeId)){
            return getSchoolPrimarySchoolAndAboveResultByNoticeId(districtId,noticeId);
        }
        if (Objects.nonNull(planId)){
            return getSchoolPrimarySchoolAndAboveResultByPlanId(districtId,planId);
        }
        return null;
    }

    /**
     * 按学校-获取小学及以上数据-筛查通知
     */
    private SchoolPrimarySchoolAndAboveResultVO getSchoolPrimarySchoolAndAboveResultByNoticeId(Integer districtId, Integer noticeId){
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if(Objects.isNull(user)){
            return null;
        }
        // 获取当前层级下，所有参与任务的学校
        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        List<ScreeningResultStatistic> screeningResultStatistics = getStatisticByNoticeIdAndDistrictId(noticeId, user, districtIds,false);
        return getSchoolPrimarySchoolAndAboveResultVO(screeningResultStatistics);
    }

    /**
     * 按学校-获取小学及以上数据-筛查计划
     */
    private SchoolPrimarySchoolAndAboveResultVO getSchoolPrimarySchoolAndAboveResultByPlanId(Integer districtId, Integer planId){

        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if(Objects.isNull(user)){
            return null;
        }

        ScreeningPlan screeningPlan = screeningPlanService.getById(planId);
        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);

        List<ScreeningResultStatistic> screeningResultStatistics = getStatisticByPlanIdsAndDistrictId(Lists.newArrayList(screeningPlan), districtIds,false);
        return getSchoolPrimarySchoolAndAboveResultVO(screeningResultStatistics);
    }

    /**
     * 获取小学及以上数据
     */
    private SchoolPrimarySchoolAndAboveResultVO getSchoolPrimarySchoolAndAboveResultVO(List<ScreeningResultStatistic> screeningResultStatistics){

        if (CollectionUtils.isEmpty(screeningResultStatistics)) {
            return null;
        }
        //学校id
        Set<Integer> schoolIds = screeningResultStatistics.stream().map(ScreeningResultStatistic::getSchoolId).collect(Collectors.toSet());
        Set<Integer> planIds = screeningResultStatistics.stream().map(ScreeningResultStatistic::getScreeningPlanId).collect(Collectors.toSet());
        //获取学校的名称
        Map<Integer, String> schoolIdDistrictNameMap = schoolService.getByIds(Lists.newArrayList(schoolIds)).stream().collect(Collectors.toMap(School::getId,School::getName));
        Map<String, Boolean> hasRescreenReportMap = hasRescreenReportMap(Lists.newArrayList(planIds), Lists.newArrayList(schoolIds));
        SchoolPrimarySchoolAndAboveResultVO schoolPrimarySchoolAndAboveResultVO = new SchoolPrimarySchoolAndAboveResultVO();
        //获取数据
        schoolPrimarySchoolAndAboveResultVO.setItemData(screeningResultStatistics,schoolIdDistrictNameMap,hasRescreenReportMap);
        return schoolPrimarySchoolAndAboveResultVO;
    }

}
