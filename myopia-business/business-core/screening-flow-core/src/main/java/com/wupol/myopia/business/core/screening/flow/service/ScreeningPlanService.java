package com.wupol.myopia.business.core.screening.flow.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.ScreeningConstant;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.ScreeningPlanMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
public class ScreeningPlanService extends BaseService<ScreeningPlanMapper, ScreeningPlan> {

    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;

    /**
     * 更新筛查学生数量
     *
     * @param userId
     * @param screeningPlanId
     * @param studentNumbers
     */
    public boolean updateStudentNumbers(Integer userId, Integer screeningPlanId, Integer studentNumbers) {
        ScreeningPlan screeningPlan = new ScreeningPlan();
        screeningPlan.setId(screeningPlanId).setStudentNumbers(studentNumbers);
        return updateById(screeningPlan, userId);
    }

    /**
     * 通过ids获取
     *
     * @param pageRequest 分页入参
     * @param ids         ids
     * @return IPage<ScreeningPlanResponseDTO>
     */
    public IPage<ScreeningPlanResponseDTO> getListByIds(PageRequest pageRequest, List<Integer> ids, boolean needFilterAbolishPlan) {
        return baseMapper.getPlanLists(pageRequest.toPage(), ids, needFilterAbolishPlan);
    }

    /**
     * 设置操作人再更新
     *
     * @param entity
     * @param userId
     * @return
     */
    public boolean updateById(ScreeningPlan entity, Integer userId) {
        entity.setOperateTime(new Date()).setOperatorId(userId);
        return updateById(entity);
    }

    public IPage<ScreeningPlanPageDTO> selectPageByQuery(Page<?> page, ScreeningPlanQueryDTO query) {
        return baseMapper.selectPageByQuery(page, query);
    }

    /**
     * 发布计划
     *
     * @param id 筛查计划ID
     * @param user 当前用户
     */
    public Boolean release(Integer id, CurrentUser user) {
        //1. 更新状态&发布时间
        ScreeningPlan screeningPlan = getById(id);
        return release(screeningPlan,user);
    }

    /**
     * 发布计划
     *
     * @param screeningPlan 筛查计划
     * @param user 当前用户
     */
    public Boolean release(ScreeningPlan screeningPlan, CurrentUser user) {
        //1. 更新状态&发布时间
        screeningPlan.setReleaseStatus(CommonConst.STATUS_RELEASE).setReleaseTime(new Date());
        return updateById(screeningPlan, user.getId());
    }

    /**
     * 新增或更新
     *
     * @param screeningPlanDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateWithSchools(Integer currentUserId, ScreeningPlanDTO screeningPlanDTO, boolean needUpdateNoticeStatus) {
        // 新增或更新筛查计划信息
        screeningPlanDTO.setOperatorId(currentUserId);
        if (!saveOrUpdate(screeningPlanDTO)) {
            throw new BusinessException("创建失败");
        }
        // 新增或更新筛查学校信息
        screeningPlanSchoolService.saveOrUpdateBatchWithDeleteExcludeSchoolsByPlanId(screeningPlanDTO.getId(), screeningPlanDTO.getScreeningOrgId(), screeningPlanDTO.getSchools());
        if (needUpdateNoticeStatus && Objects.nonNull(screeningPlanDTO.getScreeningTaskId())) {
            // 更新通知状态
            List<Integer> typeList = Lists.newArrayList();
            if (Objects.equals(screeningPlanDTO.getScreeningOrgType(), ScreeningOrgTypeEnum.ORG.getType())) {
                typeList.add(ScreeningNotice.TYPE_ORG);
            }else if (Objects.equals(screeningPlanDTO.getScreeningOrgType(), ScreeningOrgTypeEnum.SCHOOL.getType())) {
                typeList.add(ScreeningNotice.TYPE_SCHOOL);
            }
            List<ScreeningNotice> screeningNoticeList = screeningNoticeService.getByScreeningTaskId(screeningPlanDTO.getScreeningTaskId(), typeList);
            if (Objects.isNull(screeningNoticeList)) {
                throw new BusinessException("找不到对应任务通知");
            }
            ScreeningNotice screeningNotice = screeningNoticeList.get(0);
            screeningNoticeDeptOrgService.statusReadAndCreate(screeningNotice.getId(), screeningPlanDTO.getScreeningOrgId(), screeningPlanDTO.getId(), currentUserId);
        }
    }

    /**
     * 删除，并删除关联的学校信息
     *
     * @param screeningPlanId
     * @return
     */
    public void removeWithSchools(CurrentUser user, Integer screeningPlanId) {
        // 1. 修改通知状态为已读
        ScreeningPlan screeningPlan = getById(screeningPlanId);
        if (!CommonConst.DEFAULT_ID.equals(screeningPlan.getScreeningTaskId())) {
            //自己创建的screening_task_id默认0
            ScreeningNotice screeningNotice = screeningNoticeService.getByScreeningTaskId(screeningPlan.getScreeningTaskId());
            if (Objects.isNull(screeningNotice)) {
                throw new BusinessException("找不到对应任务通知");
            }
            screeningNoticeDeptOrgService.read(screeningNotice.getId(), screeningPlan.getScreeningOrgId(), user);
        }
        // 2. 删除计划关联的学校信息
        screeningPlanSchoolStudentService.deleteByPlanIdAndExcludeSchoolIds(screeningPlanId, Collections.emptyList());
        screeningPlanSchoolService.deleteByPlanIdAndExcludeSchoolIds(screeningPlanId, Collections.emptyList());
        // 3. 删除计划
        removeById(screeningPlanId);
    }

