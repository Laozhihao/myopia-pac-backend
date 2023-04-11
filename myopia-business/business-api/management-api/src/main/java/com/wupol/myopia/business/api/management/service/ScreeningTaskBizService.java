package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.screening.facade.ScreeningTaskBizFacade;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningTaskAndDistrictVO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskPageDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.facade.ScreeningRelatedFacade;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.validation.ValidationException;
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
    @Autowired
    private ScreeningTaskBizFacade screeningTaskBizFacade;
    @Autowired
    private ScreeningTaskOrgService screeningTaskOrgService;

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
        List<ScreeningNotice> screeningNoticeList = screeningTaskBizFacade.getScreeningNoticeList(id, user, screeningTask);
        if (CollUtil.isEmpty(screeningNoticeList)){
            throw new BusinessException("发布失败,筛查通知为空");
        }
        screeningNoticeService.saveBatch(screeningNoticeList);

        //3. 为筛查机构/学校创建通知
        for (ScreeningNotice screeningNotice : screeningNoticeList) {
            screeningTaskOrgBizService.noticeBatchByScreeningTask(user, screeningTask, screeningNotice);
        }
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

    /**
     * 创建任务
     * @param screeningNotice 通知
     * @param screeningTaskDTO 任务入参
     * @param user 用户
     * @return 任务
     */
    public void createTask(ScreeningNotice screeningNotice, ScreeningTaskDTO screeningTaskDTO, CurrentUser user) {
        // 已创建校验
        if (screeningTaskService.checkIsCreated(screeningNotice.getId(), screeningTaskDTO.getGovDeptId())) {
            throw new ValidationException("该部门任务已创建");
        }
        screeningTaskDTO.setCreateUserId(user.getId());
        saveOrUpdateWithScreeningOrgs(user, screeningTaskDTO, true);
    }

    /**
     * 推送任务
     *
     * @param taskDTO 任务
     */
    public void publishTask(ScreeningTaskDTO taskDTO) {
        // 没有筛查机构，直接报错
        if (CollectionUtils.isEmpty(screeningTaskOrgService.getOrgListsByTaskId(taskDTO.getId()))) {
            throw new ValidationException("无筛查机构");
        }
        release(taskDTO.getId(), CurrentUserUtil.getCurrentUser());
    }

    /**
     * 校验任务是否存在与发布状态
     * 同时校验权限
     *
     * @param screeningTaskId 筛查通知ID
     * @return
     * 筛查通知
     */
    public ScreeningTask validateExistAndAuthorize(Integer screeningTaskId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // 校验用户机构
        if (user.isScreeningUser() || user.isHospitalUser()) {
            // 筛查机构，无权限处理
            throw new ValidationException("无权限");
        }
        ScreeningTask screeningTask = validateExistWithReleaseStatus(screeningTaskId, CommonConst.STATUS_RELEASE);
        if (user.isGovDeptUser()) {
            // 政府部门人员，需校验是否同部门
            Assert.isTrue(user.getOrgId().equals(screeningTask.getGovDeptId()), "无该部门权限");
        }
        return screeningTask;
    }

    /**
     * 校验筛查任务是否存在且校验发布状态
     *
     * @param id 筛查通知id
     * @return 筛查通知
     */
    public ScreeningTask validateExistWithReleaseStatus(Integer id, Integer releaseStatus) {
        ScreeningTask screeningTask = validateExist(id);
        Integer taskStatus = screeningTask.getReleaseStatus();
        if (releaseStatus.equals(taskStatus)) {
            throw new BusinessException(String.format("该任务%s", CommonConst.STATUS_RELEASE.equals(taskStatus) ? "已发布" : "未发布"));
        }
        return screeningTask;
    }

    /**
     * 校验筛查通知是否存在
     *
     * @param id 筛查通知ID
     * @return 筛查通知
     */
    public ScreeningTask validateExist(Integer id) {
        if (Objects.isNull(id)) {
            throw new BusinessException("参数ID不存在");
        }
        ScreeningTask screeningTask = screeningTaskService.getById(id);
        if (Objects.isNull(screeningTask)) {
            throw new BusinessException("查无该任务");
        }
        return screeningTask;
    }

}
