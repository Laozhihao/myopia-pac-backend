package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.management.domain.dto.QuestionAreaDTO;
import com.wupol.myopia.business.api.management.domain.dto.QuestionSearchDTO;
import com.wupol.myopia.business.api.management.domain.vo.*;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 问卷管理
 *
 * @author xz
 */
@Service
@Log4j2
public class QuestionnaireManagementService {
    @Autowired
    private ScreeningTaskService screeningTaskService;

    @Autowired
    private DistrictBizService districtBizService;

    @Autowired
    private ManagementScreeningPlanBizService managementScreeningPlanBizService;

    @Autowired
    private SchoolBizService schoolBizService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private ScreeningPlanService screeningPlanService;

    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    @Autowired
    private UserQuestionRecordService userQuestionRecordService;

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    @Autowired
    private SchoolService schoolService;


    /**
     * 根据机构id获得所有任务
     *
     * @param user user
     * @return
     */
    public List<QuestionTaskVO> getQuestionTaskByUnitId(CurrentUser user) {
        List<ScreeningTask> screeningTasks = screeningTaskService.list(new LambdaQueryWrapper<ScreeningTask>().eq(!user.isPlatformAdminUser(), ScreeningTask::getGovDeptId, user.getOrgId()).eq(ScreeningTask::getScreeningType, 1).eq(ScreeningTask::getReleaseStatus, 1));
        if (CollectionUtils.isEmpty(screeningTasks)) {
            return Lists.newArrayList();
        }

        Map<Integer, Set<ScreeningTask>> yearTaskMap = getYears(screeningTasks);
        return yearTaskMap.entrySet().stream().map(item -> {
            QuestionTaskVO questionTaskVO = new QuestionTaskVO();
            questionTaskVO.setAnnual(item.getKey() + "");
            questionTaskVO.setTasks(item.getValue().stream().map(it2 -> {
                QuestionTaskVO.Item taskItem = new QuestionTaskVO.Item();
                taskItem.setTaskId(it2.getId());
                taskItem.setTaskTitle(it2.getTitle());
                taskItem.setScreeningEndTime(it2.getEndTime());
                taskItem.setScreeningStartTime(it2.getStartTime());
                taskItem.setCreateTime(it2.getCreateTime());
                return taskItem;
            }).collect(Collectors.toList()).stream().sorted(Comparator.comparing(QuestionTaskVO.Item::getCreateTime).reversed()).collect(Collectors.toList()));
            return questionTaskVO;
        }).collect(Collectors.toList()).stream().sorted(Comparator.comparing(QuestionTaskVO::getAnnual).reversed()).map(item -> {
            item.setAnnual(item.getAnnual() + "年度");
            return item;
        }).collect(Collectors.toList());
    }

