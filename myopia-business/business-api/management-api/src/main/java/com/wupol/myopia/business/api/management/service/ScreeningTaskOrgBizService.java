package com.wupol.myopia.business.api.management.service;

import com.alibaba.excel.util.CollectionUtils;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalAdmin;
import com.wupol.myopia.business.core.hospital.service.HospitalAdminService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskOrgDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationAdmin;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationAdminService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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
        return orgVoLists.stream().map(orgVo -> {
            ScreeningTaskOrgDTO dto = new ScreeningTaskOrgDTO();
            BeanUtils.copyProperties(orgVo, dto);
            dto.setName(screeningOrganizationService.getNameById(orgVo.getScreeningOrgId()));
            ScreeningPlan screeningPlan = screeningPlanService.getPlanByTaskId(orgVo.getScreeningTaskId(),orgVo.getScreeningOrgId());
            List<ScreeningPlanSchool> schools =  screeningPlanSchoolService.getSchoolListsByPlanId(screeningPlan.getId());
            dto.setScreeningSchoolNum(schools.size());
            dto.setScreeningSituation(findByScreeningSituation(schools,screeningPlan));
            //调查问卷暂时为0
            dto.setQuestionnaire(getScreeningState(0,0,0,1));
            return dto;
        }).collect(Collectors.toList());
    }

    private String findByScreeningSituation(List<ScreeningPlanSchool> schools, ScreeningPlan screeningPlan) {
        //未开始
        int notStart = 0;
        //进行中
        int underWay = 0;
        //已结束
        int end = 0;

        for (ScreeningPlanSchool ps : schools){
          List<Integer> list = visionScreeningResultService.getByPlanIdAndSchoolId(screeningPlan.getId(),ps.getSchoolId());
          if (list.size()>0){
              underWay++;
          }else {
              notStart++;
          }
        }
        if (DateUtil.betweenDay(screeningPlan.getEndTime(),new Date())<0){
            end = underWay + notStart;
            notStart = 0;
            underWay = 0;
        }
        return getScreeningState(notStart,underWay,end,0);
    }

    private String getScreeningState(int notStart,int underWay,int end,int type){
        if (type==0){
            return "未开始/进行中/已完成："+notStart+"/"+underWay+"/"+end;
        }
        return "未开始/已完成："+notStart+"/"+end;
    }

    public List<ScreeningTaskOrgDTO> getScreeningSchoolDetails(Integer screeningTaskId) {
      List<ScreeningPlan> planList = screeningPlanService.getPlanByTaskIds(screeningTaskId);
        return planList.stream().map(orgVo -> {
            ScreeningTaskOrgDTO dto = new ScreeningTaskOrgDTO();
            BeanUtils.copyProperties(orgVo, dto);
            dto.setName(screeningOrganizationService.getNameById(orgVo.getScreeningOrgId()));
            Map<Integer, Long> schoolIdStudentCountMap = screeningPlanSchoolStudentService.getSchoolStudentCountByScreeningPlanId(orgVo.getId());
            dto.getScreeningPlanSchools().forEach(vo -> {
                        vo.setSchoolName(screeningPlanSchoolService.getOneByPlanIdAndSchoolId(orgVo.getId(),vo.getSchoolId()).getSchoolName());
                        vo.setStudentCount(schoolIdStudentCountMap.getOrDefault(vo.getSchoolId(), (long) 0).intValue());
                        vo.setPracticalStudentCount(visionScreeningResultService.getBySchoolIdAndOrgIdAndPlanId(vo.getSchoolId(), vo.getScreeningOrgId(), vo.getScreeningPlanId()).size());
                        vo.setScreeningProportion(MathUtil.divide(vo.getPracticalStudentCount(),vo.getStudentCount()).toString()+"%");
                        vo.setScreeningSituation(screeningPlanSchoolService.findSituation(vo.getSchoolId(),orgVo));
                        vo.setQuestionnaireStudentCount(0);
                        vo.setQuestionnaireProportion("0.00%");
                        vo.setQuestionnaireSituation(ScreeningPlanSchool.notStart);
                    }
            );
            return dto;
        }).collect(Collectors.toList());
    }
}
