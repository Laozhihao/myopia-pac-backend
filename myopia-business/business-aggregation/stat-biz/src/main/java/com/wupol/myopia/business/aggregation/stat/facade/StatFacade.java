package com.wupol.myopia.business.aggregation.stat.facade;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CompareUtil;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.stat.domain.bo.StatisticDetailBO;
import com.wupol.myopia.business.aggregation.stat.domain.bo.StatisticResultBO;
import com.wupol.myopia.business.aggregation.stat.domain.builder.ScreeningResultStatisticBuilder;
import com.wupol.myopia.business.aggregation.stat.domain.vo.SchoolResultDetailVO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.facade.ManagementScreeningPlanFacade;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 统计门面
 *
 * @author hang.yuan 2022/9/16 20:49
 */
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
@Slf4j
public class StatFacade {

    private final ScreeningNoticeService screeningNoticeService;
    private final SchoolService schoolService;
    private final ScreeningPlanService screeningPlanService;
    private final ScreeningResultStatisticService screeningResultStatisticService;
    private final ManagementScreeningPlanFacade managementScreeningPlanFacade;
    private final StatConclusionService statConclusionService;
    private final ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    private final DistrictService districtService;
    private final GovDeptService govDeptService;


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

        List<Integer> allGovDeptIds = govDeptService.getGovDetIds(user);
        List<ScreeningPlan> screeningPlans = managementScreeningPlanFacade.getScreeningPlanByNoticeIdsOrTaskIdsAndUser(noticeIds, null, user,allGovDeptIds);
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

    /**
     * 根据筛查计划ID和学校ID获取统计结果
     * @param screeningPlanId 筛查计划ID
     * @param schoolId 学校ID
     */
    public TwoTuple<List<VisionScreeningResultStatistic>,List<CommonDiseaseScreeningResultStatistic>> getRealTimeSchoolStatistics(Integer screeningPlanId, Integer schoolId){
        //根据筛查计划ID 获取筛查数据结论
        List<StatConclusion> statConclusions = statConclusionService.listByScreeningPlanIdAndSchoolId(screeningPlanId,schoolId);
        if(CollUtil.isEmpty(statConclusions)){
            return TwoTuple.of(Lists.newArrayList(),Lists.newArrayList());
        }
        List<VisionScreeningResultStatistic> visionScreeningResultStatisticList =Lists.newArrayList();
        List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList =Lists.newArrayList();
        screeningResultStatistic(statConclusions,Boolean.TRUE,visionScreeningResultStatisticList,commonDiseaseScreeningResultStatisticList);
        return TwoTuple.of(visionScreeningResultStatisticList,commonDiseaseScreeningResultStatisticList);
    }


