package com.wupol.myopia.business.api.management.service;

import com.alibaba.excel.util.CollectionUtils;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskOrgDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTaskOrg;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationAdminService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2021/4/25 15:44
 */
@Service
public class ScreeningTaskOrgBizService {

    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Autowired
    private ScreeningTaskOrgService screeningTaskOrgService;
    @Autowired
    private ScreeningTaskService screeningTaskService;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;


    /**
     * 批量更新或新增筛查任务的机构信息（删除非列表中的筛查机构）
     * @param screeningTaskId
     * @param screeningOrgs
     */
    public void saveOrUpdateBatchWithDeleteExcludeOrgsByTaskId(CurrentUser user, Integer screeningTaskId, List<ScreeningTaskOrg> screeningOrgs) {
        // 删除掉已有的不存在的机构信息
        List<Integer> excludeOrgIds = CollectionUtils.isEmpty(screeningOrgs) ? Collections.emptyList() : screeningOrgs.stream().map(ScreeningTaskOrg::getScreeningOrgId).collect(Collectors.toList());
        screeningTaskOrgService.deleteByTaskIdAndExcludeOrgIds(screeningTaskId, excludeOrgIds);
        if (!CollectionUtils.isEmpty(screeningOrgs)) {
            saveOrUpdateBatchByTaskId(user, screeningTaskId, screeningOrgs, false);
        }
    }

    /**
     * 批量更新或新增筛查任务的机构信息
     * @param screeningTaskId
     * @param screeningOrgs
     */
    public void saveOrUpdateBatchByTaskId(CurrentUser user, Integer screeningTaskId, List<ScreeningTaskOrg> screeningOrgs, boolean needNotice) {
        // 1. 查出剩余的
        Map<Integer, Integer> orgIdMap = screeningTaskOrgService.getOrgListsByTaskId(screeningTaskId).stream().collect(Collectors.toMap(ScreeningTaskOrg::getScreeningOrgId, ScreeningTaskOrg::getId));
        // 2. 更新id，并批量新增或修改
        screeningOrgs.forEach(taskOrg -> taskOrg.setScreeningTaskId(screeningTaskId).setId(orgIdMap.getOrDefault(taskOrg.getScreeningOrgId(), null)));
        screeningTaskOrgService.saveOrUpdateBatch(screeningOrgs);
        if (needNotice) {
            ScreeningTask screeningTask = screeningTaskService.getById(screeningTaskId);
            ScreeningNotice screeningNotice = screeningNoticeService.getByScreeningTaskId(screeningTaskId);
            this.noticeBatch(user, screeningTask, screeningNotice, screeningOrgs);
        }
    }

    /**
     * 批量通知
     * @param user
     * @param screeningTask
     * @param screeningNotice
     * @return
     */
    public Boolean noticeBatchByScreeningTask(CurrentUser user, ScreeningTask screeningTask, ScreeningNotice screeningNotice) {
        List<ScreeningTaskOrg> orgLists = screeningTaskOrgService.getOrgListsByTaskId(screeningTask.getId());
        return noticeBatch(user, screeningTask, screeningNotice, orgLists);
    }

    /**
     * 批量通知（已通知的不重复通知）
     * @param user
     * @param screeningTask
     * @param screeningNotice
     * @param orgLists
     * @return
     */
    private Boolean noticeBatch(CurrentUser user, ScreeningTask screeningTask, ScreeningNotice screeningNotice, List<ScreeningTaskOrg> orgLists) {
        List<Integer> existAcceptOrgIds = screeningNoticeDeptOrgService.getByScreeningNoticeId(screeningNotice.getId()).stream().map(ScreeningNoticeDeptOrg::getAcceptOrgId).collect(Collectors.toList());
        List<ScreeningNoticeDeptOrg> screeningNoticeDeptOrgs = orgLists.stream().filter(org -> !existAcceptOrgIds.contains(org.getScreeningOrgId())).map(org -> new ScreeningNoticeDeptOrg().setScreeningNoticeId(screeningNotice.getId()).setDistrictId(screeningTask.getDistrictId()).setAcceptOrgId(org.getScreeningOrgId()).setOperatorId(user.getId())).collect(Collectors.toList());
        return screeningNoticeDeptOrgService.saveBatch(screeningNoticeDeptOrgs);
    }

    /**
     * 根据任务Id获取机构列表-带机构名称
     * @param screeningTaskId
     * @return
     */
    public List<ScreeningTaskOrgDTO> getOrgVoListsByTaskId(Integer screeningTaskId) {
        List<ScreeningTaskOrg> orgVoLists = screeningTaskOrgService.getOrgListsByTaskId(screeningTaskId);
        return orgVoLists.stream().map(orgVo -> {
            ScreeningTaskOrgDTO dto = new ScreeningTaskOrgDTO();
            BeanUtils.copyProperties(orgVo, dto);
            dto.setName(screeningOrganizationService.getNameById(orgVo.getScreeningOrgId()));
            return dto;
        }).collect(Collectors.toList());
    }

}
