package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.mapper.ScreeningNoticeMapper;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningNoticeQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningNoticeNameVO;
import com.wupol.myopia.business.management.domain.vo.ScreeningNoticeVo;
import com.wupol.myopia.business.management.facade.ScreeningRelatedFacade;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ScreeningNoticeService extends BaseService<ScreeningNoticeMapper, ScreeningNotice> {
    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private ScreeningTaskService screeningTaskService;
    @Autowired
    private ScreeningRelatedFacade screeningRelatedFacade;

    /**
     * 设置操作人再更新
     *
     * @param entity
     * @param userId
     * @return
     */
    public boolean updateById(ScreeningNotice entity, Integer userId) {
        entity.setOperateTime(new Date()).setOperatorId(userId);
        return updateById(entity);
    }

    /**
     * 分页查询
     *
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<ScreeningNoticeVo> getPage(ScreeningNoticeQuery query, PageRequest pageRequest) {
        Page<ScreeningNotice> page = (Page<ScreeningNotice>) pageRequest.toPage();
        if (StringUtils.isNotBlank(query.getCreatorNameLike()) && screeningRelatedFacade.initCreateUserIdsAndReturnIsEmpty(query)) {
            return new Page<>();
        }
        IPage<ScreeningNoticeVo> screeningNoticeIPage = baseMapper.selectPageByQuery(page, query);
        List<Integer> userIds = screeningNoticeIPage.getRecords().stream().map(ScreeningNotice::getCreateUserId).distinct().collect(Collectors.toList());
        Map<Integer, String> userIdNameMap = oauthServiceClient.getUserBatchByIds(userIds).getData().stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getRealName));
        screeningNoticeIPage.getRecords().forEach(vo -> vo.setCreatorName(userIdNameMap.getOrDefault(vo.getCreateUserId(), "")).setDistrictDetail(districtService.getDistrictPositionDetailById(vo.getDistrictId())));
        return screeningNoticeIPage;
    }

    /**
     * 发布通知
     *
     * @param id
     * @return
     */
    public void release(Integer id, CurrentUser user) {
        //1. 更新状态&发布时间
        ScreeningNotice notice = getById(id);
        notice.setId(id).setReleaseStatus(CommonConst.STATUS_RELEASE).setReleaseTime(new Date());
        if (!updateById(notice, user.getId())) {
            throw new BusinessException("发布失败");
        }
        List<GovDept> govDepts = govDeptService.getAllSubordinateWithDistrictId(notice.getGovDeptId());
        List<ScreeningNoticeDeptOrg> screeningNoticeDeptOrgs = govDepts.stream().map(govDept -> new ScreeningNoticeDeptOrg().setScreeningNoticeId(id).setDistrictId(govDept.getDistrictId()).setAcceptOrgId(govDept.getId()).setOperatorId(user.getId())).collect(Collectors.toList());
        //2. 为下属部门创建通知
        screeningNoticeDeptOrgService.saveBatch(screeningNoticeDeptOrgs);
        // 3. 为消息中心创建通知
        List<Integer> govOrgIds = govDepts.stream().map(GovDept::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(govOrgIds)) {
            return;
        }
        ApiResult<List<UserDTO>> userBatchByOrgIds = oauthServiceClient.getUserBatchByOrgIds(govOrgIds, SystemCode.MANAGEMENT_CLIENT.getCode());
        List<Integer> toUserIds = userBatchByOrgIds.getData().stream().map(UserDTO::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(toUserIds)) {
            noticeService.batchCreateScreeningNotice(user.getId(), id, toUserIds, CommonConst.NOTICE_SCREENING_NOTICE, notice.getTitle(), notice.getTitle(), notice.getStartTime(), notice.getEndTime());
        }
    }

    /**
     * 部门是否已存在该标题
     *
     * @param screeningNoticeId 已有的ID，更新时使用。新增时可为null
     * @param govDeptId         部门ID
     * @param title             标题
     * @return
     */
    public boolean checkTitleExist(Integer screeningNoticeId, Integer govDeptId, String title) {
        return baseMapper.checkTitleExist(govDeptId, title, screeningNoticeId).size() > 0;
    }

    /**
     * 发布筛查通知时，判断时间段是否合法（只查看已发布的且校验type为0）
     * 一个部门在一个时间段内只能发布一个筛查通知【即时间不允许重叠，且只能创建今天之后的时间段】
     *
     * @param screeningNotice：必须存在govDeptId、startTime、endTime
     * @return
     */
    public boolean checkTimeLegal(ScreeningNotice screeningNotice) {
        return baseMapper.selectByTimePeriods(screeningNotice).size() > 0;
    }

    /**
     * 通过筛查通知ids查找
     *
     * @param ids ids
     * @return List<ScreeningNotice>
     */
    public List<ScreeningNotice> getByIds(List<Integer> ids) {
        return baseMapper.getByIdsOrderByCreateTime(ids);
    }

    /**
     * 根据任务ID获取通知（type为1）
     *
     * @param screeningTaskId
     * @return
     */
    public ScreeningNotice getByScreeningTaskId(Integer screeningTaskId) {
        return baseMapper.getByTaskId(screeningTaskId);
    }

    /**
     * 获取该用户所在部门参与的筛查通知（发布筛查通知，或者接受过筛查通知）
     *
     * @param user
     * @return
     */
    public List<ScreeningNotice> getRelatedNoticeByUser(CurrentUser user) {
        if (user.isGovDeptUser()) {
            //查找所有的上级部门
            Set<Integer> superiorGovIds = govDeptService.getSuperiorGovIds(user.getOrgId());
            superiorGovIds.add(user.getOrgId());
            //查找政府发布的通知
            return this.getNoticeByReleaseOrgId(superiorGovIds, ScreeningNotice.TYPE_GOV_DEPT);
        }
        if (user.isPlatformAdminUser()) {
            //这里只是查找政府的通知
            return this.getAllReleaseNotice();
        }
        if (user.isScreeningUser()) {
            //该部门发布的通知
            return this.getNoticeBySreeningUser(user.getOrgId());
        }
        return Collections.emptyList();
    }

    /**
     * 获取筛查机构发布的筛查计划所对应的通知
     *
     * @return
     */
    private List<ScreeningNotice> getNoticeBySreeningUser(Integer screeningOrgId) {
        if (screeningOrgId == null) {
            return Collections.emptyList();
        }
        // 1.筛查机构自己创建的筛查计划(无论发布与否）
        List<ScreeningNotice> screeningNotices = screeningNoticeDeptOrgService.selectByAcceptIdAndType(screeningOrgId, ScreeningNotice.TYPE_ORG);
        Set<Integer> taskIds = screeningNotices.stream().map(ScreeningNotice::getScreeningTaskId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(taskIds)) {
            return Collections.emptyList();
        }
        List<ScreeningTask> screeningTasks = screeningTaskService.listByIds(taskIds);
        Set<Integer> noticeIds = screeningTasks.stream().map(ScreeningTask::getScreeningNoticeId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(noticeIds)) {
            return Collections.emptyList();
        }
        // 从 第一点注释 中发现，政府的筛查通知一定已经发布
        return listByIds(noticeIds);
    }

    /**
     * 获取所有已经发布的政府通知
     *
     * @return
     */
    private List<ScreeningNotice> getAllReleaseNotice() {
        LambdaQueryWrapper<ScreeningNotice> screeningNoticeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        screeningNoticeLambdaQueryWrapper
                .eq(ScreeningNotice::getReleaseStatus, CommonConst.STATUS_RELEASE)
                .eq(ScreeningNotice::getType, ScreeningNotice.TYPE_GOV_DEPT);
        return baseMapper.selectList(screeningNoticeLambdaQueryWrapper);
    }

    /**
     * 根据发布部门获取通知
     *
     * @param orgIds
     * @param orgType
     * @return
     */
    public List<ScreeningNotice> getNoticeByReleaseOrgId(Set<Integer> orgIds, Integer orgType) {
        if (CollectionUtils.isEmpty(orgIds) || orgType == null) {
            return Collections.emptyList();
        }
        List<ScreeningNotice> screeningNotices = new ArrayList<>();
        Lists.partition(new ArrayList<>(orgIds), 100).forEach(orgIdList -> {
            LambdaQueryWrapper<ScreeningNotice> screeningNoticeLambdaQueryWrapper = new LambdaQueryWrapper<>();
            screeningNoticeLambdaQueryWrapper.eq(ScreeningNotice::getType, orgType);
            screeningNoticeLambdaQueryWrapper.in(ScreeningNotice::getGovDeptId, orgIdList);
            screeningNoticeLambdaQueryWrapper.eq(ScreeningNotice::getReleaseStatus, CommonConst.STATUS_RELEASE);
            screeningNotices.addAll(baseMapper.selectList(screeningNoticeLambdaQueryWrapper));
        });
        return screeningNotices;
    }

    /**
     * 获取年度
     *
     * @return
     */
    public List<Integer> getYears(List<ScreeningNotice> screeningNotices) {
        Set<Integer> yearSet = new HashSet<>();
        screeningNotices.forEach(screeningNotice -> {
            Integer startYear = DateUtil.getYear(screeningNotice.getStartTime());
            Integer endYear = DateUtil.getYear(screeningNotice.getEndTime());
            yearSet.add(startYear);
            yearSet.add(endYear);
        });
        return new ArrayList<>(yearSet).stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }

    /**
     * 获取已经发布的通知
     *
     * @param noticeId
     * @return
     */
    public ScreeningNotice getReleasedNoticeById(Integer noticeId) {
        ScreeningNotice screeningNotice = getById(noticeId);
        if (screeningNotice == null) {
            throw new BusinessException("无法找到该通知");
        }
        if (!screeningNotice.getReleaseStatus().equals(CommonConst.STATUS_RELEASE)) {
            throw new BusinessException("该通知未发布");
        }
        return screeningNotice;
    }


    /**
     * 获取筛查任务名字
     *
     * @param screeningNoticeIds
     * @param year
     */
    public List<ScreeningNoticeNameVO> getScreeningNoticeNameVO(Set<Integer> screeningNoticeIds, Integer year) {
        List<ScreeningNotice> screeningNotices = listByIds(screeningNoticeIds);
        screeningNotices = screeningNotices.stream().sorted(Comparator.comparing(ScreeningNotice::getReleaseTime).reversed()).collect(Collectors.toList());
        return screeningNotices.stream().filter(screeningNotice ->
                year.equals(DateUtil.getYear(screeningNotice.getStartTime())) || year.equals(DateUtil.getYear(screeningNotice.getEndTime()))
        ).map(screeningNotice -> {
            ScreeningNoticeNameVO screeningNoticeNameVO = new ScreeningNoticeNameVO();
            screeningNoticeNameVO.setNoticeTitle(screeningNotice.getTitle()).setNoticeId(screeningNotice.getId()).setScreeningStartTime(screeningNotice.getStartTime()).setScreeningEndTime(screeningNotice.getEndTime());
            return screeningNoticeNameVO;
        }).collect(Collectors.toList());
    }

    /**
     * 获取最新
     *
     * @param currentUser
     * @return
     */
    public ScreeningNotice getLatestNoticeByUser(CurrentUser currentUser) {
        // 获取发布的筛查通知最新的数据
        Set<Integer> govDeptIds = new HashSet<>();
        govDeptIds.add(currentUser.getOrgId());
        List<ScreeningNotice> screeningNotices = this.getNoticeByReleaseOrgId(govDeptIds, ScreeningNotice.TYPE_GOV_DEPT);
        // 最新排序
        List<ScreeningNotice> screeningNoticeList = screeningNotices.stream().sorted(Comparator.comparing(ScreeningNotice::getReleaseTime).reversed()).collect(Collectors.toList());
        // 取出第一条
        Optional<ScreeningNotice> screeningNoticeOptional = screeningNoticeList.stream().findFirst();
        return screeningNoticeOptional.orElse(null);
    }
}
