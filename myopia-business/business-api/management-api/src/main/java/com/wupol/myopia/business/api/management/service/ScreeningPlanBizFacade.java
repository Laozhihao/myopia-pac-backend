package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.PlanLinkNoticeRequestDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.stat.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.validation.ValidationException;
import java.util.List;
import java.util.Objects;

/**
 * 筛查计划业务
 *
 * @author hang.yuan 2022/10/12 17:20
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ScreeningPlanBizFacade {

    private final ScreeningOrganizationService screeningOrganizationService;
    private final ScreeningPlanService screeningPlanService;
    private final ScreeningTaskOrgService screeningTaskOrgService;
    private final ScreeningTaskService screeningTaskService;
    private final SchoolService schoolService;
    private final ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    private final ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    private final StatConclusionService statConclusionService;
    private final VisionScreeningResultService visionScreeningResultService;
    private final StatRescreenService statRescreenService;
    private final SchoolMonitorStatisticService schoolMonitorStatisticService;
    private final SchoolVisionStatisticService schoolVisionStatisticService;
    private final ScreeningResultStatisticService screeningResultStatisticService;


    @Transactional(rollbackFor = Exception.class)
    public void createInfo(ScreeningPlanDTO screeningPlanDTO, CurrentUser user) {

        validPlanParam(screeningPlanDTO, user);

        // 若为筛查人员或医生，只能发布自己机构的计划
        if (user.isScreeningUser() || (user.isHospitalUser() && (Objects.nonNull(user.getScreeningOrgId())))) {
            screeningPlanDTO.setScreeningOrgId(user.getScreeningOrgId());
            screeningPlanDTO.setScreeningOrgType(ScreeningOrgTypeEnum.ORG.getType());
        }

        // 校验筛查类型权限-机构
        orgPlan(screeningPlanDTO);
        //校验筛查类型权限-学校
        schoolPlan(screeningPlanDTO);
        screeningPlanDTO.setCreateUserId(user.getId());
        screeningPlanService.saveOrUpdateWithSchools(user.getId(), screeningPlanDTO, true);
    }

    /**
     * 校验计划参数
     * @param screeningPlanDTO
     * @param user
     */
    private void validPlanParam(ScreeningPlanDTO screeningPlanDTO, CurrentUser user) {
        Assert.notNull(screeningPlanDTO.getScreeningType(), "筛查类型不能为空");
        // 校验用户机构，政府部门，无法新增计划
        if (user.isGovDeptUser()) {
            throw new ValidationException("无权限");
        }
        // 平台管理员，筛查机构ID必传
        if (user.isPlatformAdminUser()) {
            Assert.notNull(screeningPlanDTO.getScreeningOrgId(), "筛查机构ID不能为空");
            Assert.notNull(screeningPlanDTO.getScreeningOrgType(), "筛查机构类型不能为空");
            if (Objects.equals(screeningPlanDTO.getScreeningOrgType(), ScreeningOrgTypeEnum.SCHOOL.getType())
                    && Objects.equals(screeningPlanDTO.getScreeningType(), ScreeningTypeEnum.COMMON_DISEASE.getType())){
                throw new BusinessException("筛查机构是学校时，筛查类型不能为常见病筛查");
            }
        }
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningPlanDTO.getStartTime())) {
            throw new ValidationException(BizMsgConstant.VALIDATION_START_TIME_ERROR);
        }
    }

    private void schoolPlan(ScreeningPlanDTO screeningPlanDTO) {
        if (Objects.equals(screeningPlanDTO.getScreeningOrgType(), ScreeningOrgTypeEnum.SCHOOL.getType())){
            School school = schoolService.getById(screeningPlanDTO.getScreeningOrgId());
            Assert.isTrue(school.getScreeningTypeConfig().contains(String.valueOf(screeningPlanDTO.getScreeningType())), "暂未开通该筛查类型配置，如需开通，请联系管理员");
            // 有传screeningTaskId时，需判断是否已创建且筛查任务是否有该筛查机构
            if (Objects.nonNull(screeningPlanDTO.getScreeningTaskId())) {
                if (screeningPlanService.checkIsCreated(screeningPlanDTO.getScreeningTaskId(), screeningPlanDTO.getScreeningOrgId(),screeningPlanDTO.getScreeningOrgType())) {
                    throw new ValidationException("筛查计划已创建");
                }
                ScreeningTaskOrg screeningTaskOrg = screeningTaskOrgService.getOne(screeningPlanDTO.getScreeningTaskId(), screeningPlanDTO.getScreeningOrgId(),screeningPlanDTO.getScreeningOrgType());
                if (Objects.isNull(screeningTaskOrg)) {
                    throw new ValidationException("筛查任务查无该机构");
                }
                ScreeningTask screeningTask = screeningTaskService.getById(screeningPlanDTO.getScreeningTaskId());
                screeningPlanDTO.setSrcScreeningNoticeId(screeningTask.getScreeningNoticeId()).setDistrictId(screeningTask.getDistrictId()).setGovDeptId(screeningTask.getGovDeptId());
            } else {
                // 用户自己新建的筛查计划需设置districtId
                screeningPlanDTO.setDistrictId(school.getDistrictId());
            }
        }
    }

    private void orgPlan(ScreeningPlanDTO screeningPlanDTO) {
        if (Objects.equals(screeningPlanDTO.getScreeningOrgType(), ScreeningOrgTypeEnum.ORG.getType())){
            ScreeningOrganization organization = screeningOrganizationService.getById(screeningPlanDTO.getScreeningOrgId());
            Assert.isTrue(organization.getScreeningTypeConfig().contains(String.valueOf(screeningPlanDTO.getScreeningType())), "暂未开通该筛查类型配置，如需开通，请联系管理员");
            // 有传screeningTaskId时，需判断是否已创建且筛查任务是否有该筛查机构
            if (Objects.nonNull(screeningPlanDTO.getScreeningTaskId())) {
                if (screeningPlanService.checkIsCreated(screeningPlanDTO.getScreeningTaskId(), screeningPlanDTO.getScreeningOrgId(),screeningPlanDTO.getScreeningOrgType())) {
                    throw new ValidationException("筛查计划已创建");
                }
                ScreeningTaskOrg screeningTaskOrg = screeningTaskOrgService.getOne(screeningPlanDTO.getScreeningTaskId(), screeningPlanDTO.getScreeningOrgId(),screeningPlanDTO.getScreeningOrgType());
                if (Objects.isNull(screeningTaskOrg)) {
                    throw new ValidationException("筛查任务查无该机构");
                }
                ScreeningTask screeningTask = screeningTaskService.getById(screeningPlanDTO.getScreeningTaskId());
                screeningPlanDTO.setSrcScreeningNoticeId(screeningTask.getScreeningNoticeId()).setDistrictId(screeningTask.getDistrictId()).setGovDeptId(screeningTask.getGovDeptId());
            } else {
                // 用户自己新建的筛查计划需设置districtId
                screeningPlanDTO.setDistrictId(organization.getDistrictId());
            }
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public Integer linkNotice(PlanLinkNoticeRequestDTO requestDTO) {
        Integer planId = requestDTO.getPlanId();
        Integer screeningNoticeDeptOrgId = requestDTO.getScreeningNoticeDeptOrgId();
        Integer screeningTaskId = requestDTO.getScreeningTaskId();


        ScreeningPlan plan = screeningPlanService.getById(planId);
        if (Objects.isNull(plan)) {
            throw new BusinessException("通过计划Id查询不到计划" + planId);
        }

        ScreeningNoticeDeptOrg deptOrg = screeningNoticeDeptOrgService.getById(screeningNoticeDeptOrgId);
        if (Objects.isNull(deptOrg)) {
            throw new BusinessException("通知机构信息异常" + screeningNoticeDeptOrgId);
        }

        ScreeningTask task = screeningTaskService.getById(screeningTaskId);
        if (Objects.isNull(task)) {
            throw new BusinessException("任务信息异常" + screeningNoticeDeptOrgId);
        }

        Integer districtId = task.getDistrictId();

        Integer screeningNoticeId = task.getScreeningNoticeId();
        plan.setSrcScreeningNoticeId(screeningNoticeId)
                .setScreeningTaskId(screeningTaskId)
                .setDistrictId(districtId);
        screeningPlanService.updateById(plan);


        deptOrg.setOperationStatus(CommonConst.STATUS_NOTICE_CREATED).setScreeningTaskPlanId(planId);
        screeningNoticeDeptOrgService.updateById(deptOrg);

        // 计划学生
        List<ScreeningPlanSchoolStudent> planStudents = screeningPlanSchoolStudentService.getByScreeningPlanId(planId);
        planStudents.forEach(planStudent->{
            planStudent.setPlanDistrictId(districtId)
                    .setScreeningTaskId(screeningTaskId)
                    .setSrcScreeningNoticeId(screeningNoticeId);
        });
        screeningPlanSchoolStudentService.updateBatchById(planStudents);

        // 筛查结果
        List<VisionScreeningResult> visionResults = visionScreeningResultService.getByPlanId(planId);
        visionResults.forEach(result->{
            result.setTaskId(screeningTaskId)
                    .setDistrictId(districtId);
        });
        visionScreeningResultService.updateBatchById(visionResults);

        // 统计结果
        List<StatConclusion> statConclusions = statConclusionService.getByPlanId(planId);
        statConclusions.forEach(statConclusion -> {
            statConclusion.setSrcScreeningNoticeId(screeningNoticeId)
                    .setTaskId(screeningTaskId)
                    .setDistrictId(districtId);
        });

        // 复测统计表
        List<StatRescreen> statRescreens = statRescreenService.getByPlanId(planId);
        statRescreens.forEach(statRescreen -> {
            statRescreen.setSrcScreeningNoticeId(screeningNoticeId).setTaskId(screeningTaskId);
        });
        statRescreenService.updateBatchById(statRescreens);

        // 删除统计数据
        schoolMonitorStatisticService.deleteByPlanId(planId);
        schoolVisionStatisticService.deleteByPlanId(planId);
        screeningResultStatisticService.deleteByPlanId(planId);
        return screeningNoticeId;

    }
}
