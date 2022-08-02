package com.wupol.myopia.business.api.management.service;

import com.alibaba.excel.util.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wupol.myopia.base.constant.QuestionnaireUserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanSchoolBizService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalAdmin;
import com.wupol.myopia.business.core.hospital.service.HospitalAdminService;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ScreeningSchoolCount;
import com.wupol.myopia.business.core.screening.flow.domain.dto.GradeQuestionnaireInfo;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanSchoolDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskOrgDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationAdmin;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationAdminService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2021/4/25 15:44
 */
@Service
public class ScreeningTaskOrgBizService {
    private static final Integer ZERO = 0;

    private static final Integer ONE = 1;


    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Autowired
    private ScreeningOrganizationAdminService screeningOrganizationAdminService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private ScreeningTaskOrgService screeningTaskOrgService;
    @Autowired
    private ScreeningTaskService screeningTaskService;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private HospitalAdminService hospitalAdminService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;
    @Autowired
    private ScreeningPlanSchoolBizService screeningPlanSchoolBizService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private UserQuestionRecordService userQuestionRecordService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private StudentService studentService;



    /**
     * 批量更新或新增筛查任务的机构信息（删除非列表中的筛查机构）
     * @param screeningTaskId
     * @param screeningOrgs
     */
    public void saveOrUpdateBatchWithDeleteExcludeOrgsByTaskId(CurrentUser user, Integer screeningTaskId, List<ScreeningTaskOrg> screeningOrgs) {
        // 删除掉已有的不存在的机构信息
        List<Integer> excludeOrgIds = CollectionUtils.isEmpty(screeningOrgs) ? Collections.emptyList() : screeningOrgs.stream().map(ScreeningTaskOrg::getScreeningOrgId).collect(Collectors.toList());
        screeningTaskOrgService.deleteByTaskIdAndExcludeOrgIds(screeningTaskId, excludeOrgIds);
        if (!CollectionUtils.isEmpty(screeningOrgs)) {
            saveOrUpdateBatchByTaskId(user, screeningTaskId, screeningOrgs, false);
        }
    }

    /**
     * 批量更新或新增筛查任务的机构信息
     * @param screeningTaskId
     * @param screeningOrgs
     */
    public void saveOrUpdateBatchByTaskId(CurrentUser user, Integer screeningTaskId, List<ScreeningTaskOrg> screeningOrgs, boolean needNotice) {
        // 1. 查出剩余的
        Map<Integer, Integer> orgIdMap = screeningTaskOrgService.getOrgListsByTaskId(screeningTaskId).stream().collect(Collectors.toMap(ScreeningTaskOrg::getScreeningOrgId, ScreeningTaskOrg::getId));
        // 2. 更新id，并批量新增或修改
        screeningOrgs.forEach(taskOrg -> taskOrg.setScreeningTaskId(screeningTaskId).setId(orgIdMap.getOrDefault(taskOrg.getScreeningOrgId(), null)));
        screeningTaskOrgService.saveOrUpdateBatch(screeningOrgs);
        if (needNotice) {
            ScreeningTask screeningTask = screeningTaskService.getById(screeningTaskId);
            ScreeningNotice screeningNotice = screeningNoticeService.getByScreeningTaskId(screeningTaskId);
            this.noticeBatch(user, screeningTask, screeningNotice, screeningOrgs);
        }
    }

    /**
     * 批量通知
     * @param user
     * @param screeningTask
     * @param screeningNotice
     * @return
     */
    public Boolean noticeBatchByScreeningTask(CurrentUser user, ScreeningTask screeningTask, ScreeningNotice screeningNotice) {
        List<ScreeningTaskOrg> orgLists = screeningTaskOrgService.getOrgListsByTaskId(screeningTask.getId());
        return noticeBatch(user, screeningTask, screeningNotice, orgLists);
    }