    /**
     * 视力筛查结果统计
     * @param statConclusionList 筛查结论数据集合
     * @param isSchoolStatistics 是否是学校统计
     * @param visionScreeningResultStatisticList 视力筛查结果统计数据集合
     * @param commonDiseaseScreeningResultStatisticList 常见病筛查结果统计数据集合
     */
    public void screeningResultStatistic(List<StatConclusion> statConclusionList,Boolean isSchoolStatistics,
                                          List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                          List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList){
        if(CollUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> screeningTypeStatConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getScreeningType));
        screeningTypeStatConclusionMap.forEach((screeningType,statConclusions)->{
            if (Objects.equals(Boolean.TRUE,isSchoolStatistics)){
                schoolStatistics(visionScreeningResultStatisticList, commonDiseaseScreeningResultStatisticList, screeningType, statConclusions);
            }else {
                districtStatistic(visionScreeningResultStatisticList,commonDiseaseScreeningResultStatisticList,screeningType,statConclusions);
            }
        });

    }

    /**
     * 按学校统计
     * @param visionScreeningResultStatisticList
     * @param commonDiseaseScreeningResultStatisticList
     * @param screeningType
     * @param statConclusions
     */
    private void schoolStatistics(List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                            List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList,
                            Integer screeningType, List<StatConclusion> statConclusions) {
        if (Objects.equals(ScreeningTypeEnum.VISION.getType(),screeningType)){
            schoolStatistics(statConclusions,visionScreeningResultStatisticList,null);
        }else {
            schoolStatistics(statConclusions,null,commonDiseaseScreeningResultStatisticList);
        }
    }

    /**
     * 按区域统计
     * @param visionScreeningResultStatisticList
     * @param commonDiseaseScreeningResultStatisticList
     * @param screeningType
     * @param statConclusions
     */
    private void districtStatistic(List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                  List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList,
                                  Integer screeningType, List<StatConclusion> statConclusions) {
        if (Objects.equals(ScreeningTypeEnum.VISION.getType(),screeningType)){
            districtStatistic(statConclusions,visionScreeningResultStatisticList,null);
        }else {
            districtStatistic(statConclusions,null,commonDiseaseScreeningResultStatisticList);
        }
    }



    /**
     * 按学校统计逻辑
     * @param statConclusions 筛查结论数据集合
     * @param visionScreeningResultStatisticList 视力筛查结果统计数据集合
     * @param commonDiseaseScreeningResultStatisticList 常见病筛查结果统计数据集合
     */
    private void schoolStatistics(List<StatConclusion> statConclusions,
                            List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                            List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList) {
        if (CollUtil.isEmpty(statConclusions)){
            return;
        }
        //根据筛查数据结论 按计划ID分组
        Map<Integer, List<StatConclusion>> statConclusionMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getPlanId));

        Set<Integer> screeningPlanIds = statConclusionMap.keySet();

        //根据筛查计划ID 获取筛查计划数据
        List<ScreeningPlan> screeningPlans = screeningPlanService.getByIds(screeningPlanIds);
        if(CollUtil.isEmpty(screeningPlans)){
            log.error("未找到筛查计划数据，screeningPlanIds:{}",CollUtil.join(screeningPlanIds,","));
            return;
        }
        Map<Integer, ScreeningPlan> screeningPlanMap = screeningPlans.stream().collect(Collectors.toMap(ScreeningPlan::getId, Function.identity()));


        List<ScreeningPlanSchoolStudent> planSchoolStudents = screeningPlanSchoolStudentService.getByScreeningPlanIds(Lists.newArrayList(screeningPlanIds));
        if (CollUtil.isEmpty(planSchoolStudents)){
            log.error("未找到参与筛查计划的学生，screeningPlanIds:{}",CollUtil.join(screeningPlanIds,","));
            return;
        }
        Map<Integer, List<ScreeningPlanSchoolStudent>> planSchoolStudentMap = planSchoolStudents.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getScreeningPlanId));

        //分别统计每个学校的数据
        for (Integer screeningPlanId : screeningPlanIds) {
            // 排除空数据
            List<StatConclusion> statConclusionList = statConclusionMap.get(screeningPlanId);
            if (CollUtil.isEmpty(statConclusionList)){
                return;
            }
            ScreeningPlan screeningPlan = screeningPlanMap.get(screeningPlanId);
            if(Objects.isNull(screeningPlan)){
                return;
            }
            StatisticResultBO statisticResultBO = new StatisticResultBO()
                    .setScreeningNoticeId(screeningPlan.getSrcScreeningNoticeId())
                    .setScreeningType(screeningPlan.getScreeningType())
                    .setScreeningOrgId(screeningPlan.getScreeningOrgId())
                    .setScreeningTaskId(screeningPlan.getScreeningTaskId())
                    .setScreeningPlanId(screeningPlan.getId());

            //筛查数据结论 按学校ID分组
            Map<Integer, List<StatConclusion>> schoolIdStatConslusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));

            //获取学校信息
            List<School> schoolList = schoolService.getByIds(Lists.newArrayList(schoolIdStatConslusionMap.keySet()));
            if (CollUtil.isEmpty(schoolList)){
                return;
            }
            Map<Integer, School> schoolIdMap = schoolList.stream().collect(Collectors.toMap(School::getId, Function.identity()));

            //筛查计划Id 获取筛查学生
            List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = planSchoolStudentMap.get(screeningPlanId);

            Map<Integer, List<ScreeningPlanSchoolStudent>> planSchoolStudentNumMap = screeningPlanSchoolStudentService.groupingByFunction(screeningPlanSchoolStudents,ScreeningPlanSchoolStudent::getSchoolId);

            //3.2 每个学校分别统计
            schoolIdStatConslusionMap.forEach((schoolId,schoolStatConclusionList)->{
                List<ScreeningPlanSchoolStudent> planSchoolStudentList = planSchoolStudentNumMap.getOrDefault(schoolId, Collections.emptyList());
                School school = schoolIdMap.get(schoolId);
                if (Objects.isNull(school)){
                    return;
                }
                statisticResultBO.setSchoolId(schoolId).setIsTotal(Boolean.FALSE)
                        .setDistrictId(school.getDistrictId())
                        .setPlanSchoolStudentList(planSchoolStudentList);
                ScreeningResultStatisticBuilder.screening(visionScreeningResultStatisticList, commonDiseaseScreeningResultStatisticList, statisticResultBO, schoolStatConclusionList);
            });

        }
    }

    /**
     * 按区域统计逻辑
     * @param statConclusionList 筛查结论数据集合
     * @param visionScreeningResultStatisticList 视力筛查结果统计数据集合
     * @param commonDiseaseScreeningResultStatisticList 常见病筛查结果统计数据集合
     */
    private void districtStatistic(List<StatConclusion> statConclusionList,
                           List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                           List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList){
        if (CollUtil.isEmpty(statConclusionList) ){
            return;
        }
        //根据筛查通知ID分组
        Map<Integer, List<StatConclusion>> statConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSrcScreeningNoticeId));

        statConclusionMap.forEach((screeningNoticeId,statConclusions)->{

            // 筛查通知中的学校所在地区层级的计划学生总数
            Map<Integer, List<ScreeningPlanSchoolStudent>> districtPlanStudentCountMap = screeningPlanSchoolStudentService.getPlanStudentCountBySrcScreeningNoticeId(screeningNoticeId);

            //查出通知对应的地区顶级层级：从任务所在省级开始（因为筛查计划可选全省学校）
            ScreeningNotice screeningNotice = screeningNoticeService.getById(screeningNoticeId);
            if (Objects.isNull(screeningNotice)){
                return;
            }
            Integer provinceDistrictId = districtService.getProvinceId(screeningNotice.getDistrictId());

            //同一个筛查通知下不同地区筛查数据结论 ,根据地区分组
            Map<Integer, List<StatConclusion>> districtStatConclusionMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getDistrictId));

            //根据地区生成筛查统计
            genStatisticsByDistrictId(screeningNotice, provinceDistrictId, districtPlanStudentCountMap,districtStatConclusionMap, visionScreeningResultStatisticList, commonDiseaseScreeningResultStatisticList);

        });
    }

    /**
     * 根据地区生成视力筛查统计
     * @param screeningNotice 筛查通知数据
     * @param districtId 区域ID
     * @param districtPlanStudentCountMap 区域筛查计划学生成数
     * @param districtStatConclusions 区域筛查结论数据集合
     * @param visionScreeningResultStatisticList 视力筛查结果统计数据集合
     * @param commonDiseaseScreeningResultStatisticList 常见病筛查结果统计数据集合
     */
    private void genStatisticsByDistrictId(ScreeningNotice screeningNotice, Integer districtId, Map<Integer, List<ScreeningPlanSchoolStudent>> districtPlanStudentCountMap,
                                           Map<Integer, List<StatConclusion>> districtStatConclusions,
                                           List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                           List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList) {
        if (ObjectsUtil.hasNull(screeningNotice,districtId,districtPlanStudentCountMap,districtStatConclusions)){
            return;
        }

        List<District> childDistricts = Lists.newArrayList();
        List<Integer> childDistrictIds = Lists.newArrayList();
        try {
            // 合计的要包括自己层级的筛查数据
            childDistricts = districtService.getChildDistrictByParentIdPriorityCache(districtId);
            childDistrictIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        } catch (Exception e) {
            log.error("获取区域层级失败", e);
        }

        //2.4 层级循环处理并添加到对应的统计中
        //获取两集合的交集
        List<Integer> haveStatConclusionsChildDistrictIds = CompareUtil.getRetain(childDistrictIds, districtStatConclusions.keySet());
        List<Integer> haveStudentDistrictIds = CompareUtil.getRetain(childDistrictIds, districtPlanStudentCountMap.keySet());

        // 层级合计数据
        List<StatConclusion> totalStatConclusions = haveStatConclusionsChildDistrictIds.stream().map(districtStatConclusions::get).flatMap(Collection::stream).collect(Collectors.toList());
        List<ScreeningPlanSchoolStudent> totalPlanStudentCountList = haveStudentDistrictIds.stream().flatMap(id -> {
            List<ScreeningPlanSchoolStudent> planSchoolStudentList = districtPlanStudentCountMap.get(id);
            if (CollUtil.isNotEmpty(planSchoolStudentList)) {
                return planSchoolStudentList.stream();
            } else {
                return Stream.empty();
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        // 层级自身数据
        List<StatConclusion> selfStatConclusions = districtStatConclusions.getOrDefault(districtId, Collections.emptyList());
        List<ScreeningPlanSchoolStudent> selfPlanStudentCount = districtPlanStudentCountMap.getOrDefault(districtId, Collections.emptyList());

        StatisticResultBO totalStatistic = new StatisticResultBO()
                .setScreeningNoticeId(screeningNotice.getId())
                .setScreeningType(screeningNotice.getScreeningType())
                .setDistrictId(districtId)
                .setPlanSchoolStudentList(totalPlanStudentCountList)
                .setStatConclusions(totalStatConclusions);

        genTotalStatistics(totalStatistic, visionScreeningResultStatisticList,commonDiseaseScreeningResultStatisticList);

        StatisticResultBO selfStatistic = new StatisticResultBO()
                .setScreeningNoticeId(screeningNotice.getId())
                .setScreeningType(screeningNotice.getScreeningType())
                .setDistrictId(districtId)
                .setPlanSchoolStudentList(selfPlanStudentCount)
                .setStatConclusions(selfStatConclusions);
        genSelfStatistics(selfStatistic, visionScreeningResultStatisticList,commonDiseaseScreeningResultStatisticList);

        if (totalStatConclusions.size() != selfStatConclusions.size()) {
            //递归统计下层级数据
            childDistricts.forEach(childDistrict -> genStatisticsByDistrictId(screeningNotice, childDistrict.getId(), districtPlanStudentCountMap, districtStatConclusions,visionScreeningResultStatisticList,commonDiseaseScreeningResultStatisticList ));
        }
    }

    /**
     * 合计统计
     * @param totalStatistic 结果统计流转实体
     * @param visionScreeningResultStatisticList 视力筛查结果统计数据集合
     * @param commonDiseaseScreeningResultStatisticList 常见病筛查结果统计数据集合
     */
    private void genTotalStatistics(StatisticResultBO totalStatistic,
                                    List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                    List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList) {

        if (com.wupol.framework.core.util.CollectionUtils.isEmpty(totalStatistic.getStatConclusions()) ) {
            // 计划筛查学生不为0时，即使还没有筛查数据，也要新增统计
            return;
        }
        // 层级总的筛查数据不一定属于同一个任务，所以取默认0
        totalStatistic.setScreeningTaskId(CommonConst.DEFAULT_ID)
                .setScreeningPlanId(CommonConst.DEFAULT_ID)
                .setIsTotal(Boolean.TRUE).setSchoolId(-1).setScreeningOrgId(-1);

        if (Objects.nonNull(visionScreeningResultStatisticList)){
            List<VisionScreeningResultStatistic> list= Lists.newArrayList();
            buildScreening(totalStatistic,list,null);
            visionScreeningResultStatisticList.addAll(list);
        }

        if (Objects.nonNull(commonDiseaseScreeningResultStatisticList)){
            List<CommonDiseaseScreeningResultStatistic> list= Lists.newArrayList();
            buildScreening(totalStatistic,null,list);
            commonDiseaseScreeningResultStatisticList.addAll(list);
        }
    }

    /**
     * 单条统计
     * @param selfStatistic 结果统计流转实体
     * @param visionScreeningResultStatisticList 视力筛查结果统计数据集合
     * @param commonDiseaseScreeningResultStatisticList 常见病筛查结果统计数据集合
     */
    private void genSelfStatistics(StatisticResultBO selfStatistic,
                                   List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                   List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList) {
        List<StatConclusion> statConclusions = selfStatistic.getStatConclusions();
        if (com.wupol.framework.core.util.CollectionUtils.isEmpty(statConclusions)) {
            return;
        }

        // set默认值
        selfStatistic.setScreeningTaskId(CommonConst.DEFAULT_ID)
                .setScreeningPlanId(CommonConst.DEFAULT_ID)
                .setIsTotal(Boolean.FALSE).setSchoolId(-1).setScreeningOrgId(-1);

        if (Objects.nonNull(visionScreeningResultStatisticList)){
            List<VisionScreeningResultStatistic> list= Lists.newArrayList();
            buildScreening(selfStatistic,list,null);
            visionScreeningResultStatisticList.addAll(list);
        }

        if (Objects.nonNull(commonDiseaseScreeningResultStatisticList)){
            List<CommonDiseaseScreeningResultStatistic> list= Lists.newArrayList();
            buildScreening(selfStatistic,null,list);
            commonDiseaseScreeningResultStatisticList.addAll(list);
        }
    }

    /**
     * 按区域 - 视力筛查数据统计
     * @param statistic 结果统计流转实体
     * @param visionScreeningResultStatisticList 视力筛查结果统计数据集合
     * @param commonDiseaseScreeningResultStatisticList 常见病筛查结果统计数据集合
     */
    private void buildScreening(StatisticResultBO statistic,
                                List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                                List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList) {

        List<StatConclusion> statConclusions = statistic.getStatConclusions();
        if (CollUtil.isEmpty(statConclusions)){
            return;
        }
        ScreeningResultStatisticBuilder.screening(visionScreeningResultStatisticList,commonDiseaseScreeningResultStatisticList,statistic,statConclusions);
    }

}
