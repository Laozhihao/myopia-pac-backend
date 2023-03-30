package com.wupol.myopia.business.aggregation.screening.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
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
        notices.forEach(notice-> notice.setGovDeptName(govDeptMap.get(sourceNoticeMap.get(taskMap.get(notice.getScreeningTaskId())))));
        return notices;
    }
}
