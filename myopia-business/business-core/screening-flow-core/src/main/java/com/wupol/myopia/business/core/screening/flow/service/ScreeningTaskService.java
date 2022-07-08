package com.wupol.myopia.business.core.screening.flow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskPageDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.ScreeningTaskMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTaskOrg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ScreeningTaskService extends BaseService<ScreeningTaskMapper, ScreeningTask> {

    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Autowired
    private ScreeningTaskOrgService screeningTaskOrgService;


    /**
     * 设置操作人再更新
     *
     * @param entity
     * @param userId
     * @return
     */
    public boolean updateById(ScreeningTask entity, Integer userId) {
        entity.setOperateTime(new Date()).setOperatorId(userId);
        return updateById(entity);
    }

    public IPage<ScreeningTaskPageDTO> selectPageByQuery(Page<ScreeningTask> page, ScreeningTaskQueryDTO query) {
        return baseMapper.selectPageByQuery(page, query);
    }

    /**
     * 删除，并删除关联的机构信息
     * 需修改通知状态为已读
     *
     * @param screeningTaskId
     * @return
     */
    public void removeWithOrgs(Integer screeningTaskId, CurrentUser user) {
        // 1. 修改通知状态为已读
        ScreeningTask screeningTask = getById(screeningTaskId);
        screeningNoticeDeptOrgService.read(screeningTask.getScreeningNoticeId(), screeningTask.getGovDeptId(), user);
        // 2. 删除任务关联的筛查机构信息
        screeningTaskOrgService.deleteByTaskIdAndExcludeOrgIds(screeningTaskId, Collections.emptyList());
        // 3. 删除任务
        removeById(screeningTaskId);
    }

    /**
     * 校验部门是否已创建
     *
     * @param screeningNoticeId
     * @param govDeptId
     */
    public boolean checkIsCreated(Integer screeningNoticeId, Integer govDeptId) {
        return baseMapper.countByNoticeIdAndGovId(screeningNoticeId, govDeptId) > 0;
    }

    /**
     * 获取筛查任务
     *
     * @param districtIdList 行政区域ids
     * @param notificationId 筛查通知id
     */
    public List<ScreeningTask> getScreeningTaskByDistrictIdAndNotificationId(List<Integer> districtIdList, Integer notificationId) {
        return baseMapper.getScreeningTaskByDistrictIdAndNotificationId(districtIdList, notificationId);
    }

    /**
     * 根据机构id获取筛查任务
     *
     * @param orgId 机构id
     */
    public List<ScreeningTask> getScreeningTaskByOrgId(Integer orgId) {
        List<ScreeningTaskOrg> taskOrgs = screeningTaskOrgService.getOrgListsByOrgId(orgId);
        return baseMapper.selectList(new LambdaQueryWrapper<ScreeningTask>().in(!CollectionUtils.isEmpty(taskOrgs), ScreeningTask::getId, taskOrgs.stream().map(ScreeningTaskOrg::getScreeningTaskId).collect(Collectors.toList())));
    }
}
