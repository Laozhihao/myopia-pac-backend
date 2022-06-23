package com.wupol.myopia.business.api.management.service;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.excel.util.StringUtils;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalAdmin;
import com.wupol.myopia.business.core.hospital.service.HospitalAdminService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ScreeningSchoolCount;
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

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2021/4/25 15:44
 */
@Service
public class ScreeningTaskOrgBizService {

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
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;



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
        if (!org.springframework.util.CollectionUtils.isEmpty(toUserIds)) {
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
        Map<Integer, ScreeningPlan> planMap = screeningPlanList.stream().collect(Collectors.toMap(ScreeningPlan::getScreeningOrgId, Function.identity()));
        // 统计每个计划下的筛查学校数量
        List<ScreeningPlanSchool> planSchoolList = screeningPlanSchoolService.getByPlanIds(new ArrayList<>(planMap.keySet()));
        Map<Integer, Long> planSchoolCountMap = planSchoolList.stream().collect(Collectors.groupingBy(ScreeningPlanSchool::getScreeningPlanId, Collectors.counting()));
        // 统计筛查中的学校数量
        List<ScreeningSchoolCount> screeningSchoolCountList = visionScreeningResultService.countScreeningSchoolByTaskId(screeningTaskId);
        Map<Integer, Integer> schoolCountMap = screeningSchoolCountList.stream().collect(Collectors.toMap(ScreeningSchoolCount::getPlanId, ScreeningSchoolCount::getSchoolCount));
        return orgVoLists.stream().map(orgVo -> {
            ScreeningTaskOrgDTO dto = new ScreeningTaskOrgDTO();
            BeanUtils.copyProperties(orgVo, dto);
            dto.setName(screeningOrgNameMap.getOrDefault(orgVo.getScreeningOrgId(), StringUtils.EMPTY));
            // TODO：调查问卷暂时为0
            dto.setQuestionnaire(getScreeningState(0,0,0,1));
            // TODO：差评，开发调查问卷模块时，优化该模块：学校进度统计仅返回数量，不拼接文字，减少与展示样式的耦合
            ScreeningPlan screeningPlan = planMap.get(orgVo.getScreeningOrgId());
            if (screeningPlan == null){
                return dto.setScreeningSchoolNum(0).setScreeningSituation(getScreeningState(0,0,0,0));
            }
            int total =  Optional.ofNullable(planSchoolCountMap.get(screeningPlan.getId())).map(Long::intValue).orElse(0);
            return dto.setScreeningSchoolNum(total)
                    .setScreeningSituation(findByScreeningSituation(total, Optional.of(schoolCountMap.get(screeningPlan.getId())).orElse(0), screeningPlan.getEndTime()));
        }).collect(Collectors.toList());
    }

    private String findByScreeningSituation(int total, int screeningCount, Date screeningEndTime) {
        if (DateUtil.betweenDay(screeningEndTime, new Date()) > 0){
            return getScreeningState(0,0, total,0);
        }
        return getScreeningState(total - screeningCount, screeningCount, 0,0);
    }

    private String getScreeningState(int notStart,int underWay,int end,int type){
        if (type==0){
            return "未开始/进行中/已结束："+notStart+"/"+underWay+"/"+end;
        }
        return "未开始/已结束："+notStart+"/"+end;
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
        Map<Integer, ScreeningPlan> planMap = screeningPlanList.stream().collect(Collectors.toMap(ScreeningPlan::getScreeningOrgId, Function.identity()));
        // 批量获取筛查学校信息
        List<ScreeningPlanSchool> planSchoolList = screeningPlanSchoolService.getByPlanIds(new ArrayList<>(planMap.keySet()));
        Map<Integer, List<ScreeningPlanSchool>> planSchoolGroupByPlanIdMap = planSchoolList.stream().collect(Collectors.groupingBy(ScreeningPlanSchool::getScreeningPlanId));
        return orgVoLists.stream().map(orgVo -> {
            ScreeningTaskOrgDTO dto = new ScreeningTaskOrgDTO();
            BeanUtils.copyProperties(orgVo, dto);
            dto.setName(screeningOrgNameMap.getOrDefault(orgVo.getScreeningOrgId(), StringUtils.EMPTY));
            ScreeningPlan plan = planMap.get(orgVo.getScreeningOrgId());
            // TODO：不在循环内操作数据库
            if (plan != null){
                Map<Integer, Long> schoolIdStudentCountMap = screeningPlanSchoolStudentService.getSchoolStudentCountByScreeningPlanId(plan.getId());
                List<ScreeningPlanSchool> screeningPlanSchools = planSchoolGroupByPlanIdMap.get(plan.getId());
                if (!screeningPlanSchools.isEmpty()){
                    dto.setScreeningPlanSchools(getScreeningPlanSchools(screeningPlanSchools, schoolIdStudentCountMap, plan));
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private List<ScreeningPlanSchoolDTO> getScreeningPlanSchools(List<ScreeningPlanSchool> screeningPlanSchools,Map<Integer, Long> schoolIdStudentCountMap,ScreeningPlan screeningPlan){
            return screeningPlanSchools.stream().map(vo -> {
                ScreeningPlanSchoolDTO schoolDTO = new ScreeningPlanSchoolDTO();
                schoolDTO.setSchoolName(screeningPlanSchoolService.getOneByPlanIdAndSchoolId(screeningPlan.getId(),vo.getSchoolId()).getSchoolName());
                schoolDTO.setStudentCount(schoolIdStudentCountMap.getOrDefault(vo.getSchoolId(), (long) 0).intValue());
                schoolDTO.setPracticalStudentCount(visionScreeningResultService.getBySchoolIdAndOrgIdAndPlanId(vo.getSchoolId(), vo.getScreeningOrgId(), vo.getScreeningPlanId()).size());
                BigDecimal num = MathUtil.divide(schoolDTO.getPracticalStudentCount(),schoolDTO.getStudentCount());
                if (num.equals(BigDecimal.ZERO)){
                    schoolDTO.setScreeningProportion("0.00%");
                }else {
                    schoolDTO.setScreeningProportion(num.toString()+"%");
                }
                schoolDTO.setScreeningSituation(screeningPlanSchoolService.findSituation(vo.getSchoolId(),screeningPlan));
                schoolDTO.setQuestionnaireStudentCount(0);
                schoolDTO.setQuestionnaireProportion("0.00%");
                schoolDTO.setQuestionnaireSituation(ScreeningPlanSchool.NOT_START);
                return schoolDTO;
            }).collect(Collectors.toList());
    }
}
