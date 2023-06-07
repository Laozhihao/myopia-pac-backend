package com.wupol.myopia.business.core.screening.flow.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeNameDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.ScreeningNoticeMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.ValidationException;
import java.util.*;
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
    private ScreeningTaskService screeningTaskService;

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

    public IPage<ScreeningNoticeDTO> selectPageByQuery(IPage<?> page, ScreeningNoticeQueryDTO query) {
        return baseMapper.selectPageByQuery(page, query);
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
        List<ScreeningNotice> screeningNoticeList = baseMapper.checkTitleExist(govDeptId, title, screeningNoticeId);
        return CollUtil.isNotEmpty(screeningNoticeList);
    }

    /**
     * 发布筛查通知时，判断时间段是否合法（只查看已发布的且校验type为0）
     * 一个部门在一个时间段内只能发布一个筛查通知【即时间不允许重叠，且只能创建今天之后的时间段】
     *
     * @param screeningNotice：必须存在govDeptId、startTime、endTime
     * @return
     */
    public boolean checkTimeLegal(ScreeningNotice screeningNotice) {
        List<ScreeningNotice> screeningNoticeList = baseMapper.selectByTimePeriods(screeningNotice);
        return CollUtil.isNotEmpty(screeningNoticeList);
    }

    /**
     * 通过筛查通知ids查找
     *
     * @param ids ids
     * @return List<ScreeningNotice>
     */
    public List<ScreeningNotice> getByIds(List<Integer> ids) {
        return baseMapper.getByIdsOrderByStartTime(ids);
    }

    /**
     * 根据任务ID获取通知（type为1,3）TODO:机构/学校
     *
     * @param screeningTaskId
     * @return
     */
    public ScreeningNotice getByScreeningTaskId(Integer screeningTaskId) {
        List<ScreeningNotice> screeningNoticeList = getByScreeningTaskId(screeningTaskId, Lists.newArrayList(1));
        return CollUtil.isNotEmpty(screeningNoticeList)?screeningNoticeList.get(0):new ScreeningNotice();
    }

    /**
     * 根据任务ID获取通知（type为1,3）
     *
     * @param screeningTaskId
     * @return
     */
    public List<ScreeningNotice> getByScreeningTaskId(Integer screeningTaskId,List<Integer> typeList) {
        return baseMapper.selectList(Wrappers.lambdaQuery(ScreeningNotice.class)
                .eq(ScreeningNotice::getScreeningTaskId,screeningTaskId)
                .in(CollUtil.isNotEmpty(typeList),ScreeningNotice::getType,typeList));
    }

    /**
     * 获取筛查机构发布的筛查计划所对应的通知
     *
     * @return
     */
    public List<ScreeningNotice> getNoticeBySreeningUser(Integer screeningOrgId) {
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
    public List<ScreeningNotice> getAllReleaseNotice() {
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
     * @param screeningNotices
     * @param year
     */
    public List<ScreeningNoticeNameDTO> getScreeningNoticeNameDTO(List<ScreeningNotice> screeningNotices, Integer year) {
        screeningNotices = screeningNotices.stream().sorted(Comparator.comparing(ScreeningNotice::getReleaseTime).reversed()).collect(Collectors.toList());
        return screeningNotices.stream().filter(screeningNotice ->
                year.equals(DateUtil.getYear(screeningNotice.getStartTime())) || year.equals(DateUtil.getYear(screeningNotice.getEndTime()))
        ).map(screeningNotice -> {
            ScreeningNoticeNameDTO screeningNoticeNameDTO = new ScreeningNoticeNameDTO();
            screeningNoticeNameDTO.setNoticeTitle(screeningNotice.getTitle())
                    .setNoticeId(screeningNotice.getId())
                    .setScreeningStartTime(screeningNotice.getStartTime())
                    .setScreeningEndTime(screeningNotice.getEndTime())
                    .setScreeningType(screeningNotice.getScreeningType());
            return screeningNoticeNameDTO;
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

    public List<ScreeningNotice> getByDistrictIds(List<Integer> districtIds) {
        LambdaQueryWrapper<ScreeningNotice> screeningNoticeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        screeningNoticeLambdaQueryWrapper
                .in(ScreeningNotice::getDistrictId, districtIds)
                .orderByDesc(ScreeningNotice::getStartTime);
        return baseMapper.selectList(screeningNoticeLambdaQueryWrapper);
    }

    /**
     * 创建筛查通知或发布时校验
     * 1. 开始时间只能在今天或以后
     * 2. 一个部门在一个时间段内只能发布一个筛查通知【即时间不允许重叠，且只能创建今天之后的时间段】
     * 3. 同一个部门下，筛查标题唯一性，要进行校验，标题不能相同。
     *
     * @param screeningNotice
     */
    public void createOrReleaseValidate(ScreeningNotice screeningNotice) {
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningNotice.getStartTime())) {
            throw new ValidationException("筛查开始时间不能早于今天");
        }
        // 一个部门在一个时间段内只能发布一个筛查通知【即时间不允许重叠，且只能创建今天之后的时间段】
        if (checkTimeLegal(screeningNotice)) {
            throw new ValidationException("该部门该时间段已存在筛查通知");
        }
        // 同一个部门下，筛查标题唯一性，要进行校验，标题不能相同。
        if (checkTitleExist(screeningNotice.getId(), screeningNotice.getGovDeptId(), screeningNotice.getTitle())) {
            throw new ValidationException("该部门已存在相同标题通知");
        }
    }

    /**
     * 获取通知
     */
    public List<ScreeningNotice> getReleaseNotice() {
        LambdaQueryWrapper<ScreeningNotice> screeningNoticeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        screeningNoticeLambdaQueryWrapper.eq(ScreeningNotice::getType, ScreeningNotice.TYPE_GOV_DEPT);
        screeningNoticeLambdaQueryWrapper.eq(ScreeningNotice::getReleaseStatus, CommonConst.STATUS_RELEASE);
        return baseMapper.selectList(screeningNoticeLambdaQueryWrapper);
    }

}
