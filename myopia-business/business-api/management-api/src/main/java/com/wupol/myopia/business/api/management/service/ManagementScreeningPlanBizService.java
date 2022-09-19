package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.aggregation.screening.facade.ManagementScreeningPlanFacade;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
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
    @Autowired
    private ManagementScreeningPlanFacade managementScreeningPlanFacade;
    @Autowired
    private SchoolService schoolService;

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
            List<Integer> orgIds = screeningOrganizationService.getByNameLike(query.getScreeningOrgNameLike(),Boolean.FALSE).stream().map(ScreeningOrganization::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(orgIds)) {
                // 可以直接返回空
                return new Page<>();
            }
            query.setScreeningOrgIds(orgIds);
        }
        IPage<ScreeningPlanPageDTO> screeningPlanPage = screeningPlanService.selectPageByQuery(pageRequest.toPage(), query);
        // 设置创建人、地址、部门名称及机构名称
        List<Integer> allGovDeptIds = screeningPlanPage.getRecords().stream().map(ScreeningPlanPageDTO::getGovDeptId).distinct().collect(Collectors.toList());
        Map<Integer, String> govDeptIdNameMap = org.springframework.util.CollectionUtils.isEmpty(allGovDeptIds) ? Collections.emptyMap() : govDeptService.getByIds(allGovDeptIds).stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
        List<Integer> userIds = screeningPlanPage.getRecords().stream().map(ScreeningPlan::getCreateUserId).distinct().collect(Collectors.toList());
        Map<Integer, String> userIdNameMap = oauthServiceClient.getUserBatchByIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getRealName));
        List<Integer> screeningOrgIds = screeningPlanPage.getRecords().stream().map(ScreeningPlan::getScreeningOrgId).collect(Collectors.toList());
        Map<Integer, ScreeningOrganization> orgMap = CollectionUtils.isEmpty(screeningOrgIds) ? new HashMap<>() : screeningOrganizationService.getByIds(screeningOrgIds).stream().collect(Collectors.toMap(ScreeningOrganization::getId, x -> x));

        List<ScreeningPlanPageDTO> records = screeningPlanPage.getRecords();
        if (CollUtil.isEmpty(records)){
            return screeningPlanPage;
        }
        Set<Integer> schoolIds = records.stream()
                .filter(screeningPlanPageDTO -> Objects.equals(screeningPlanPageDTO.getScreeningOrgType(), ScreeningOrgTypeEnum.SCHOOL.getType()))
                .map(ScreeningPlan::getScreeningOrgId)
                .collect(Collectors.toSet());
        Map<Integer,String> schoolNameMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(schoolIds)){
            List<School> schoolList = schoolService.listByIds(schoolIds);
            Map<Integer, String> collect = schoolList.stream().collect(Collectors.toMap(School::getId, School::getName));
            schoolNameMap.putAll(collect);
        }

        records.forEach(vo -> {
            ScreeningOrganization org = orgMap.get(vo.getScreeningOrgId());
            String orgName;
            if (Objects.equals(vo.getScreeningOrgType(), ScreeningOrgTypeEnum.SCHOOL.getType())) {
                orgName = schoolNameMap.getOrDefault(vo.getScreeningOrgId(), StrUtil.EMPTY);
            }else {
                orgName = Optional.ofNullable(org).map(ScreeningOrganization::getName).orElse(StrUtil.EMPTY);
            }
            vo.setCreatorName(userIdNameMap.getOrDefault(vo.getCreateUserId(), StrUtil.EMPTY))
                    .setDistrictName(districtService.getDistrictNameByDistrictId(vo.getDistrictId()))
                    .setGovDeptName(govDeptIdNameMap.getOrDefault(vo.getGovDeptId(), StrUtil.EMPTY))
                    .setScreeningOrgName(orgName)
                    .setQrCodeConfig(Optional.ofNullable(org).map(ScreeningOrganization::getQrCodeConfig).orElse(StrUtil.EMPTY));

        });
        return screeningPlanPage;
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
        return managementScreeningPlanFacade.getScreeningPlanByNoticeIdsOrTaskIdsAndUser(screeningNoticeIds, null, user);
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
        return managementScreeningPlanFacade.getScreeningPlanByNoticeIdsOrTaskIdsAndUser(noticeSet, null, user);
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
        return managementScreeningPlanFacade.getScreeningPlanByNoticeIdsOrTaskIdsAndUser(null, taskSet, user);
    }

}