    /**
     * 批量通知（已通知的不重复通知）
     * @param user
     * @param screeningTask
     * @param screeningNotice
     * @param orgLists
     * @return
     */
    private Boolean noticeBatch(CurrentUser user, ScreeningTask screeningTask, ScreeningNotice screeningNotice, List<ScreeningTaskOrg> orgLists) {
        List<Integer> existAcceptOrgIds = screeningNoticeDeptOrgService.getByScreeningNoticeId(screeningNotice.getId()).stream().map(ScreeningNoticeDeptOrg::getAcceptOrgId).collect(Collectors.toList());
        List<ScreeningNoticeDeptOrg> screeningNoticeDeptOrgs = orgLists.stream().filter(org -> !existAcceptOrgIds.contains(org.getScreeningOrgId())).map(org -> new ScreeningNoticeDeptOrg().setScreeningNoticeId(screeningNotice.getId()).setDistrictId(screeningTask.getDistrictId()).setAcceptOrgId(org.getScreeningOrgId()).setOperatorId(user.getId())).collect(Collectors.toList());
        boolean result = screeningNoticeDeptOrgService.saveBatch(screeningNoticeDeptOrgs);

        // 查找筛查机构用户
        List<Integer> orgIds = orgLists.stream().map(ScreeningTaskOrg::getScreeningOrgId).collect(Collectors.toList());
        List<ScreeningOrganizationAdmin> adminLists = screeningOrganizationAdminService.getByOrgIds(orgIds);
        // 通知绑定了该筛查机构的医院信息
        List<HospitalAdmin> hospitalAdmins = hospitalAdminService.getHospitalAdminByOrgIds(orgIds);
        List<Integer> toUserIds = adminLists.stream().map(ScreeningOrganizationAdmin::getUserId).collect(Collectors.toList());
        toUserIds.addAll(hospitalAdmins.stream().map(HospitalAdmin::getUserId).collect(Collectors.toList()));
        if (!CollectionUtils.isEmpty(toUserIds)) {
            // 为消息中心创建通知
            noticeService.batchCreateNotice(user.getId(), screeningTask.getScreeningNoticeId(), toUserIds, CommonConst.NOTICE_SCREENING_DUTY, screeningTask.getTitle(), screeningTask.getTitle(), screeningTask.getStartTime(), screeningTask.getEndTime());
        }
        return result;
    }