    /**
     * 校验筛查机构是否已创建计划
     *
     * @param screeningTaskId 通知ID
     * @param screeningOrgId  机构ID
     */
    public boolean checkIsCreated(Integer screeningTaskId, Integer screeningOrgId,Integer screeningOrgType) {
        LambdaQueryWrapper<ScreeningPlan> queryWrapper = Wrappers.lambdaQuery(ScreeningPlan.class)
                .eq(ScreeningPlan::getScreeningTaskId, screeningTaskId)
                .eq(ScreeningPlan::getScreeningOrgId, screeningOrgId)
                .eq(ScreeningPlan::getScreeningOrgType, screeningOrgType)
                .ne(ScreeningPlan::getReleaseStatus, 2);
        return baseMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 获取筛查计划id
     *
     * @param districtId
     * @param taskId
     * @return
     */
    public Set<ScreeningPlanSchoolInfoDTO> getByDistrictIdAndTaskId(Integer districtId, Integer taskId) {
        return baseMapper.selectSchoolInfo(districtId, taskId, CommonConst.STATUS_RELEASE);
    }

    /**
     * 筛选年度的计划并组装VO
     *
     * @param screeningPlans
     * @param year
     * @return
     */
    public List<ScreeningPlanNameDTO> getScreeningPlanNameDTOs(List<ScreeningPlan> screeningPlans, Integer year) {
        screeningPlans = screeningPlans.stream().sorted(Comparator.comparing(ScreeningPlan::getReleaseTime).reversed()).collect(Collectors.toList());
        return screeningPlans.stream().filter(screeningPlan ->
                year.equals(DateUtil.getYear(screeningPlan.getStartTime())) || year.equals(DateUtil.getYear(screeningPlan.getEndTime()))
        ).map(screeningPlan -> {
            ScreeningPlanNameDTO screeningTaskNameVO = new ScreeningPlanNameDTO();
            screeningTaskNameVO.setPlanName(screeningPlan.getTitle())
                    .setPlanId(screeningPlan.getId())
                    .setScreeningStartTime(screeningPlan.getStartTime())
                    .setScreeningEndTime(screeningPlan.getEndTime())
                    .setScreeningType(screeningPlan.getScreeningType());
            return screeningTaskNameVO;
        }).collect(Collectors.toList());
    }


    /**
     * 分页获取筛查计划
     *
     * @param pageRequest 分页请求
     * @param orgId       机构ID
     * @param needFilterAbolishPlan   需要排除作废的计划
     * @return IPage<ScreeningTaskResponse>
     */
    public IPage<ScreeningOrgPlanResponseDTO> getPageByOrgId(PageRequest pageRequest, Integer orgId, boolean needFilterAbolishPlan) {
        return baseMapper.getPageByOrgId(pageRequest.toPage(), orgId, needFilterAbolishPlan);
    }

    /**
     * 通过筛查机构ID和机构类型获取计划
     *
     * @param orgId 机构ID
     * @param orgType 机构类型
     * @return List<ScreeningPlan>
     */
    public List<ScreeningPlan> getByOrgIdAndOrgType(Integer orgId,Integer orgType) {
        return baseMapper.selectList(Wrappers.lambdaQuery(ScreeningPlan.class)
                .eq(ScreeningPlan::getScreeningOrgId,orgId)
                .eq(ScreeningPlan::getScreeningOrgType,orgType)
                .eq(ScreeningPlan::getReleaseStatus,CommonConst.STATUS_RELEASE));
    }

    public List<ScreeningPlan> getByTaskIdsAndOrgIdAndOrgType(List<Integer> taskIds,Integer orgId,Integer orgType) {
        return baseMapper.selectList(Wrappers.lambdaQuery(ScreeningPlan.class)
                .in(ScreeningPlan::getScreeningTaskId,taskIds)
                .eq(ScreeningPlan::getScreeningOrgId,orgId)
                .eq(ScreeningPlan::getScreeningOrgType,orgType));
    }

    /**
     * 通过orgIds获取发布的计划
     *
     * @param orgIds 机构Ids
     * @return List<ScreeningPlan>
     */
    public List<ScreeningPlan> getReleasePlanByOrgIds(List<Integer> orgIds) {
        return baseMapper.getReleasePlanByOrgIds(orgIds);
    }

    /**
     * 根据筛查计划ID获取原始的筛查通知ID列表
     *
     * @param screeningPlanIds
     * @return
     */
    public List<Integer> getSrcScreeningNoticeIdsOfReleasePlanByPlanIds(List<Integer> screeningPlanIds) {
        if (CollectionUtils.isEmpty(screeningPlanIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ScreeningPlan> screeningPlanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        screeningPlanLambdaQueryWrapper.in(ScreeningPlan::getId, screeningPlanIds);
        screeningPlanLambdaQueryWrapper.eq(ScreeningPlan::getReleaseStatus, CommonConst.STATUS_RELEASE);
        List<ScreeningPlan> screeningPlans = baseMapper.selectList(screeningPlanLambdaQueryWrapper);
        return screeningPlans.stream().map(ScreeningPlan::getSrcScreeningNoticeId).distinct().collect(Collectors.toList());
    }

    /**
     * @param screeningOrgId
     * @return
     */
    public List<Integer> getReleasePlanSchoolIdByScreeningOrgId(Integer screeningOrgId, Integer channel) {
        List<ScreeningPlanSchool> screeningPlanSchools = screeningPlanSchoolService.getReleasePlanScreeningSchoolsByScreeningOrgId(screeningOrgId);
        List<Integer> planIds = screeningPlanSchools.stream().map(ScreeningPlanSchool::getScreeningPlanId).collect(Collectors.toList());
        List<Integer> planChannelIds = baseMapper.selectList(new LambdaQueryWrapper<ScreeningPlan>()
                        .in(!planIds.isEmpty(), ScreeningPlan::getId, planIds).eq(ScreeningPlan::getScreeningType, channel).eq(ScreeningPlan::getReleaseStatus, CommonConst.STATUS_RELEASE))
                .stream().map(ScreeningPlan::getId).collect(Collectors.toList());
        return screeningPlanSchools.stream().filter(item -> planChannelIds.contains(item.getScreeningPlanId())).map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toList());
    }

    /**
     * 获取用户当前的计划
     *
     * @param deptId
     */
    public Set<Integer> getCurrentReleasePlanIds(Integer deptId) {
        List<ScreeningPlanSchool> screeningPlanSchools = screeningPlanSchoolService.getReleasePlanScreeningSchoolsByScreeningOrgId(deptId);
        return screeningPlanSchools.stream().map(ScreeningPlanSchool::getScreeningPlanId).collect(Collectors.toSet());
    }

    /**
     * 获取用户当前的计划
     *
     * @param deptId
     */
    public Set<Integer> getCurrentReleasePlanIds(Integer deptId, Integer channel) {
        List<ScreeningPlanSchool> screeningPlanSchools = screeningPlanSchoolService.getReleasePlanScreeningSchoolsByScreeningOrgId(deptId);
        List<Integer> planIds = screeningPlanSchools.stream().map(ScreeningPlanSchool::getScreeningPlanId).collect(Collectors.toList());
        List<Integer> planChannelIds = baseMapper.selectList(new LambdaQueryWrapper<ScreeningPlan>()
                        .in(!planIds.isEmpty(), ScreeningPlan::getId, planIds).eq(ScreeningPlan::getScreeningType, channel).eq(ScreeningPlan::getReleaseStatus, CommonConst.STATUS_RELEASE))
                .stream().map(ScreeningPlan::getId).collect(Collectors.toList());
        return screeningPlanSchools.stream().filter(item -> planChannelIds.contains(item.getScreeningPlanId())).map(ScreeningPlanSchool::getScreeningPlanId).collect(Collectors.toSet());
    }

    /**
     * 获取用户当前的计划
     *
     * @param screeningOrgId
     * @param schoolId
     * @return
     */
    public ScreeningPlan getCurrentReleasePlan(Integer screeningOrgId, Integer schoolId, Integer channel) {
        return baseMapper.selectScreeningPlanDetailByOrgIdAndSchoolId(schoolId, screeningOrgId, ScreeningConstant.SCREENING_RELEASE_STATUS, new Date(), channel);
    }

    /**
     * 根据通知获取学生
     *
     * @param noticeId
     * @return
     */
    public long getAllPlanStudentNumByNoticeId(Integer noticeId) {
        List<ScreeningPlan> allPlans = this.getAllReleasePlanByNoticeId(noticeId);
        Integer allPlanStudentNums = allPlans.stream().map(ScreeningPlan::getStudentNumbers).reduce(0, Integer::sum);
        return allPlanStudentNums.longValue();
    }

    /**
     * 获取所有的计划筛查学生数
     *
     * @param noticeId
     * @return
     */
    public List<ScreeningPlan> getAllReleasePlanByNoticeId(Integer noticeId) {
        return baseMapper.selectList(Wrappers.lambdaQuery(ScreeningPlan.class)
                .eq(ScreeningPlan::getSrcScreeningNoticeId, noticeId)
                .eq(ScreeningPlan::getReleaseStatus,CommonConst.STATUS_RELEASE));
    }

    /**
     * 根据通知ID或任务ID或计划ID查询筛查计划
     * @param noticeId 通知ID
     * @param taskId 任务ID
     * @param planId 计划ID
     */
    public List<ScreeningPlan> getReleasePlanByNoticeIdOrTaskIdOrPlanId(Integer noticeId,Integer taskId,Integer planId) {
        Assert.isTrue(!ObjectsUtil.allNull(noticeId,taskId,planId),"通知ID,任务ID,计划ID不能都为空");
        return baseMapper.selectList(Wrappers.lambdaQuery(ScreeningPlan.class)
                .eq(Objects.nonNull(noticeId),ScreeningPlan::getSrcScreeningNoticeId, noticeId)
                .eq(Objects.nonNull(taskId),ScreeningPlan::getScreeningTaskId, taskId)
                .eq(Objects.nonNull(planId),ScreeningPlan::getId, planId)
                .eq(ScreeningPlan::getReleaseStatus,CommonConst.STATUS_RELEASE));
    }

    /**
     * 根据通知ID和发布状态查询筛查计划
     * @param noticeIds 通知ID集合
     * @param releaseStatus 发布状态
     */
    public List<ScreeningPlan> getPlanByNoticeIdsAndStatusBatch(List<Integer> noticeIds, Integer releaseStatus ) {
        return baseMapper.selectList(Wrappers.lambdaQuery(ScreeningPlan.class)
                .in(ScreeningPlan::getSrcScreeningNoticeId, noticeIds)
                .eq(ScreeningPlan::getReleaseStatus,releaseStatus));
    }

    /**
     * 获取年度
     *
     * @return
     */
    public List<Integer> getYears(List<ScreeningPlan> screeningPlans) {
        Set<Integer> yearSet = new HashSet<>();
        screeningPlans.forEach(screeningPlan -> {
            Integer startYear = DateUtil.getYear(screeningPlan.getStartTime());
            Integer endYear = DateUtil.getYear(screeningPlan.getEndTime());
            yearSet.add(startYear);
            yearSet.add(endYear);
        });
        return new ArrayList<>(yearSet).stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }

    /**
     * 获取已经发布的通知
     *
     * @param planId
     * @return
     */
    public ScreeningPlan getReleasedPlanById(Integer planId) {
        ScreeningPlan screeningPlan = getById(planId);
        if (screeningPlan == null) {
            throw new BusinessException("无法找到该计划");
        }
        if (!screeningPlan.getReleaseStatus().equals(CommonConst.STATUS_RELEASE)) {
            throw new BusinessException("该计划未发布");
        }
        return screeningPlan;
    }

    /**
     * 通过Ids获取
     *
     * @param ids 筛查计划Id
     * @return List<ScreeningPlan>
     */
    public List<ScreeningPlan> getByIds(Collection<Integer> ids) {
        return baseMapper.selectBatchIds(ids);
    }

    /**
     * 通过Ids获取
     *
     * @param ids 筛查计划Id
     * @return List<ScreeningPlan>
     */
    public List<ScreeningPlan> getByIdsOrderByStartTime(Collection<Integer> ids) {
        LambdaQueryWrapper<ScreeningPlan> screeningPlanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        screeningPlanLambdaQueryWrapper.in(ScreeningPlan::getId, ids)
                .orderByAsc(ScreeningPlan::getStartTime);
        return baseMapper.selectList(screeningPlanLambdaQueryWrapper);
    }

    /**
     * 通过政府机构Id获取
     *
     * @param taskId 任务Id
     *
     * @return ScreeningPlan
     */
    public List<ScreeningPlan> getByTaskId(Integer taskId) {
        return list(new LambdaQueryWrapper<ScreeningPlan>().eq(ScreeningPlan::getScreeningTaskId, taskId));
    }

    /**
     * 通过任务ID和状态获取筛查计划
     * @param taskId
     * @param releaseStatus
     */
    public List<ScreeningPlan> getByTaskId(Integer taskId,Integer releaseStatus) {
        return baseMapper.selectList(Wrappers.lambdaQuery(ScreeningPlan.class)
                .eq(ScreeningPlan::getScreeningTaskId,taskId)
                .eq(ScreeningPlan::getReleaseStatus,releaseStatus));
    }


    /**
     * 学校自主筛查创建/编辑筛查计划
     * @param screeningPlan 筛查计划
     * @param screeningPlanSchool 筛查计划学校
     * @param twoTuple 筛查计划学校学生
     */
    @Transactional(rollbackFor = Exception.class)
    public void savePlanInfo(ScreeningPlan screeningPlan, ScreeningPlanSchool screeningPlanSchool, TwoTuple<List<ScreeningPlanSchoolStudent>, List<Integer>> twoTuple) {
        boolean saveOrUpdate = saveOrUpdate(screeningPlan);
        if (Objects.equals(Boolean.TRUE,saveOrUpdate)){
            if (Objects.nonNull(screeningPlanSchool)){
                screeningPlanSchool.setScreeningPlanId(screeningPlan.getId());
                screeningPlanSchoolService.saveOrUpdate(screeningPlanSchool);
            }
            screeningPlanSchoolStudentService.addScreeningStudent(twoTuple,screeningPlan.getId());
        }
    }

    /**
     * 检查筛查时间段
     * @param screeningPlan 筛查计划
     */
    public Boolean checkTimeLegal(ScreeningPlan screeningPlan) {
        LambdaQueryWrapper<ScreeningPlan> queryWrapper = Wrappers.lambdaQuery(ScreeningPlan.class)
                .eq(ScreeningPlan::getScreeningType, screeningPlan.getScreeningType())
                .eq(ScreeningPlan::getScreeningOrgId, screeningPlan.getScreeningOrgId())
                .eq(ScreeningPlan::getScreeningOrgType, ScreeningOrgTypeEnum.SCHOOL.getType())
                .eq(ScreeningPlan::getReleaseStatus, CommonConst.STATUS_RELEASE)
                .le(ScreeningPlan::getStartTime, screeningPlan.getEndTime())
                .ge(ScreeningPlan::getEndTime, screeningPlan.getStartTime());

        return CollUtil.isNotEmpty(baseMapper.selectList(queryWrapper));
    }

    /**
     * 检查筛查标题
     * @param screeningPlan 筛查计划
     */
    public Boolean checkTitleExist(ScreeningPlan screeningPlan) {
        LambdaQueryWrapper<ScreeningPlan> queryWrapper = Wrappers.lambdaQuery(ScreeningPlan.class)
                .eq(ScreeningPlan::getTitle, screeningPlan.getTitle())
                .eq(ScreeningPlan::getScreeningOrgId, screeningPlan.getScreeningOrgId())
                .eq(ScreeningPlan::getScreeningOrgType, ScreeningOrgTypeEnum.SCHOOL.getType())
                .eq(ScreeningPlan::getReleaseStatus, CommonConst.STATUS_RELEASE);

        return CollUtil.isNotEmpty(baseMapper.selectList(queryWrapper));
    }
}
