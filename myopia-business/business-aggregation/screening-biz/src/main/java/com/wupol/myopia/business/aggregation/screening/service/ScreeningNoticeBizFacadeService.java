package com.wupol.myopia.business.aggregation.screening.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.dto.LinkNoticeQueue;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.PlanLinkNoticeRequestDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知
 *
 * @author Simple4H
 */
@Service
public class ScreeningNoticeBizFacadeService {

    @Resource
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Resource
    private ScreeningTaskService screeningTaskService;
    @Resource
    private ScreeningNoticeService screeningNoticeService;
    @Resource
    private GovDeptService govDeptService;
    @Resource
    private ScreeningPlanService screeningPlanService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private DistrictService districtService;
    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;
    @Resource
    private SchoolService schoolService;

    /**
     * 获取可关联的通知
     *
     * @param orgId 机构/学校Id
     * @param type  类型
     * @return List<ScreeningNoticeDTO>
     */
    public List<ScreeningNoticeDTO> getCanLinkNotice(Integer orgId, Integer type) {
        List<ScreeningNoticeDTO> notices = screeningNoticeDeptOrgService.getCanLinkNotice(orgId, type);
        if (CollectionUtils.isEmpty(notices)) {
            return notices;
        }

        // 通过TaskId查询最原始的通知
        List<ScreeningTask> tasks = screeningTaskService.listByIds(notices.stream().map(ScreeningNotice::getScreeningTaskId).collect(Collectors.toList()));
        Map<Integer, Integer> taskMap = tasks.stream().collect(Collectors.toMap(ScreeningTask::getId, ScreeningTask::getScreeningNoticeId));
        Map<Integer, Integer> taskGovMap = tasks.stream().collect(Collectors.toMap(ScreeningTask::getId, ScreeningTask::getGovDeptId));

        // 获取行政部门
        List<GovDept> govDeptList = govDeptService.getByIds(Lists.newArrayList(tasks.stream().map(ScreeningTask::getGovDeptId).collect(Collectors.toList())));
        Map<Integer, String> govDeptMap = govDeptList.stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
        notices.forEach(notice -> notice.setGovDeptName(govDeptMap.get(taskGovMap.get(taskMap.get(notice.getScreeningTaskId())))));
        return notices;
    }

    /**
     * 关联通知
     *
     * @param requestDTO requestDTO
     * @param userId     用户Id
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void linkNotice(PlanLinkNoticeRequestDTO requestDTO, Integer userId) {
        Integer planId = requestDTO.getPlanId();
        Integer screeningNoticeDeptOrgId = requestDTO.getScreeningNoticeDeptOrgId();
        Integer screeningTaskId = requestDTO.getScreeningTaskId();

        List<Object> queueList = redisUtil.lGetAll(RedisConstant.NOTICE_LINK_LIST);

        // 判断是否重复请求
        if (!CollectionUtils.isEmpty(queueList) && queueList.stream().map(s -> (LinkNoticeQueue) s).anyMatch(s -> Objects.equals(s.getPlanId(), planId))) {
            throw new BusinessException("计划已经被选中，请执行完毕后，再操作");
        }

        ScreeningPlan plan = screeningPlanService.getById(planId);
        if (Objects.isNull(plan)) {
            throw new BusinessException("通过计划Id查询不到计划" + planId);
        }

        ScreeningNoticeDeptOrg deptOrg = screeningNoticeDeptOrgService.getById(screeningNoticeDeptOrgId);
        if (Objects.isNull(deptOrg)) {
            throw new BusinessException("通知机构信息异常" + screeningNoticeDeptOrgId);
        }

        ScreeningTask task = screeningTaskService.getById(screeningTaskId);
        if (Objects.isNull(task)) {
            throw new BusinessException("任务信息异常" + screeningNoticeDeptOrgId);
        }

        Integer screeningNoticeId = task.getScreeningNoticeId();
        ScreeningNotice notice = screeningNoticeService.getById(screeningNoticeId);
        if (Objects.isNull(notice)) {
            throw new BusinessException("通知信息异常" + screeningNoticeId);
        }
        checkSchoolDistrict(planId, notice.getDistrictId());

        plan.setSrcScreeningNoticeId(screeningNoticeId)
                .setScreeningTaskId(screeningTaskId);
        screeningPlanService.updateById(plan);

        deptOrg.setOperationStatus(CommonConst.STATUS_NOTICE_CREATED).setScreeningTaskPlanId(planId);
        screeningNoticeDeptOrgService.updateById(deptOrg);

        // 处理学生逻辑放在Redis中
        String uniqueId = String.format(CommonConst.NOTICE_LINK_UNIQUE, screeningNoticeId, screeningTaskId, planId);

        redisUtil.lSet(RedisConstant.NOTICE_LINK_LIST, new LinkNoticeQueue()
                .setUniqueId(uniqueId)
                .setScreeningNoticeId(screeningNoticeId)
                .setScreeningTaskId(screeningTaskId)
                .setPlanId(planId)
                .setCreateUserId(userId));
    }

    /**
     * 判断筛查学校行政区域是否属于通知的行政区域
     *
     * @param planId           计划Id
     * @param noticeDistrictId 通知Id
     */
    public void checkSchoolDistrict(Integer planId, Integer noticeDistrictId) {
        Set<Integer> schoolIds = screeningPlanSchoolService.getSchoolIdsByPlanIds(Lists.newArrayList(planId));
        if (CollectionUtils.isEmpty(schoolIds)) {
            return;
        }
        List<Integer> districtIds = schoolService.getByIds(schoolIds).stream().map(School::getDistrictId).collect(Collectors.toList());
        List<Integer> provinceAllDistrictIds = districtService.getProvinceAllDistrictIds(noticeDistrictId);
        if (new HashSet<>(provinceAllDistrictIds).containsAll(districtIds)) {
            return;
        }
        throw new BusinessException("学校行政区域异常");
    }
}
