package com.wupol.myopia.business.aggregation.screening.facade;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 管理端筛查计划业务
 *
 * @author hang.yuan 2022/9/16 21:02
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManagementScreeningPlanFacade {

    private final GovDeptService govDeptService;
    private final ScreeningPlanService screeningPlanService;


    /**
     * 查找用户在参与筛查通知（发布筛查通知，或者接收筛查通知）中，所有筛查计划
     *
     * @param noticeIds 通知ID集
     * @param taskIds   任务ID集
     * @param user      当前用户
     * @return
     */
    public List<ScreeningPlan> getScreeningPlanByNoticeIdsOrTaskIdsAndUser(Set<Integer> noticeIds, Set<Integer> taskIds, CurrentUser user) {
        LambdaQueryWrapper<ScreeningPlan> screeningPlanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (user.isScreeningUser() || (user.isHospitalUser() && (Objects.nonNull(user.getScreeningOrgId())))) {
            screeningPlanLambdaQueryWrapper.eq(ScreeningPlan::getScreeningOrgId, user.getScreeningOrgId());
        } else if (user.isGovDeptUser()) {
            List<Integer> allGovDeptIds = govDeptService.getAllSubordinate(user.getOrgId());
            allGovDeptIds.add(user.getOrgId());
            screeningPlanLambdaQueryWrapper.in(ScreeningPlan::getGovDeptId, allGovDeptIds);
        }
        if (CollectionUtils.isEmpty(noticeIds) && CollectionUtils.isEmpty(taskIds)) {
            return Collections.emptyList();
        }
        if (!CollectionUtils.isEmpty(noticeIds)) {
            screeningPlanLambdaQueryWrapper.in(ScreeningPlan::getSrcScreeningNoticeId, noticeIds);
        }
        if (!CollectionUtils.isEmpty(taskIds)) {
            screeningPlanLambdaQueryWrapper.in(ScreeningPlan::getScreeningTaskId, taskIds);
        }
        return screeningPlanService.list(screeningPlanLambdaQueryWrapper.eq(ScreeningPlan::getReleaseStatus, CommonConst.STATUS_RELEASE));
    }
}
