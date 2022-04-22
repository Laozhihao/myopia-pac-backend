package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.domain.vo.*;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
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

    private final ScreeningNoticeService screeningNoticeService;
    private final DistrictService districtService;
    private final SchoolService schoolService;
    private final ScreeningPlanService screeningPlanService;
    private final ScreeningResultStatisticService screeningResultStatisticService;
    private final ManagementScreeningPlanBizService managementScreeningPlanBizService;

    /**
     * 按学校-获取幼儿园数据
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    public SchoolKindergartenResultVO getSchoolKindergartenResult(Integer districtId, Integer noticeId,Integer planId) {
        SchoolKindergartenResultVO schoolKindergartenResultVO = new SchoolKindergartenResultVO();
        if (ObjectsUtil.allNull(noticeId,planId)){
            return schoolKindergartenResultVO;
        }
        if (Objects.nonNull(noticeId)){
           return getSchoolKindergartenResultByNoticeId(districtId,noticeId);
        }
        if (Objects.nonNull(planId)){
            return getSchoolKindergartenResultByPlanId(districtId,planId);
        }
        return schoolKindergartenResultVO;
    }

    /**
     * 按学校-获取幼儿园数据-筛查通知
     */
    private SchoolKindergartenResultVO getSchoolKindergartenResultByNoticeId(Integer districtId, Integer noticeId){
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if(Objects.isNull(user)){
            return new SchoolKindergartenResultVO();
        }
        // 获取当前层级下，所有参与任务的学校
        ScreeningNotice screeningNotice = screeningNoticeService.getReleasedNoticeById(noticeId);
        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        List<ScreeningResultStatistic> screeningResultStatistics = getStatisticByNoticeIdAndDistrictId(noticeId, user, districtIds,true);
        return getSchoolKindergartenResultVO(screeningResultStatistics,screeningNotice);
    }

    /**
     * 根据通知ID和区域ID获取筛查结果统计
     */
    private List<ScreeningResultStatistic> getStatisticByNoticeIdAndDistrictId(Integer noticeId, CurrentUser user, List<Integer> districtIds,boolean isKindergarten){
        if(ObjectsUtil.allNull(noticeId,user)){
            return Lists.newArrayList();
        }
        if(user.isGovDeptUser()){
            List<ScreeningPlan> screeningPlans = screeningPlanService.getAllPlanByNoticeId(noticeId);
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
        List<ScreeningPlan> screeningPlans = managementScreeningPlanBizService.getScreeningPlanByNoticeIdsAndUser(noticeIds, user);
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
            return new SchoolKindergartenResultVO();
        }
        ScreeningPlan screeningPlan = screeningPlanService.getById(planId);
        ScreeningNotice screeningNotice = screeningNoticeService.getById(screeningPlan.getSrcScreeningNoticeId());
        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        List<ScreeningResultStatistic> screeningResultStatistics = getStatisticByPlanIdsAndDistrictId(Lists.newArrayList(screeningPlan), districtIds,true);
        return getSchoolKindergartenResultVO(screeningResultStatistics,screeningNotice);
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
                    .in(ScreeningResultStatistic::getSchoolType,getSchoolType(isKindergarten));;
            statistics.addAll(screeningResultStatisticService.list(query));
        });
        return statistics;
    }

    /**
     * 获取幼儿园数据
     */
    private SchoolKindergartenResultVO getSchoolKindergartenResultVO(List<ScreeningResultStatistic> screeningResultStatistics,ScreeningNotice screeningNotice){
        SchoolKindergartenResultVO schoolKindergartenResultVO = new SchoolKindergartenResultVO();
        if (CollectionUtils.isEmpty(screeningResultStatistics)) {
            return schoolKindergartenResultVO;
        }
        //学校id
        List<Integer> schoolIds = screeningResultStatistics.stream().map(ScreeningResultStatistic::getSchoolId).collect(Collectors.toList());
        List<Integer> schoolDistrictIdList = schoolService.getByIds(schoolIds).stream().map(School::getDistrictId).collect(Collectors.toList());
        //获取学校的地区
        Map<Integer, String> schoolIdDistrictNameMap = districtService.getByIds(schoolDistrictIdList);
        //获取数据
        schoolKindergartenResultVO.setItemData(screeningResultStatistics,schoolIdDistrictNameMap);
        return schoolKindergartenResultVO;
    }




    /**
     * 按学校-获取小学及以上数据
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    public SchoolPrimarySchoolAndAboveResultVO getSchoolPrimarySchoolAndAboveResult(Integer districtId, Integer noticeId,Integer planId) {
        SchoolPrimarySchoolAndAboveResultVO schoolPrimarySchoolAndAboveResultVO = new SchoolPrimarySchoolAndAboveResultVO();
        if (ObjectsUtil.allNull(noticeId,planId)){
            return schoolPrimarySchoolAndAboveResultVO;
        }
        if (Objects.nonNull(noticeId)){
            return getSchoolPrimarySchoolAndAboveResultByNoticeId(districtId,noticeId);
        }
        if (Objects.nonNull(planId)){
            return getSchoolPrimarySchoolAndAboveResultByPlanId(districtId,planId);
        }
        return schoolPrimarySchoolAndAboveResultVO;
    }

    /**
     * 按学校-获取小学及以上数据-筛查通知
     */
    private SchoolPrimarySchoolAndAboveResultVO getSchoolPrimarySchoolAndAboveResultByNoticeId(Integer districtId, Integer noticeId){
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if(Objects.isNull(user)){
            return new SchoolPrimarySchoolAndAboveResultVO();
        }
        // 获取当前层级下，所有参与任务的学校
        ScreeningNotice screeningNotice = screeningNoticeService.getReleasedNoticeById(noticeId);
        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        List<ScreeningResultStatistic> screeningResultStatistics = getStatisticByNoticeIdAndDistrictId(noticeId, user, districtIds,false);
        return getSchoolPrimarySchoolAndAboveResultVO(screeningResultStatistics,screeningNotice);
    }

    /**
     * 按学校-获取小学及以上数据-筛查计划
     */
    private SchoolPrimarySchoolAndAboveResultVO getSchoolPrimarySchoolAndAboveResultByPlanId(Integer districtId, Integer planId){

        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if(Objects.isNull(user)){
            return new SchoolPrimarySchoolAndAboveResultVO();
        }

        ScreeningPlan screeningPlan = screeningPlanService.getById(planId);
        ScreeningNotice screeningNotice = screeningNoticeService.getById(screeningPlan.getSrcScreeningNoticeId());
        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);

        List<ScreeningResultStatistic> screeningResultStatistics = getStatisticByPlanIdsAndDistrictId(Lists.newArrayList(screeningPlan), districtIds,false);
        return getSchoolPrimarySchoolAndAboveResultVO(screeningResultStatistics,screeningNotice);
    }

    /**
     * 获取小学及以上数据
     */
    private SchoolPrimarySchoolAndAboveResultVO getSchoolPrimarySchoolAndAboveResultVO(List<ScreeningResultStatistic> screeningResultStatistics,ScreeningNotice screeningNotice){
        SchoolPrimarySchoolAndAboveResultVO schoolPrimarySchoolAndAboveResultVO = new SchoolPrimarySchoolAndAboveResultVO();
        if (CollectionUtils.isEmpty(screeningResultStatistics)) {
            return schoolPrimarySchoolAndAboveResultVO;
        }
        //学校id
        List<Integer> schoolIds = screeningResultStatistics.stream().map(ScreeningResultStatistic::getSchoolId).collect(Collectors.toList());
        List<Integer> schoolDistrictIdList = schoolService.getByIds(schoolIds).stream().map(School::getDistrictId).collect(Collectors.toList());
        //获取学校的地区
        Map<Integer, String> schoolIdDistrictNameMap = districtService.getByIds(schoolDistrictIdList);
        //获取数据
        schoolPrimarySchoolAndAboveResultVO.setItemData(screeningResultStatistics,schoolIdDistrictNameMap);
        return schoolPrimarySchoolAndAboveResultVO;
    }

    /**
     * 按学校-获取学校统计详情
     * @param screeningPlanId 筛查计划ID
     * @param schoolId 学校ID
     */
    public SchoolResultDetailVO getSchoolStatisticDetail(Integer screeningPlanId, Integer screeningNoticeId,Integer schoolId) {
        if (Objects.nonNull(schoolId)){
            School school = schoolService.getById(schoolId);
            boolean isKindergarten = Objects.equals(8, school.getType());
            CurrentUser user = CurrentUserUtil.getCurrentUser();
            if(Objects.isNull(user) && ObjectsUtil.allNotNull(screeningNoticeId,screeningPlanId) ){
                if (isKindergarten){
                    return  new KindergartenResultDetailVO();
                }else {
                    return  new PrimarySchoolAndAboveResultDetailVO();
                }
            }

            if (Objects.nonNull(screeningNoticeId)){
                return getSchoolStatisticDetailByNoticeId(screeningNoticeId,school,user);
            }
            if (Objects.nonNull(screeningPlanId)){
                return getSchoolStatisticDetailByPlanId(screeningPlanId,school);
            }
        }
        return null;
    }
    /**
     * 按学校-获取学校统计详情-筛查通知
     */
    private SchoolResultDetailVO getSchoolStatisticDetailByNoticeId(Integer screeningNoticeId,School school,CurrentUser user){
        ScreeningNotice screeningNotice = screeningNoticeService.getById(screeningNoticeId);
        List<ScreeningResultStatistic> screeningResultStatistics = getStatisticByNoticeIdAndSchoolId(screeningNoticeId, user,school.getId());
        return getSchoolResultDetailVO(screeningResultStatistics,Objects.equals(8, school.getType()),screeningNotice,school);

    }

    /**
     * 按学校-获取学校统计详情-筛查计划
     */
    private SchoolResultDetailVO getSchoolStatisticDetailByPlanId(Integer screeningPlanId,School school){
        ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
        ScreeningNotice screeningNotice = screeningNoticeService.getById(screeningPlan.getSrcScreeningNoticeId());
        List<ScreeningResultStatistic> screeningResultStatistics = getStatisticByPlanIdsAndSchoolId(Lists.newArrayList(screeningPlan), school.getId());
        return getSchoolResultDetailVO(screeningResultStatistics,Objects.equals(8, school.getType()),screeningNotice,school);
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
     * 根据筛查通知ID和学校Id获取筛查结果统计
     */
    private List<ScreeningResultStatistic> getStatisticByNoticeIdAndSchoolId(Integer noticeId, CurrentUser user, Integer schoolId) {
        if(ObjectsUtil.allNull(noticeId,user)){
            return Lists.newArrayList();
        }
        if(user.isGovDeptUser()){
            List<ScreeningPlan> screeningPlans = screeningPlanService.getAllPlanByNoticeId(noticeId);
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
        List<ScreeningPlan> screeningPlans = managementScreeningPlanBizService.getScreeningPlanByNoticeIdsAndUser(noticeIds, user);
        return getStatisticByPlanIdsAndSchoolId(screeningPlans, schoolId);
    }

    /**
     * 获取学校筛查结果统计详情
     */
    private SchoolResultDetailVO getSchoolResultDetailVO(List<ScreeningResultStatistic> screeningResultStatistics,
                                                         boolean isKindergarten,ScreeningNotice screeningNotice,School school){

        if(isKindergarten){
            if (CollectionUtil.isNotEmpty(screeningResultStatistics)){
                KindergartenResultDetailVO detailVO=new KindergartenResultDetailVO();
                detailVO.setBaseData(screeningNotice,school);
                detailVO.setItemData(screeningResultStatistics.get(0));
                return detailVO;
            }
        }else {
            if (CollectionUtil.isNotEmpty(screeningResultStatistics)){
                PrimarySchoolAndAboveResultDetailVO detailVO = new PrimarySchoolAndAboveResultDetailVO();
                detailVO.setBaseData(screeningNotice,school);
                detailVO.setItemData(screeningResultStatistics.get(0));
                return detailVO;
            }
        }
        return null;
    }
}
