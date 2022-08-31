package com.wupol.myopia.business.api.management.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.schedule.DistrictStatisticTask;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.stat.domain.model.SchoolMonitorStatistic;
import com.wupol.myopia.business.core.stat.domain.model.SchoolVisionStatistic;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.service.SchoolMonitorStatisticService;
import com.wupol.myopia.business.core.stat.service.SchoolVisionStatisticService;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2022/8/31
 **/

@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class ScreeningPlanApiService {

    private final ScreeningPlanService screeningPlanService;
    private final ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    private final ScreeningNoticeService screeningNoticeService;
    private final SchoolVisionStatisticService schoolVisionStatisticService;
    private final SchoolMonitorStatisticService schoolMonitorStatisticService;
    private final ScreeningResultStatisticService screeningResultStatisticService;
    private final DistrictStatisticTask districtStatisticTask;
    private final VisionScreeningResultService visionScreeningResultService;
    private final ScreeningPlanSchoolService screeningPlanSchoolService;
    private final ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    /**
     * 作废筛查计划
     *
     * @param planId 筛查计划ID
     * @return void
     **/
    public void abolishScreeningPlan(Integer planId) {
        Assert.notNull(planId, "筛查计划ID不能为空");
        ScreeningPlan screeningPlan = screeningPlanService.getById(planId);
        Assert.isTrue(Objects.nonNull(screeningPlan) && !CommonConst.STATUS_ABOLISH.equals(screeningPlan.getReleaseStatus()), "无效筛查计划");
        // 1.身份校验，仅平台管理员可以作废计划
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        Assert.isTrue(currentUser.isPlatformAdminUser(), "无操作权限，请联系平台管理员");
        // 2.更新计划状态为作废状态
        screeningPlanService.updateById(new ScreeningPlan().setId(planId).setReleaseStatus(CommonConst.STATUS_ABOLISH));
        // 3.筛查通知状态变更未创建
        if (Objects.nonNull(screeningPlan.getScreeningTaskId()) && !CommonConst.DEFAULT_ID.equals(screeningPlan.getScreeningTaskId())) {
            ScreeningNotice screeningNotice = screeningNoticeService.getByScreeningTaskId(screeningPlan.getScreeningTaskId());
            ScreeningNoticeDeptOrg screeningNoticeDeptOrg = new ScreeningNoticeDeptOrg().setOperationStatus(CommonConst.STATUS_NOTICE_READ).setOperatorId(currentUser.getId()).setScreeningTaskPlanId(0);
            screeningNoticeDeptOrgService.update(screeningNoticeDeptOrg, new ScreeningNoticeDeptOrg().setScreeningNoticeId(screeningNotice.getId()).setAcceptOrgId(screeningPlan.getScreeningOrgId()));
        }
        // 4. 删除相关统计数据
        schoolVisionStatisticService.remove(new SchoolVisionStatistic().setScreeningPlanId(planId));
        schoolMonitorStatisticService.remove(new SchoolMonitorStatistic().setScreeningPlanId(planId));
        screeningResultStatisticService.deleteByPlanId(planId);
        // 5. 删除并重跑所属通知的区域统计数据
        if (Objects.nonNull(screeningPlan.getSrcScreeningNoticeId()) && !CommonConst.DEFAULT_ID.equals(screeningPlan.getScreeningTaskId())) {
            screeningResultStatisticService.remove(new ScreeningResultStatistic().setScreeningNoticeId(screeningPlan.getSrcScreeningNoticeId()).setSchoolId(-1));
            districtStatisticTask.districtStatisticsByNoticeIds(Lists.newArrayList(screeningPlan.getSrcScreeningNoticeId()), null);
        }
    }

    /**
     * 删除筛查计划学校
     *
     * @param planId    计划ID
     * @param schoolId  学校ID
     * @return void
     **/
    public void deletePlanSchool(Integer planId, Integer schoolId) {
        int count = visionScreeningResultService.count(new VisionScreeningResult().setPlanId(planId).setSchoolId(schoolId));
        Assert.isTrue(count <= 0, "该学校已有筛查数据，不可删除！");
        screeningPlanSchoolService.remove(new ScreeningPlanSchool().setScreeningPlanId(planId).setSchoolId(schoolId));
        screeningPlanSchoolStudentService.remove(new ScreeningPlanSchoolStudent().setScreeningPlanId(planId).setSchoolId(schoolId));
    }
}
