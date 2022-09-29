package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningTaskAndDistrictVO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskPageDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTaskOrg;
import com.wupol.myopia.business.core.screening.flow.facade.ScreeningRelatedFacade;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2021/4/25 16:25
 */
@Service
public class ScreeningTaskBizService {

    @Autowired
    private ScreeningTaskService screeningTaskService;
    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Autowired
    private ScreeningTaskOrgBizService screeningTaskOrgBizService;
    @Autowired
    private ScreeningTaskOrgService screeningTaskOrgService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private ScreeningRelatedFacade screeningRelatedFacade;
    @Autowired
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private ScreeningNoticeBizService screeningNoticeBizService;

    /**
     * 新增或更新
     *
     * @param screeningTaskDTO
     */
    public void saveOrUpdateWithScreeningOrgs(CurrentUser user, ScreeningTaskDTO screeningTaskDTO, boolean needUpdateNoticeStatus) {
        // 新增或更新筛查任务信息
        screeningTaskDTO.setOperatorId(user.getId());
        if (!screeningTaskService.saveOrUpdate(screeningTaskDTO)) {
            throw new BusinessException("创建失败");
        }
        // 新增或更新筛查机构信息
        screeningTaskOrgBizService.saveOrUpdateBatchWithDeleteExcludeOrgsByTaskId(user, screeningTaskDTO.getId(), screeningTaskDTO.getScreeningOrgs());
        if (needUpdateNoticeStatus) {
            //更新通知状态＆更新ID
            screeningNoticeDeptOrgService.statusReadAndCreate(screeningTaskDTO.getScreeningNoticeId(), screeningTaskDTO.getGovDeptId(), screeningTaskDTO.getId(), user.getId());
        }
    }

    /**
     * 获取任务DTO（带行政区明细）
     *
     * @param screeningTaskId
     * @return
     */
    public ScreeningTaskAndDistrictVO getScreeningTaskAndDistrictById(Integer screeningTaskId) {
        ScreeningTaskAndDistrictVO screeningTaskAndDistrictVO = ScreeningTaskAndDistrictVO.build(screeningTaskService.getById(screeningTaskId));
        screeningTaskAndDistrictVO.setDistrictDetail(districtService.getDistrictPositionDetailById(screeningTaskAndDistrictVO.getDistrictId()));
        return screeningTaskAndDistrictVO;
    }

    /**
     * 分页查询
     *
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<ScreeningTaskPageDTO> getPage(ScreeningTaskQueryDTO query, PageRequest pageRequest) {
        Page<ScreeningTask> page = (Page<ScreeningTask>) pageRequest.toPage();
        if (StringUtils.isNotBlank(query.getCreatorNameLike()) && screeningRelatedFacade.initCreateUserIdsAndReturnIsEmpty(query)) {
            return new Page<>();
        }
        IPage<ScreeningTaskPageDTO> screeningTaskIPage = screeningTaskService.selectPageByQuery(page, query);
        List<Integer> userIds = screeningTaskIPage.getRecords().stream().map(ScreeningTask::getCreateUserId).distinct().collect(Collectors.toList());
        Map<Integer, String> userIdNameMap = oauthServiceClient.getUserBatchByIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getRealName));
        screeningTaskIPage.getRecords().forEach(vo ->
                vo.setDistrictName(districtService.getDistrictNameByDistrictId(vo.getDistrictId()))
                        .setCreatorName(userIdNameMap.getOrDefault(vo.getCreateUserId(), ""))
                        .setGovDeptName(govDeptService.getNameById(vo.getGovDeptId()))
        );
        return screeningTaskIPage;
    }

    /**
     * 发布任务
     *
     * @param id
     * @return
     */
    public void release(Integer id, CurrentUser user) {
        //1. 更新状态&发布时间
        ScreeningTask screeningTask = screeningTaskService.getById(id);
        screeningTask.setReleaseStatus(CommonConst.STATUS_RELEASE).setReleaseTime(new Date());
        if (!screeningTaskService.updateById(screeningTask, user.getId())) {
            throw new BusinessException("发布失败");
        }

        //2. 发布通知
        List<ScreeningNotice> screeningNoticeList = getScreeningNoticeList(id, user, screeningTask);
        if (CollUtil.isEmpty(screeningNoticeList)){
            throw new BusinessException("发布失败,筛查通知为空");
        }
        screeningNoticeService.saveBatch(screeningNoticeList);

        //3. 为筛查机构/学校创建通知
        for (ScreeningNotice screeningNotice : screeningNoticeList) {
            screeningTaskOrgBizService.noticeBatchByScreeningTask(user, screeningTask, screeningNotice);
        }
    }

