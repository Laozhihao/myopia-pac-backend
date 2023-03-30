package com.wupol.myopia.business.aggregation.screening.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.dto.LinkNoticeQueue;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.PlanLinkNoticeRequestDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        List<ScreeningNotice> sourceNotices = screeningNoticeService.getByIds(tasks.stream().map(ScreeningTask::getScreeningNoticeId).collect(Collectors.toList()));
        Map<Integer, Integer> sourceNoticeMap = sourceNotices.stream().collect(Collectors.toMap(ScreeningNotice::getId, ScreeningNotice::getGovDeptId));

        // 获取行政部门
        List<GovDept> govDeptList = govDeptService.getByIds(Lists.newArrayList(sourceNotices.stream().map(ScreeningNotice::getGovDeptId).collect(Collectors.toList())));
        Map<Integer, String> govDeptMap = govDeptList.stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
        notices.forEach(notice -> notice.setGovDeptName(govDeptMap.get(sourceNoticeMap.get(taskMap.get(notice.getScreeningTaskId())))));
        return notices;
    }


    /**
     * 关联通知
     *
     * @param requestDTO requestDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void linkNotice(PlanLinkNoticeRequestDTO requestDTO) {
        Integer planId = requestDTO.getPlanId();
        Integer screeningNoticeDeptOrgId = requestDTO.getScreeningNoticeDeptOrgId();
        Integer screeningTaskId = requestDTO.getScreeningTaskId();

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

        Integer districtId = task.getDistrictId();

        Integer screeningNoticeId = task.getScreeningNoticeId();
        plan.setSrcScreeningNoticeId(screeningNoticeId)
                .setScreeningTaskId(screeningTaskId)
                .setDistrictId(districtId);
        screeningPlanService.updateById(plan);


        deptOrg.setOperationStatus(CommonConst.STATUS_NOTICE_CREATED).setScreeningTaskPlanId(planId);
        screeningNoticeDeptOrgService.updateById(deptOrg);

        // 处理学生逻辑放在Redis中
        String uniqueId = String.format(CommonConst.NOTICE_LINK_UNIQUE, screeningNoticeId, screeningTaskId, planId, districtId);
        List<Object> queueList = redisUtil.lGetAll(RedisConstant.NOTICE_LINK_LIST);

        // 判断是否重复请求
        if (!CollectionUtils.isEmpty(queueList)) {
            if (queueList.stream().map(s -> (LinkNoticeQueue) s).anyMatch(s -> StringUtils.equals(s.getUniqueId(), uniqueId))) {
                throw new BusinessException("重复点击");
            }
        }

        redisUtil.lSet(RedisConstant.NOTICE_LINK_LIST, new LinkNoticeQueue()
                .setUniqueId(uniqueId)
                .setScreeningNoticeId(screeningNoticeId)
                .setScreeningTaskId(screeningTaskId)
                .setPlanId(planId)
                .setDistrictId(districtId));
    }
}
