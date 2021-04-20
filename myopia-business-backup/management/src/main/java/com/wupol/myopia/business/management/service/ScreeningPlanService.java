package com.wupol.myopia.business.management.service;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.constant.ScreeningConstant;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.*;
import com.wupol.myopia.business.management.domain.mapper.ScreeningPlanMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningPlanQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningPlanNameVO;
import com.wupol.myopia.business.management.domain.vo.ScreeningPlanVo;
import com.wupol.myopia.business.management.facade.ScreeningRelatedFacade;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private ScreeningRelatedFacade screeningRelatedFacade;

    /**
     * 通过ids获取
     *
     * @param pageRequest 分页入参
     * @param ids         ids
     * @return IPage<ScreeningPlanResponse>
     */
    public IPage<ScreeningPlanResponse> getListByIds(PageRequest pageRequest, List<Integer> ids) {
        return baseMapper.getPlanLists(pageRequest.toPage(), ids);
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

    /**
     * 分页查询
     *
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<ScreeningPlanVo> getPage(ScreeningPlanQuery query, PageRequest pageRequest) {
        Page<ScreeningPlan> page = (Page<ScreeningPlan>) pageRequest.toPage();
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
        IPage<ScreeningPlanVo> screeningPlanIPage = baseMapper.selectPageByQuery(page, query);
        List<Integer> userIds = screeningPlanIPage.getRecords().stream().map(ScreeningPlan::getCreateUserId).distinct().collect(Collectors.toList());
        Map<Integer, String> userIdNameMap = oauthServiceClient.getUserBatchByIds(userIds).getData().stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getRealName));
        screeningPlanIPage.getRecords().forEach(vo -> vo.setCreatorName(userIdNameMap.getOrDefault(vo.getCreateUserId(), "")));
        return screeningPlanIPage;
    }

    /**
     * 发布计划
     *
     * @param id
     * @return
     */
    public Boolean release(Integer id, CurrentUser user) {
        //1. 更新状态&发布时间
        ScreeningPlan screeningPlan = getById(id);
        screeningPlan.setReleaseStatus(CommonConst.STATUS_RELEASE).setReleaseTime(new Date());
        return updateById(screeningPlan, user.getId());
    }

    /**
     * 新增或更新
     *
     * @param screeningPlanDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateWithSchools(CurrentUser user, ScreeningPlanDTO screeningPlanDTO, Boolean needUpdateNoticeStatus) {
        // 新增或更新筛查计划信息
        screeningPlanDTO.setOperatorId(user.getId());
        if (!saveOrUpdate(screeningPlanDTO)) {
            throw new BusinessException("创建失败");
        }
        // 新增或更新筛查学校信息
        screeningPlanSchoolService.saveOrUpdateBatchWithDeleteExcludeSchoolsByPlanId(screeningPlanDTO.getId(), screeningPlanDTO.getScreeningOrgId(), screeningPlanDTO.getSchools());
        if (needUpdateNoticeStatus && Objects.nonNull(screeningPlanDTO.getScreeningTaskId())) {
            // 更新通知状态
            ScreeningNotice screeningNotice = screeningNoticeService.getByScreeningTaskId(screeningPlanDTO.getScreeningTaskId());
            if (Objects.isNull(screeningNotice)) {
                throw new BusinessException("找不到对应任务通知");
            }
            screeningNoticeDeptOrgService.statusReadAndCreate(screeningNotice.getId(), screeningPlanDTO.getScreeningOrgId(), screeningPlanDTO.getId(), user);
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
    public boolean checkIsCreated(Integer screeningTaskId, Integer screeningOrgId) {
        return baseMapper.countByTaskIdAndOrgId(screeningTaskId, screeningOrgId) > 0;
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
     * 查找用户在参与筛查通知（发布筛查通知，或者接收筛查通知）中，所有筛查计划
     *
     * @param noticeIds
     * @param user
     * @return
     */
    public List<ScreeningPlan> getScreeningPlanByNoticeIdsAndUser(Set<Integer> noticeIds, CurrentUser user) {
        LambdaQueryWrapper<ScreeningPlan> screeningPlanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (user.isScreeningUser()) {
            screeningPlanLambdaQueryWrapper.eq(ScreeningPlan::getScreeningOrgId, user.getOrgId());
        } else if (user.isGovDeptUser()) {
            List<Integer> allGovDeptIds = govDeptService.getAllSubordinate(user.getOrgId());
            allGovDeptIds.add(user.getOrgId());
            screeningPlanLambdaQueryWrapper.in(ScreeningPlan::getGovDeptId, allGovDeptIds);
        }
        screeningPlanLambdaQueryWrapper.in(ScreeningPlan::getSrcScreeningNoticeId, noticeIds).eq(ScreeningPlan::getReleaseStatus, CommonConst.STATUS_RELEASE);
        return baseMapper.selectList(screeningPlanLambdaQueryWrapper);
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
        return getScreeningPlanByNoticeIdsAndUser(noticeSet, user);
    }

    /**
     * 筛选年度的计划并组装VO
     *
     * @param screeningPlans
     * @param year
     * @return
     */
    public Set<ScreeningPlanNameVO> getScreeningPlanNameVOs(List<ScreeningPlan> screeningPlans, Integer year) {
        return screeningPlans.stream().filter(screeningPlan ->
                year.equals(DateUtil.getYear(screeningPlan.getStartTime())) || year.equals(DateUtil.getYear(screeningPlan.getEndTime()))
        ).map(screeningPlan -> {
            ScreeningPlanNameVO screeningTaskNameVO = new ScreeningPlanNameVO();
            screeningTaskNameVO.setPlanName(screeningPlan.getTitle()).setPlanId(screeningPlan.getId()).setScreeningStartTime(screeningPlan.getStartTime()).setScreeningEndTime(screeningPlan.getEndTime());
            return screeningTaskNameVO;
        }).collect(Collectors.toSet());
    }


    /**
     * 分页获取筛查计划
     *
     * @param pageRequest 分页请求
     * @param orgId       机构ID
     * @return IPage<ScreeningTaskResponse>
     */
    public IPage<ScreeningOrgPlanResponse> getPageByOrgId(PageRequest pageRequest, Integer orgId) {
        return baseMapper.getPageByOrgId(pageRequest.toPage(), orgId);
    }

    /**
     * 通过orgId获取计划
     *
     * @param orgId 机构ID
     * @return List<ScreeningPlan>
     */
    public List<ScreeningPlan> getByOrgId(Integer orgId) {
        return baseMapper.getByOrgId(orgId);
    }

    /**
     * 通过orgIds获取计划
     *
     * @param orgIds 机构Ids
     * @return List<ScreeningPlan>
     */
    public List<ScreeningPlan> getByOrgIds(List<Integer> orgIds) {
        return baseMapper.getByOrgIds(orgIds);
    }

    /**
     * 根据筛查计划ID获取原始的筛查通知ID列表
     *
     * @param screeningPlanIds
     * @return
     */
    public List<Integer> getSrcScreeningNoticeIdsByIds(List<Integer> screeningPlanIds) {
        if (CollectionUtils.isEmpty(screeningPlanIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ScreeningPlan> screeningPlanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        screeningPlanLambdaQueryWrapper.in(ScreeningPlan::getId, screeningPlanIds);
        List<ScreeningPlan> screeningPlans = baseMapper.selectList(screeningPlanLambdaQueryWrapper);
        return screeningPlans.stream().map(ScreeningPlan::getSrcScreeningNoticeId).distinct().collect(Collectors.toList());
    }

    /**
     *
     * @param screeningOrgId
     * @return
     */
    public List<Long> getScreeningSchoolIdByScreeningOrgId(Integer screeningOrgId) {
        List<ScreeningPlanSchool> screeningPlanSchools = screeningPlanSchoolService.getScreeningSchoolsByScreeningOrgId(screeningOrgId);
        return screeningPlanSchools.stream().map(screeningPlanSchool -> screeningPlanSchool.getSchoolId().longValue()).collect(Collectors.toList());
    }

    /**
     * 获取用户当前的计划
     *
     * @param deptId
     */
    public Set<Integer> getCurrentPlanIds(Integer deptId) {
        List<ScreeningPlanSchool> screeningPlanSchools = screeningPlanSchoolService.getScreeningSchoolsByScreeningOrgId(deptId);
        return screeningPlanSchools.stream().map(ScreeningPlanSchool::getScreeningPlanId).collect(Collectors.toSet());
    }

    /**
     * 获取用户当前的计划
     *
     * @param screeningOrgId
     * @param schoolId
     * @return
     */
    public ScreeningPlan getCurrentPlan(Integer screeningOrgId, Integer schoolId) {
        return baseMapper.selectScreeningPlanDetailByOrgIdAndSchoolId(schoolId, screeningOrgId, ScreeningConstant.SCREENING_RELEASE_STATUS, new Date());
    }

    /**
     * 根据通知获取学生
     * @param noticeId
     * @return
     */
    public long getAllPlanStudentNumByNoticeId(Integer noticeId) {
        List<ScreeningPlan> allPlans = this.getAllPlanByNoticeId(noticeId);
        Integer allPlanStudentNums = allPlans.stream().map(ScreeningPlan::getStudentNumbers).reduce(0, Integer::sum);
        return allPlanStudentNums.longValue();
    }

    /**
     * 获取所有的计划筛查学生数
     *
     * @param noticeId
     * @return
     */
    public List<ScreeningPlan> getAllPlanByNoticeId(Integer noticeId) {
        //TODO @jacob是要查询发布状态么？
        ScreeningPlan screeningPlan = new ScreeningPlan();
        screeningPlan.setSrcScreeningNoticeId(noticeId).setReleaseStatus(CommonConst.STATUS_RELEASE);
        LambdaQueryWrapper<ScreeningPlan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlan::getSrcScreeningNoticeId, noticeId);
        return baseMapper.selectList(queryWrapper);
    }
}