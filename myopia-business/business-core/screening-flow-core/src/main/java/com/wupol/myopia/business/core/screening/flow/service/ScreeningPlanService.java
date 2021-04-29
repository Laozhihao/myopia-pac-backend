package com.wupol.myopia.business.core.screening.flow.service;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.ScreeningConstant;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.ScreeningPlanMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
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
    public IPage<ScreeningPlanResponseDTO> getListByIds(PageRequest pageRequest, List<Integer> ids) {
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

    public IPage<ScreeningPlanPageDTO> selectPageByQuery(Page<ScreeningPlan> page, ScreeningPlanQueryDTO query) {
        return baseMapper.selectPageByQuery(page, query);
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
     * 筛选年度的计划并组装VO
     *
     * @param screeningPlans
     * @param year
     * @return
     */
    public Set<ScreeningPlanNameDTO> getScreeningPlanNameDTOs(List<ScreeningPlan> screeningPlans, Integer year) {
        return screeningPlans.stream().filter(screeningPlan ->
                year.equals(DateUtil.getYear(screeningPlan.getStartTime())) || year.equals(DateUtil.getYear(screeningPlan.getEndTime()))
        ).map(screeningPlan -> {
            ScreeningPlanNameDTO screeningTaskNameVO = new ScreeningPlanNameDTO();
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
    public IPage<ScreeningOrgPlanResponseDTO> getPageByOrgId(PageRequest pageRequest, Integer orgId) {
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