    public QuestionAreaDTO getQuestionTaskAreas(Integer taskId, CurrentUser user) {
        try {
            QuestionAreaDTO questionAreaDTO = new QuestionAreaDTO();
            ScreeningTask task = screeningTaskService.getById(taskId);
            if (Objects.isNull(task)) {
                return new QuestionAreaDTO();
            }
            //查看该通知所有筛查学校的层级的 地区树
            List<ScreeningPlan> screeningPlans = managementScreeningPlanBizService.getScreeningPlanByUser(user).stream().filter(item -> item.getScreeningTaskId().equals(taskId)).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(screeningPlans)) {
                List<UserQuestionRecord> quests = userQuestionRecordService.list(new LambdaQueryWrapper<UserQuestionRecord>().in(UserQuestionRecord::getPlanId, screeningPlans.stream().map(ScreeningPlan::getId).collect(Collectors.toList())));
                Set<Integer> districts = schoolBizService.getAllSchoolDistrictIdsByScreeningPlanIds(quests.stream().map(UserQuestionRecord::getPlanId).collect(Collectors.toList()));
                if (!CollectionUtils.isEmpty(districts)) {
                    questionAreaDTO.setDistricts(districtBizService.getValidDistrictTree(user, districts));
                } else {
                    questionAreaDTO.setDistricts(Lists.newArrayList());
                }
            }
            if (!user.isPlatformAdminUser()) {
                District parentDistrict = districtBizService.getNotPlatformAdminUserDistrict(user);
                if (JSON.toJSONString(questionAreaDTO.getDistricts()).contains("\"id\":" + parentDistrict.getId())) {
                    questionAreaDTO.setDefaultAreaId(parentDistrict.getId());
                    questionAreaDTO.setDefaultAreaName(parentDistrict.getName());
                }
                if (!CollectionUtils.isEmpty(questionAreaDTO.getDistricts())) {
                    questionAreaDTO.setDistricts(questionAreaDTO.getDistricts().get(0).getChild());
                }
            }
            return questionAreaDTO;
        } catch (Exception e) {
            log.error("获得任务区域失败", e);
            throw new BusinessException("获得任务区域失败！");
        }
    }


    /**
     * 获取年度
     *
     * @return
     */
    private Map<Integer, Set<ScreeningTask>> getYears(List<ScreeningTask> screeningTasks) {
        Map<Integer, Set<ScreeningTask>> yearTask = Maps.newConcurrentMap();
        screeningTasks.forEach(screeningTask -> {
            Integer startYear = DateUtil.getYear(screeningTask.getStartTime());
            Integer endYear = DateUtil.getYear(screeningTask.getEndTime());
            Set<ScreeningTask> startYearTask = Objects.nonNull(yearTask.get(startYear)) ? yearTask.get(startYear) : Sets.newHashSet();
            startYearTask.add(screeningTask);
            yearTask.put(startYear, startYearTask);
            Set<ScreeningTask> endYearTask = Objects.nonNull(yearTask.get(endYear)) ? yearTask.get(endYear) : Sets.newHashSet();
            endYearTask.add(screeningTask);
            yearTask.put(endYear, endYearTask);
        });
        yearTask.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(e -> yearTask.put(e.getKey(), e.getValue()));
        return yearTask;
    }

    /**
     * 学校填写情况
     *
     * @param taskId
     * @param areaId
     * @return
     */
    public QuestionSchoolVO getQuestionSchool(Integer taskId, Integer areaId) throws IOException {
        if (Objects.isNull(areaId) || Objects.isNull(taskId)) {
            return new QuestionSchoolVO();
        }
        ScreeningTask task = screeningTaskService.getById(taskId);
        if (Objects.isNull(task)) {
            return new QuestionSchoolVO();
        }
        List<ScreeningPlan> plans = screeningPlanService.list(new LambdaQueryWrapper<ScreeningPlan>().eq(ScreeningPlan::getScreeningTaskId, taskId));

        Map<Integer, ScreeningPlan> planMap = plans.stream().collect(Collectors.toMap(ScreeningPlan::getId, screeningPlan -> screeningPlan));
        List<ScreeningPlanSchool> searchPage = screeningPlanSchoolService.list(new LambdaQueryWrapper<ScreeningPlanSchool>()
                .in(!CollectionUtils.isEmpty(plans), ScreeningPlanSchool::getScreeningPlanId, plans.stream().map(ScreeningPlan::getId).collect(Collectors.toList()))
                .orderByDesc(ScreeningPlanSchool::getCreateTime));

        Map<Integer, ScreeningPlanSchool> schoolPlanMap = searchPage.stream().collect(Collectors.toMap(ScreeningPlanSchool::getSchoolId, screeningPlanSchool -> screeningPlanSchool));

        Set<Integer> schoolIds = getSchoolIds(taskId, areaId);
        QuestionSchoolVO questionSchoolVO = new QuestionSchoolVO();
        questionSchoolVO.setSchoolAmount(schoolIds.size());
        questionSchoolVO.setSchoolAccomplish(
                schoolIds.stream().map(item ->
                        getStudentQuestionEndByType(item, Lists.newArrayList(QuestionnaireTypeEnum.PRIMARY_SECONDARY_SCHOOLS.getType()), taskId))
                        .collect(Collectors.toList()).stream().mapToInt(item -> item).sum());
        questionSchoolVO.setStudentEnvironmentAmount(schoolIds.size());
        questionSchoolVO.setStudentSpecialAmount(schoolIds.size());
        questionSchoolVO.setStudentEnvironmentAccomplish(getSchoolQuestionEndByType(schoolIds, taskId, schoolPlanMap, planMap));
        questionSchoolVO.setStudentSpecialAccomplish(getSchoolQuestionEndByType(schoolIds, taskId, schoolPlanMap, planMap));
        return questionSchoolVO;
    }

    /**
     * 待办填写情况
     *
     * @param taskId
     * @param areaId
     * @return
     */
    public List<QuestionBacklogVO> getQuestionBacklog(Integer taskId, Integer areaId) throws IOException {
        List<QuestionnaireTypeEnum> types = Lists.newArrayList(QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT, QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL);
        if (Objects.isNull(areaId) || Objects.isNull(taskId)) {
            return types.stream().map(item -> {
                QuestionBacklogVO vo = new QuestionBacklogVO();
                vo.setQuestionnaireTitle(item.getDesc());
                return vo;
            }).collect(Collectors.toList());
        }
        Set<Integer> schoolIds = getSchoolIds(taskId, areaId);
        return types.stream().map(item -> {
            QuestionBacklogVO vo = new QuestionBacklogVO();
            vo.setAmount(schoolIds.size());
            vo.setQuestionnaireTitle(item.getDesc());
            vo.setAccomplish(getStudentQuestionEndBySchool(schoolIds, item.getType(), taskId));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获得任务下学校Id
     *
     * @param taskId
     * @param areaId
     * @return
     * @throws IOException
     */
    private Set<Integer> getSchoolIds(Integer taskId, Integer areaId) throws IOException {
        List<ScreeningPlan> plans = screeningPlanService.list(new LambdaQueryWrapper<ScreeningPlan>().eq(ScreeningPlan::getScreeningTaskId, taskId));
        Set<Integer> districtIds = getAreaIdsBySchoolsAndTaskId(taskId, areaId, plans);
        Set<Integer> schoolIds = screeningPlanSchoolService.list(new LambdaQueryWrapper<ScreeningPlanSchool>().in(ScreeningPlanSchool::getScreeningPlanId, plans.stream().map(ScreeningPlan::getId).collect(Collectors.toList())))
                .stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toSet());
        return schoolService.list(new LambdaQueryWrapper<School>()
                .in(School::getId, schoolIds)
                .in(School::getDistrictId, districtIds)
        ).stream().map(School::getId).collect(Collectors.toSet());
    }

    public IPage<QuestionSchoolRecordVO> getQuestionSchoolList(QuestionSearchDTO questionSearchDTO) throws IOException {
        if (Objects.isNull(questionSearchDTO.getAreaId()) || Objects.isNull(questionSearchDTO.getTaskId())) {
            return new Page<>();
        }
        //查看该通知所有筛查学校的层级的 地区树
        List<ScreeningPlan> plans = screeningPlanService.list(new LambdaQueryWrapper<ScreeningPlan>().eq(ScreeningPlan::getScreeningTaskId, questionSearchDTO.getTaskId()));
        Map<Integer, ScreeningPlan> planMap = plans.stream().collect(Collectors.toMap(ScreeningPlan::getId, screeningPlan -> screeningPlan));
        if (CollectionUtils.isEmpty(plans)) {
            return new Page<>();
        }

        Set<Integer> districtIds = getAreaIdsBySchoolsAndTaskId(questionSearchDTO.getTaskId(), questionSearchDTO.getAreaId(), plans);
        if (CollectionUtils.isEmpty(districtIds)) {
            return new Page<>();
        }

        List<ScreeningPlanSchool> searchPage = screeningPlanSchoolService.list(new LambdaQueryWrapper<ScreeningPlanSchool>()
                .in(!CollectionUtils.isEmpty(plans), ScreeningPlanSchool::getScreeningPlanId, plans.stream().map(ScreeningPlan::getId).collect(Collectors.toList()))
                .like(Objects.nonNull(questionSearchDTO.getSchoolName()), ScreeningPlanSchool::getSchoolName, questionSearchDTO.getSchoolName())
                .orderByDesc(ScreeningPlanSchool::getCreateTime));
        if (CollectionUtils.isEmpty(searchPage)) {
            return new Page<>();
        }
        List<Integer> orgIds = searchPage.stream().map(ScreeningPlanSchool::getScreeningOrgId).collect(Collectors.toList());
        Map<Integer, ScreeningOrganization> orgIdMap = screeningOrganizationService.getByIds(orgIds).stream().collect(Collectors.toMap(ScreeningOrganization::getId, screeningOrganization -> screeningOrganization));
        Map<Integer, ScreeningPlanSchool> schoolIdsPlanMap = searchPage.stream().collect(Collectors.toMap(ScreeningPlanSchool::getSchoolId, screeningPlanSchool -> screeningPlanSchool));

        List<Integer> schoolIds = searchPage.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toList());

        Page<School> queryPage = new Page<>(questionSearchDTO.getCurrent(), questionSearchDTO.getSize());
        Page<School> resultPage = schoolService.page(queryPage, new LambdaQueryWrapper<School>()
                .in(School::getId, schoolIds)
                .in(School::getDistrictId, districtIds)
        );

        Map<Integer, List<UserQuestionRecord>> userRecordToSchoolMap = getRecordSchoolIdMap(Sets.newHashSet(schoolIds), questionSearchDTO.getTaskId(), Lists.newArrayList(QuestionnaireTypeEnum.PRIMARY_SECONDARY_SCHOOLS.getType()));
        Map<Integer, List<UserQuestionRecord>> userRecordToStudentSpecialMap = getRecordSchoolIdMap(Sets.newHashSet(schoolIds), questionSearchDTO.getTaskId(), Lists.newArrayList(QuestionnaireTypeEnum.VISION_SPINE.getType()));
        Map<Integer, List<UserQuestionRecord>> userRecordToStudentEnvironmentMap = getRecordSchoolIdMap(Sets.newHashSet(schoolIds), questionSearchDTO.getTaskId(), Lists.newArrayList(QuestionnaireTypeEnum.PRIMARY_SCHOOL.getType(), QuestionnaireTypeEnum.MIDDLE_SCHOOL.getType(), QuestionnaireTypeEnum.UNIVERSITY_SCHOOL.getType()));

        // 学生总数
        Map<Integer, List<ScreeningPlanSchoolStudent>> studentCountIdMap = screeningPlanSchoolStudentService.list(new LambdaQueryWrapper<ScreeningPlanSchoolStudent>()
                .in(ScreeningPlanSchoolStudent::getSchoolId, schoolIds)
                .eq(ScreeningPlanSchoolStudent::getScreeningTaskId, questionSearchDTO.getTaskId())
        ).stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolId));


        List<QuestionSchoolRecordVO> records = resultPage.getRecords().stream().map(item -> {
            QuestionSchoolRecordVO vo = new QuestionSchoolRecordVO();
            vo.setSchoolName(item.getName());
            vo.setSchoolId(item.getSchoolNo());
            vo.setOrgId(schoolIdsPlanMap.get(item.getId()).getScreeningOrgId());
            vo.setOrgName(orgIdMap.get(vo.getOrgId()).getName());
            vo.setAreaId(item.getId());
            vo.setAreaName(districtService.getDistrictName(item.getDistrictDetail()));
            vo.setSchoolSurveyStatus(CollectionUtils.isEmpty(userRecordToSchoolMap.get(item.getId())) ? 0 : userRecordToSchoolMap.get(item.getId()).get(0).getStatus());
            if (Objects.isNull(studentCountIdMap.get(item.getId()))) {
                vo.setStudentSpecialSurveyStatus(0);
                vo.setStudentEnvironmentSurveyStatus(0);
                return vo;
            }
            ScreeningPlan plan = planMap.get(schoolIdsPlanMap.get(item.getId()).getScreeningPlanId());
            vo.setStudentSpecialSurveyStatus(getCountBySchool(plan, item.getId(), userRecordToStudentSpecialMap));
            vo.setStudentEnvironmentSurveyStatus(getCountBySchool(plan, item.getId(), userRecordToStudentEnvironmentMap));
            return vo;
        }).collect(Collectors.toList());
        Page<QuestionSchoolRecordVO> returnPage = new Page<>();
        BeanUtils.copyProperties(resultPage, returnPage);
        returnPage.setRecords(records);
        return returnPage;
    }

    /**
     * 获得问卷完成学校的个数
     *
     * @return
     * @throws IOException
     */
    private Integer getCountBySchool(ScreeningPlan plan, Integer schoolId, Map<Integer, List<UserQuestionRecord>> userRecordToStudentEnvironmentMap) {
        if (plan.getEndTime().getTime() <= System.currentTimeMillis()) {
            return 2;
        } else if (CollectionUtils.isEmpty(userRecordToStudentEnvironmentMap.get(schoolId))) {
            return 0;

        } else if (!userRecordToStudentEnvironmentMap.get(schoolId).isEmpty()) {
            return 1;
        }
        return 1;
    }


    public IPage<QuestionBacklogRecordVO> getQuestionBacklogList(QuestionSearchDTO questionSearchDTO) throws IOException {
        if (Objects.isNull(questionSearchDTO.getAreaId()) || Objects.isNull(questionSearchDTO.getTaskId())) {
            return new Page<>();
        }
        List<ScreeningPlan> plans = screeningPlanService.list(new LambdaQueryWrapper<ScreeningPlan>().eq(ScreeningPlan::getScreeningTaskId, questionSearchDTO.getTaskId()));

        if (CollectionUtils.isEmpty(plans)) {
            return new Page<>();
        }

        Set<Integer> districtIds = getAreaIdsBySchoolsAndTaskId(questionSearchDTO.getTaskId(), questionSearchDTO.getAreaId(), plans);
        if (CollectionUtils.isEmpty(districtIds)) {
            return new Page<>();
        }
        List<ScreeningPlanSchool> searchPage = screeningPlanSchoolService.list(new LambdaQueryWrapper<ScreeningPlanSchool>()
                .in(!CollectionUtils.isEmpty(plans), ScreeningPlanSchool::getScreeningPlanId, plans.stream().map(ScreeningPlan::getId).collect(Collectors.toList()))
                .like(Objects.nonNull(questionSearchDTO.getSchoolName()), ScreeningPlanSchool::getSchoolName, questionSearchDTO.getSchoolName())
                .orderByDesc(ScreeningPlanSchool::getCreateTime));
        if (CollectionUtils.isEmpty(searchPage)) {
            return new Page<>();
        }

        List<Integer> orgIds = searchPage.stream().map(ScreeningPlanSchool::getScreeningOrgId).collect(Collectors.toList());
        Map<Integer, ScreeningOrganization> orgIdMap = screeningOrganizationService.getByIds(orgIds).stream().collect(Collectors.toMap(ScreeningOrganization::getId, screeningOrganization -> screeningOrganization));
        List<Integer> schoolIds = searchPage.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toList());
        Map<Integer, List<UserQuestionRecord>> userRecordToSchoolMap = getRecordSchoolIdMap(Sets.newHashSet(schoolIds), questionSearchDTO.getTaskId(), Lists.newArrayList(QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT.getType()));

        Page<School> queryPage = new Page<>(questionSearchDTO.getCurrent(), questionSearchDTO.getSize());
        Page<School> resultPage = schoolService.page(queryPage, new LambdaQueryWrapper<School>()
                .in(School::getId, schoolIds)
                .in(School::getDistrictId, districtIds)
        );
        Map<Integer, ScreeningPlanSchool> schoolIdsPlanMap = searchPage.stream().collect(Collectors.toMap(ScreeningPlanSchool::getSchoolId, screeningPlanSchool -> screeningPlanSchool));

        List<QuestionBacklogRecordVO> records = resultPage.getRecords().stream().map(item -> {
            QuestionBacklogRecordVO vo = new QuestionBacklogRecordVO();
            vo.setSchoolName(item.getName());
            vo.setSchoolId(item.getSchoolNo());
            vo.setOrgId(schoolIdsPlanMap.get(item.getId()).getScreeningOrgId());
            vo.setOrgName(orgIdMap.get(vo.getOrgId()).getName());
            vo.setAreaId(item.getId());
            vo.setAreaName(districtService.getDistrictName(item.getDistrictDetail()));
            vo.setEnvironmentalStatus(CollectionUtils.isEmpty(userRecordToSchoolMap.get(item.getId())) ? 0 : userRecordToSchoolMap.get(item.getId()).get(0).getStatus());
            return vo;
        }).collect(Collectors.toList());
        Page<QuestionBacklogRecordVO> returnPage = new Page<>();
        BeanUtils.copyProperties(resultPage, returnPage);
        returnPage.setRecords(records);
        return returnPage;
    }

    private Integer getStudentQuestionEndByType(Integer schoolId, List<Integer> types, Integer taskId) {
        return userQuestionRecordService.count(new LambdaQueryWrapper<UserQuestionRecord>()
                .eq(UserQuestionRecord::getSchoolId, schoolId)
                .in(!CollectionUtils.isEmpty(types), UserQuestionRecord::getQuestionnaireType, types)
                .eq(UserQuestionRecord::getStatus, 2)
                .eq(UserQuestionRecord::getTaskId, taskId)
        );
    }

    private Integer getStudentQuestionEndBySchool(Set<Integer> schoolIds, Integer type, Integer taskId) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return 0;
        }
        return userQuestionRecordService.count(new LambdaQueryWrapper<UserQuestionRecord>()
                .eq(UserQuestionRecord::getQuestionnaireType, type)
                .in(!CollectionUtils.isEmpty(schoolIds), UserQuestionRecord::getSchoolId, schoolIds)
                .eq(UserQuestionRecord::getStatus, 2)
                .eq(UserQuestionRecord::getTaskId, taskId)
        );
    }

    private Integer getSchoolQuestionEndByType(Set<Integer> schoolIds, Integer taskId, Map<Integer, ScreeningPlanSchool> schoolPlanMap, Map<Integer, ScreeningPlan> planMap) {
        // 学生总数
        Map<Integer, List<ScreeningPlanSchoolStudent>> studentCountMaps = screeningPlanSchoolStudentService.list(new LambdaQueryWrapper<ScreeningPlanSchoolStudent>()
                .in(ScreeningPlanSchoolStudent::getSchoolId, schoolIds)
                .eq(ScreeningPlanSchoolStudent::getScreeningTaskId, taskId)
        ).stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolId));
        return schoolIds.stream().map(item -> {
            ScreeningPlan plan = planMap.get(schoolPlanMap.get(item).getScreeningPlanId());
            if (Objects.isNull(studentCountMaps.get(item))) {
                return 0;
            }
            if (plan.getEndTime().getTime() <= System.currentTimeMillis()) {
                return 1;
            }
            return 0;
        }).collect(Collectors.toList()).stream().mapToInt(item -> item).sum();
    }

    private Map<Integer, List<UserQuestionRecord>> getRecordSchoolIdMap(Set<Integer> schoolIds, Integer taskId, List<Integer> types) {
        return userQuestionRecordService.list(new LambdaQueryWrapper<UserQuestionRecord>()
                .in(UserQuestionRecord::getQuestionnaireType, types)
                .in(!CollectionUtils.isEmpty(schoolIds), UserQuestionRecord::getSchoolId, schoolIds)
                .eq(UserQuestionRecord::getTaskId, taskId)
        ).stream().collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));
    }

    public static List<Integer> getSubUtil(String soap, String regx) {
        List<Integer> list = new ArrayList<>();
        Pattern pattern = Pattern.compile(regx);
        Matcher m = pattern.matcher(soap);
        while (m.find()) {
            list.add(Integer.parseInt(m.group(1)));
        }
        return list;
    }

    /**
     * 获得任务下 且在当前区域下学校的区域
     *
     * @param taskId
     * @param areaId
     * @return
     * @throws IOException
     */
    private Set<Integer> getAreaIdsBySchoolsAndTaskId(Integer taskId, Integer areaId, List<ScreeningPlan> screeningPlans) throws IOException {
        ScreeningTask task = screeningTaskService.getById(taskId);
        if (Objects.isNull(task)) {
            return Sets.newHashSet();
        }
        //查看该通知所有筛查学校的层级的 地区树
        List<District> baseDistricts = new ArrayList<>();
        if (!CollectionUtils.isEmpty(screeningPlans)) {
            Set<Integer> districts = schoolBizService.getAllSchoolDistrictIdsByScreeningPlanIds(screeningPlans.stream().map(ScreeningPlan::getId).collect(Collectors.toList()));
            districts.add(areaId);
            if (!CollectionUtils.isEmpty(districts)) {
                List<District> childDistrictTree = districtService.getSpecificDistrictTreePriorityCache(districtService.getById(areaId).getCode());
                baseDistricts = districtService.filterDistrictTree(childDistrictTree, districts);
            }
        }
        return Sets.newHashSet(getSubUtil(JSON.toJSONString(baseDistricts), "\"id\":(.*?),"));
    }
}