    /**
     * 获取发布通知
     * @param id
     * @param user
     * @param screeningTask
     */
    private List<ScreeningNotice> getScreeningNoticeList(Integer id, CurrentUser user, ScreeningTask screeningTask) {
        List<ScreeningNotice> screeningNoticeList = Lists.newArrayList();
        List<ScreeningTaskOrg> screeningTaskOrgList = screeningTaskOrgService.getOrgListsByTaskId(screeningTask.getId());
        Map<Integer, List<ScreeningTaskOrg>> orgTypeMap = screeningTaskOrgList.stream().collect(Collectors.groupingBy(ScreeningTaskOrg::getScreeningOrgType));
        if (orgTypeMap.containsKey(ScreeningOrgTypeEnum.ORG.getType())){
            screeningNoticeList.add(buildScreeningNotice(id, user, screeningTask,ScreeningNotice.TYPE_ORG));
        }
        if (orgTypeMap.containsKey(ScreeningOrgTypeEnum.SCHOOL.getType())){
            screeningNoticeList.add(buildScreeningNotice(id, user, screeningTask,ScreeningNotice.TYPE_SCHOOL));
        }
        return screeningNoticeList;
    }

    /**
     * 构建筛查通知
     * @param id
     * @param user
     * @param screeningTask
     * @param type
     */
    private ScreeningNotice buildScreeningNotice(Integer id, CurrentUser user, ScreeningTask screeningTask,Integer type) {
        ScreeningNotice screeningNotice = new ScreeningNotice();
        BeanUtils.copyProperties(screeningTask, screeningNotice);
        screeningNotice.setCreateUserId(user.getId())
                .setOperatorId(user.getId())
                .setOperateTime(new Date())
                .setScreeningTaskId(id)
                .setGovDeptId(CommonConst.DEFAULT_ID)
                .setType(type)
                .setReleaseStatus(CommonConst.STATUS_RELEASE)
                .setReleaseTime(new Date());
        return screeningNotice;
    }

    public List<ScreeningTask> getScreeningTaskByUser(CurrentUser user) {
        List<ScreeningNotice> screeningNotices = screeningNoticeBizService.getRelatedNoticeByUser(user);
        Set<Integer> screeningNoticeIds = screeningNotices.stream().map(ScreeningNotice::getId).collect(Collectors.toSet());
        return this.getScreeningTaskByNoticeIdsAndUser(screeningNoticeIds, user);
    }

    public List<ScreeningTask> getScreeningTaskByNoticeIdsAndUser(Set<Integer> noticeIds, CurrentUser user) {
        if (CollectionUtils.isEmpty(noticeIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ScreeningTask> screeningTaskLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (user.isScreeningUser() || user.isHospitalUser()) {
            throw new BusinessException("医院、筛查机构无权限获取任务信息");
        } else if (user.isGovDeptUser()) {
            List<Integer> allGovDeptIds = govDeptService.getAllSubordinate(user.getOrgId());
            allGovDeptIds.add(user.getOrgId());
            screeningTaskLambdaQueryWrapper.in(ScreeningTask::getGovDeptId, allGovDeptIds);
        }
        screeningTaskLambdaQueryWrapper.in(ScreeningTask::getScreeningNoticeId, noticeIds).eq(ScreeningTask::getReleaseStatus, CommonConst.STATUS_RELEASE);
        return screeningTaskService.list(screeningTaskLambdaQueryWrapper);
    }

}
