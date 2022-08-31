package com.wupol.myopia.business.api.management.service;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanPageDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.facade.ScreeningRelatedFacade;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2021/4/23 10:31
 */
@Service
public class ManagementScreeningPlanBizService {

    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private ScreeningRelatedFacade screeningRelatedFacade;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private ScreeningNoticeBizService screeningNoticeBizService;

    /**
     * 分页查询
     *
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<ScreeningPlanPageDTO> getPage(ScreeningPlanQueryDTO query, PageRequest pageRequest) {
        if (StringUtils.isNotBlank(query.getCreatorNameLike()) && screeningRelatedFacade.initCreateUserIdsAndReturnIsEmpty(query)) {
            return new Page<>();
        }

        if (StringUtils.isNotBlank(query.getScreeningOrgNameLike())) {
            List<Integer> orgIds = screeningOrganizationService.getByNameLike(query.getScreeningOrgNameLike()).stream().map(ScreeningOrganization::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(orgIds)) {
                // 可以直接返回空
                return new Page<>();
            }
            query.setScreeningOrgIds(orgIds);
        }
        IPage<ScreeningPlanPageDTO> screeningPlanIPage = screeningPlanService.selectPageByQuery(pageRequest.toPage(), query);
        // 设置创建人、地址、部门名称及机构名称
        List<Integer> allGovDeptIds = screeningPlanIPage.getRecords().stream().map(ScreeningPlanPageDTO::getGovDeptId).distinct().collect(Collectors.toList());
        Map<Integer, String> govDeptIdNameMap = org.springframework.util.CollectionUtils.isEmpty(allGovDeptIds) ? Collections.emptyMap() : govDeptService.getByIds(allGovDeptIds).stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
        List<Integer> userIds = screeningPlanIPage.getRecords().stream().map(ScreeningPlan::getCreateUserId).distinct().collect(Collectors.toList());
        Map<Integer, String> userIdNameMap = oauthServiceClient.getUserBatchByIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getRealName));
        List<Integer> screeningOrgIds = screeningPlanIPage.getRecords().stream().map(ScreeningPlan::getScreeningOrgId).collect(Collectors.toList());
        Map<Integer, ScreeningOrganization> orgMap = CollectionUtils.isEmpty(screeningOrgIds) ? new HashMap<>() : screeningOrganizationService.getByIds(screeningOrgIds).stream().collect(Collectors.toMap(ScreeningOrganization::getId, x -> x));
        screeningPlanIPage.getRecords().forEach(vo -> {
            ScreeningOrganization org = orgMap.get(vo.getScreeningOrgId());
            vo.setCreatorName(userIdNameMap.getOrDefault(vo.getCreateUserId(), ""))
                    .setDistrictName(districtService.getDistrictNameByDistrictId(vo.getDistrictId()))
                    .setGovDeptName(govDeptIdNameMap.getOrDefault(vo.getGovDeptId(), ""))
                    .setScreeningOrgName(Objects.nonNull(org) ? org.getName() : "")
                    .setQrCodeConfig(Objects.nonNull(org) ? org.getQrCodeConfig() : "");
        });
        return screeningPlanIPage;
    }

    /**
     * 获取发布的筛查计划
     *
     * @param user 当前登录用户
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan>
     **/
    public List<ScreeningPlan> getReleaseScreeningPlanByUser(CurrentUser user) {
        List<ScreeningNotice> screeningNotices = screeningNoticeBizService.getRelatedNoticeByUser(user);
        Set<Integer> screeningNoticeIds = screeningNotices.stream().map(ScreeningNotice::getId).collect(Collectors.toSet());
        // 筛查机构可以直接创建计划，不需要有通知下发
        if (user.isScreeningUser() || (user.isHospitalUser() && (Objects.nonNull(user.getScreeningOrgId()))) || user.isPlatformAdminUser()) {
            screeningNoticeIds.add(ScreeningConstant.NO_EXIST_NOTICE);
        }
        return this.getScreeningPlanByNoticeIdsOrTaskIdsAndUser(screeningNoticeIds, null, user);
    }

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

    /**
     * 查找用户在参与筛查通知（发布筛查通知，或者接收筛查通知）中，所有筛查计划(已发布，无论开不开始）
     *
     * @param noticeId
     * @param user
     * @return
     */
    public List<ScreeningPlan> getScreeningPlanByNoticeIdAndUser(Integer noticeId, CurrentUser user) {
        if (ObjectsUtil.hasNull(noticeId, user)) {
            return new ArrayList<>();
        }
        Set<Integer> noticeSet = new HashSet<>();
        noticeSet.add(noticeId);
        return getScreeningPlanByNoticeIdsOrTaskIdsAndUser(noticeSet, null, user);
    }

    /**
     * @param taskId
     * @param user
     * @return
     */
    public List<ScreeningPlan> getScreeningPlanByTaskIdAndUser(Integer taskId, CurrentUser user) {
        if (ObjectsUtil.hasNull(taskId, user)) {
            return new ArrayList<>();
        }
        Set<Integer> taskSet = new HashSet<>();
        taskSet.add(taskId);
        return getScreeningPlanByNoticeIdsOrTaskIdsAndUser(null, taskSet, user);
    }

}