    /**
     * 根据任务Id获取机构列表-带机构名称
     * @param screeningTaskId
     * @return
     */
    public List<ScreeningTaskOrgDTO> getOrgVoListsByTaskId(Integer screeningTaskId) {
        List<ScreeningTaskOrg> orgVoLists = screeningTaskOrgService.getOrgListsByTaskId(screeningTaskId);
        // 批量获取筛查机构信息
        List<ScreeningOrganization> screeningOrgList = screeningOrganizationService.getByIds(orgVoLists.stream().map(ScreeningTaskOrg::getScreeningOrgId).collect(Collectors.toList()));
        Map<Integer, String> screeningOrgNameMap = screeningOrgList.stream().collect(Collectors.toMap(ScreeningOrganization::getId, ScreeningOrganization::getName));
        // 批量获取筛查计划信息
        List<ScreeningPlan> screeningPlanList = screeningPlanService.findByList(new ScreeningPlan().setScreeningTaskId(screeningTaskId));
        Map<Integer, ScreeningPlan> planGroupByOrgIdMap = screeningPlanList.stream().collect(Collectors.toMap(ScreeningPlan::getScreeningOrgId, Function.identity()));
        // 统计每个计划下的筛查学校数量
        List<ScreeningPlanSchool> planSchoolList = screeningPlanSchoolService.getByPlanIds(screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toList()));
        Map<Integer, Long> planSchoolCountMap = planSchoolList.stream().collect(Collectors.groupingBy(ScreeningPlanSchool::getScreeningPlanId, Collectors.counting()));
        // 统计筛查中的学校数量
        List<ScreeningSchoolCount> screeningSchoolCountList = visionScreeningResultService.countScreeningSchoolByTaskId(screeningTaskId);
        Map<Integer, Integer> schoolCountMap = screeningSchoolCountList.stream().collect(Collectors.toMap(ScreeningSchoolCount::getPlanId, ScreeningSchoolCount::getSchoolCount));
        List<UserQuestionRecord> userQuestionRecords = userQuestionRecordService.findRecordByPlanIdAndUserType(Lists.newArrayList(screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toSet())), QuestionnaireUserType.STUDENT.getType());

        return orgVoLists.stream().map(orgVo -> {
            ScreeningTaskOrgDTO dto = new ScreeningTaskOrgDTO();
            BeanUtils.copyProperties(orgVo, dto);
            dto.setName(screeningOrgNameMap.getOrDefault(orgVo.getScreeningOrgId(), StringUtils.EMPTY));
            List<ScreeningPlanSchool> orgSchools = planSchoolList.stream().filter(item -> item.getScreeningOrgId().equals(orgVo.getScreeningOrgId())).collect(Collectors.toList());
            Set<Integer> schoolIds = orgSchools.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toSet());
            Map<Integer, ScreeningPlanSchool> orgSchoolsMap = orgSchools.stream().collect(Collectors.toMap(ScreeningPlanSchool::getSchoolId, screeningPlanSchool -> screeningPlanSchool));
            Map<Integer, ScreeningPlan> planMap = screeningPlanList.stream().filter(item -> item.getScreeningOrgId().equals(orgVo.getScreeningOrgId())).collect(Collectors.toMap(ScreeningPlan::getId, screeningPlan -> screeningPlan));

            List<UserQuestionRecord> planUserQuestionRecords = userQuestionRecords.stream().filter(item -> planMap.containsKey(item.getPlanId())).collect(Collectors.toList());
            dto.setQuestionnaire(findQuestionnaireBySchool(schoolIds, orgSchoolsMap, planMap,planUserQuestionRecords));
            ScreeningPlan screeningPlan = planGroupByOrgIdMap.get(orgVo.getScreeningOrgId());
            if (screeningPlan == null) {
                return dto.setScreeningSchoolNum(0).setScreeningSituation(getScreeningState(0, 0, 0, 0));
            }
            int total = Optional.ofNullable(planSchoolCountMap.get(screeningPlan.getId())).map(Long::intValue).orElse(0);
            return dto.setScreeningSchoolNum(total)
                    .setScreeningSituation(findByScreeningSituation(total, Optional.ofNullable(schoolCountMap.get(screeningPlan.getId())).orElse(0), screeningPlan.getEndTime()));
        }).collect(Collectors.toList());
    }

    /**
     * 获得问卷情况学生的百分比
     *
     * @param schoolIds
     * @param schoolPlanMap
     * @param planMap
     * @return
     */
    private String findQuestionnaireBySchool(Set<Integer> schoolIds, Map<Integer, ScreeningPlanSchool> schoolPlanMap, Map<Integer, ScreeningPlan> planMap,List<UserQuestionRecord> userQuestionRecords) {
        Map<Integer, List<UserQuestionRecord>> schoolMap =userQuestionRecords.stream().collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));
        List<String> schoolStatus = schoolIds.stream().filter(item -> {
            if (Objects.nonNull(schoolPlanMap.get(item))) {
                return Objects.nonNull(planMap.get(schoolPlanMap.get(item).getScreeningPlanId()));
            }
            return false;
        }).map(schoolId -> {
            ScreeningPlan plan = planMap.get(schoolPlanMap.get(schoolId).getScreeningPlanId());
            return screeningPlanSchoolBizService.getCountBySchool(plan, schoolId, schoolMap);
        }).collect(Collectors.toList());

        AtomicInteger notStart = new AtomicInteger(0);
        AtomicInteger underWay = new AtomicInteger(0);
        AtomicInteger end = new AtomicInteger(0);
        schoolStatus.forEach(item->{
            if(ScreeningPlanSchool.NOT_START.equals(item)){
                notStart.addAndGet(1);
            }
            if(ScreeningPlanSchool.END.equals(item)){
                end.addAndGet(1);
            }
            if(ScreeningPlanSchool.IN_PROGRESS.equals(item)){
                underWay.addAndGet(1);
            }
        });
        return getScreeningState(notStart.get(), underWay.get(), end.get(), 0);
    }

    /**
     * 根据学校获得学生总数
     *
     * @param schoolIds
     * @param taskId
     * @param schoolPlanMap
     * @param planMap
     * @return
     */
    public Integer getQuestionnaireBySchoolStudentCount(Set<Integer> schoolIds, Integer taskId, Map<Integer, ScreeningPlanSchool> schoolPlanMap, Map<Integer, ScreeningPlan> planMap){
        // 学生总数
        Map<Integer, List<ScreeningPlanSchoolStudent>> studentCountMaps = screeningPlanSchoolStudentService.findStudentByTaskIdAndSchoolsIds(taskId,schoolIds).stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolId));
        return schoolIds.stream().mapToInt(item -> {
            ScreeningPlan plan = planMap.get(schoolPlanMap.get(item).getScreeningPlanId());
            if (Objects.isNull(studentCountMaps.get(item))) {
                return ZERO;
            }
            if (plan.getEndTime().getTime() <= System.currentTimeMillis()) {
                return ONE;
            }
            return ZERO;
        }).sum();
    }

    private String findByScreeningSituation(int total, int screeningCount, Date screeningEndTime) {
        if (DateUtil.betweenDay(screeningEndTime, new Date()) > 0) {
            return getScreeningState(0, 0, total, 0);
        }
        return getScreeningState(total - screeningCount, screeningCount, 0, 0);
    }

    private String getScreeningState(int notStart, int underWay, int end, int type) {
        if (type == 0) {
            return "未开始/进行中/已结束：" + notStart + "/" + underWay + "/" + end;
        }
        return "未开始/已结束：" + notStart + "/" + end;
    }

    /**
     * 获取学校筛查详情
     * TODO：跟上面getOrgVoListsByTaskId()合并
     *
     * @param screeningTaskId   筛查任务ID
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskOrgDTO>
     **/
    public List<ScreeningTaskOrgDTO> getScreeningSchoolDetails(Integer screeningTaskId) {
        List<ScreeningTaskOrg> orgVoLists = screeningTaskOrgService.getOrgListsByTaskId(screeningTaskId);
        // 批量获取筛查机构信息
        List<ScreeningOrganization> screeningOrgList = screeningOrganizationService.getByIds(orgVoLists.stream().map(ScreeningTaskOrg::getScreeningOrgId).collect(Collectors.toList()));
        Map<Integer, String> screeningOrgNameMap = screeningOrgList.stream().collect(Collectors.toMap(ScreeningOrganization::getId, ScreeningOrganization::getName));
        // 批量获取筛查计划信息
        List<ScreeningPlan> screeningPlanList = screeningPlanService.findByList(new ScreeningPlan().setScreeningTaskId(screeningTaskId));
        Map<Integer, ScreeningPlan> planGroupByOrgIdMap = screeningPlanList.stream().collect(Collectors.toMap(ScreeningPlan::getScreeningOrgId, Function.identity()));
        // 批量获取筛查学校信息
        List<ScreeningPlanSchool> planSchoolList = screeningPlanSchoolService.getByPlanIds(screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toList()));
        Map<Integer, List<ScreeningPlanSchool>> planSchoolGroupByPlanIdMap = planSchoolList.stream().collect(Collectors.groupingBy(ScreeningPlanSchool::getScreeningPlanId));
        List<UserQuestionRecord> userQuestionRecords = userQuestionRecordService.findRecordByPlanIdAndUserType(screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toList()),QuestionnaireUserType.STUDENT.getType());
        Map<Integer, List<UserQuestionRecord>> planRecords = userQuestionRecords.stream().collect(Collectors.groupingBy(UserQuestionRecord::getPlanId));


        return orgVoLists.stream().map(orgVo -> {
            ScreeningTaskOrgDTO dto = new ScreeningTaskOrgDTO();
            BeanUtils.copyProperties(orgVo, dto);
            dto.setName(screeningOrgNameMap.getOrDefault(orgVo.getScreeningOrgId(), StringUtils.EMPTY));
            ScreeningPlan plan = planGroupByOrgIdMap.get(orgVo.getScreeningOrgId());
            // TODO：不在循环内操作数据库
            if (plan != null) {
                Map<Integer, Long> schoolIdStudentCountMap = screeningPlanSchoolStudentService.getSchoolStudentCountByScreeningPlanId(plan.getId());
                List<ScreeningPlanSchool> screeningPlanSchools = planSchoolGroupByPlanIdMap.get(plan.getId());
                if (!CollectionUtils.isEmpty(screeningPlanSchools)) {
                    dto.setScreeningPlanSchools(getScreeningPlanSchools(CollectionUtils.isEmpty(planRecords.get(plan.getId())) ? Lists.newArrayList() : planRecords.get(plan.getId()), screeningPlanSchools, schoolIdStudentCountMap, plan));
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private List<ScreeningPlanSchoolDTO> getScreeningPlanSchools(List<UserQuestionRecord> userQuestionRecords,List<ScreeningPlanSchool> screeningPlanSchools, Map<Integer, Long> schoolIdStudentCountMap, ScreeningPlan screeningPlan) {
        Map<Integer, ScreeningPlanSchool> schoolPlanMap = screeningPlanSchools.stream().collect(Collectors.toMap(ScreeningPlanSchool::getSchoolId, screeningPlanSchool -> screeningPlanSchool));
        HashMap<Integer, ScreeningPlan> planMap = Maps.newHashMap();
        planMap.put(screeningPlan.getId(), screeningPlan);
        Set<Integer> studentIds = userQuestionRecords.stream().
                map(UserQuestionRecord::getStudentId).collect(Collectors.toSet());
        Map<Integer, List<Student>> userGradeIdMap = CollectionUtils.isEmpty(studentIds) ? Maps.newHashMap() : studentService.getByIds(studentIds)
                .stream().collect(Collectors.groupingBy(Student::getGradeId));
        Map<Integer, List<UserQuestionRecord>> schoolMap = userQuestionRecords.stream().collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));
        Map<Integer, List<SchoolGradeExportDTO>> gradeIdMap = schoolGradeService.getBySchoolIds(Lists.newArrayList(schoolPlanMap.keySet())).stream().collect(Collectors.groupingBy(SchoolGradeExportDTO::getSchoolId));

        return screeningPlanSchools.stream().map(vo -> {
            ScreeningPlanSchoolDTO schoolDTO = new ScreeningPlanSchoolDTO();
            schoolDTO.setSchoolName(screeningPlanSchoolService.getOneByPlanIdAndSchoolId(screeningPlan.getId(), vo.getSchoolId()).getSchoolName());
            schoolDTO.setStudentCount(schoolIdStudentCountMap.getOrDefault(vo.getSchoolId(), (long) 0).intValue());
            schoolDTO.setPracticalStudentCount(visionScreeningResultService.getBySchoolIdAndOrgIdAndPlanId(vo.getSchoolId(), vo.getScreeningOrgId(), vo.getScreeningPlanId()).size());
            BigDecimal num = MathUtil.divide(schoolDTO.getPracticalStudentCount(), schoolDTO.getStudentCount());
            if (num.equals(BigDecimal.ZERO)) {
                schoolDTO.setScreeningProportion(CommonConst.PERCENT_ZERO);
            } else {
                schoolDTO.setScreeningProportion(num.toString() + "%");
            }
            schoolDTO.setScreeningSituation(screeningPlanSchoolBizService.findSituation(vo.getSchoolId(), screeningPlan));
            buildQuestionDto(schoolDTO, vo, screeningPlan, userGradeIdMap, gradeIdMap, schoolMap);
            return schoolDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 构建问卷数据
     *
     * @return
     */
    public ScreeningPlanSchoolDTO buildQuestionDto(ScreeningPlanSchoolDTO schoolDTO,
                                                   ScreeningPlanSchool vo,
                                                   ScreeningPlan screeningPlan,
                                                   Map<Integer, List<Student>> userGradeIdMap,
                                                   Map<Integer, List<SchoolGradeExportDTO>> gradeIdMap,
                                                   Map<Integer, List<UserQuestionRecord>> schoolMap){
        Integer questionnaireStudentCount = CollectionUtils.isEmpty(schoolMap.get(vo.getSchoolId())) ? 0 : schoolMap.get(vo.getSchoolId()).stream().map(UserQuestionRecord::getStudentId).collect(Collectors.toSet()).size();
        schoolDTO.setQuestionnaireStudentCount(questionnaireStudentCount);
        if (schoolDTO.getStudentCount() == BigDecimal.ZERO.intValue()) {
            schoolDTO.setQuestionnaireProportion(CommonConst.PERCENT_ZERO);
        } else {
            BigDecimal questionNum = MathUtil.divide(schoolDTO.getQuestionnaireStudentCount(), schoolDTO.getStudentCount());
            schoolDTO.setQuestionnaireProportion(questionNum.equals(BigDecimal.ZERO) ? CommonConst.PERCENT_ZERO : questionNum.toString() + "%");
        }
        schoolDTO.setQuestionnaireSituation(screeningPlanSchoolBizService.getCountBySchool(screeningPlan, vo.getSchoolId(), schoolMap));
        if (!CollectionUtils.isEmpty(gradeIdMap.get(vo.getSchoolId())) && schoolDTO.getStudentCount() != 0) {
            schoolDTO.setGradeQuestionnaireInfos(
                    GradeQuestionnaireInfo.buildGradeInfo(vo.getSchoolId(), gradeIdMap, userGradeIdMap));
        }
        return schoolDTO;
    }
}