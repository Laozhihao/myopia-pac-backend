package com.wupol.myopia.business.management.service;

import cn.hutool.core.lang.Assert;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.ScreeningTaskOrgMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningTaskOrg;
import com.wupol.myopia.business.management.domain.vo.OrgScreeningCountVO;
import com.wupol.myopia.business.management.domain.query.ScreeningTaskQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningTaskOrgVo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
public class ScreeningTaskOrgService extends BaseService<ScreeningTaskOrgMapper, ScreeningTaskOrg> {

    /**
     * 通过筛查机构ID获取筛查任务关联
     *
     * @param orgId 筛查机构ID
     * @return 结果 筛查任务关联Lists
     */
    public List<ScreeningTaskOrg> getTaskOrgListsByOrgId(Integer orgId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningTaskOrg>().eq("screening_org_id", orgId));
    }

    /**
     * 通过机构ID统计通知任务
     * <p>可以多个一个机构ids，这样就不用全表查数据</p></p>
     *
     * @return List<OrgScreeningCountVO>
     */
    public List<OrgScreeningCountVO> countScreeningTime() {
        return baseMapper.countScreeningTimeByOrgId();
    }

    /**
     * 通过筛查任务ID获取所有关联的筛查机构信息
     *
     * @param screeningTaskId
     * @return
     */
    public List<ScreeningTaskOrg> getOrgListsByTaskId(Integer screeningTaskId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningTaskOrg>().eq("screening_task_id", screeningTaskId));
    }


    /**
     * 根据任务Id获取机构列表-带机构名称
     * @param screeningTaskId
     * @return
     */
    public List<ScreeningTaskOrgVo> getOrgVoListsByTaskId(Integer screeningTaskId) {
        return baseMapper.selectVoListByScreeningTaskId(screeningTaskId);
    }

    /**
     * 判断筛查机构时间段是否已有发布的任务
     * 一个筛查机构在同一部门一个时间段内只能出现一次
     * @param orgId：机构ID
     * @param screeningTaskQuery：必须存在govDeptId、startCreateTime、endCreateTime，已有的需有id
     * @return
     */
    public boolean checkHasTaskInPeriod(Integer orgId, ScreeningTaskQuery screeningTaskQuery) {
        return baseMapper.selectHasTaskInPeriod(orgId, screeningTaskQuery).size() > 0;
    }

    /**
     * 查询任务的筛查机构
     * @param screeningTaskId
     * @param screeningOrgId
     * @return
     */
    public ScreeningTaskOrg getOne(Integer screeningTaskId, Integer screeningOrgId) {
        return baseMapper.selectOne(new QueryWrapper<ScreeningTaskOrg>().eq("screening_task_id", screeningTaskId).eq("screening_org_id", screeningOrgId));
    }

    /**
     * 批量更新或新增筛查任务的机构信息（删除非列表中的筛查机构）
     * @param screeningTaskId
     * @param screeningOrgs
     */
    public void saveOrUpdateBatchWithDeleteExcludeOrgsByTaskId(Integer screeningTaskId, List<ScreeningTaskOrg> screeningOrgs) {
        // 删除掉已有的不存在的机构信息
        List<Integer> excludeOrgIds = CollectionUtils.isEmpty(screeningOrgs) ? Collections.EMPTY_LIST : screeningOrgs.stream().map(ScreeningTaskOrg::getScreeningOrgId).collect(Collectors.toList());
        deleteByTaskIdAndExcludeOrgIds(screeningTaskId, excludeOrgIds);
        if (!CollectionUtils.isEmpty(screeningOrgs)) {
            saveOrUpdateBatchByTaskId(screeningTaskId, screeningOrgs);

        }
    }

    /**
     * 批量更新或新增筛查任务的机构信息
     * @param screeningTaskId
     * @param screeningOrgs
     */
    public void saveOrUpdateBatchByTaskId(Integer screeningTaskId, List<ScreeningTaskOrg> screeningOrgs) {
        // 1. 查出剩余的
        Map<Integer, Integer> orgIdMap = getOrgListsByTaskId(screeningTaskId).stream().collect(Collectors.toMap(ScreeningTaskOrg::getScreeningOrgId, ScreeningTaskOrg::getId));
        // 2. 更新id，并批量新增或修改
        screeningOrgs.forEach(taskOrg -> taskOrg.setScreeningTaskId(screeningTaskId).setId(orgIdMap.getOrDefault(taskOrg.getScreeningOrgId(), null)));
        saveOrUpdateBatch(screeningOrgs);
    }

    /**
     * 删除筛查任务中，除了指定筛查机构ID的其它筛查机构信息
     * @param screeningTaskId
     * @param excludeOrgIds
     */
    public void deleteByTaskIdAndExcludeOrgIds(Integer screeningTaskId, List<Integer> excludeOrgIds) {
        Assert.notNull(screeningTaskId);
        QueryWrapper<ScreeningTaskOrg> query = new QueryWrapper<ScreeningTaskOrg>().eq("screening_task_id", screeningTaskId);
        if (!CollectionUtils.isEmpty(excludeOrgIds)) {
            query.notIn("screening_org_id", excludeOrgIds);
        }
        baseMapper.delete(query);
    }

    /**
     * 查询已有任务的筛查机构
     * @param govDeptId 部门ID
     * @param startTime 查询任务的起始时间
     * @param endTime 查询任务的结束时间
     * @return
     */
    public List<Integer> getHaveTaskOrgIds(Integer govDeptId, LocalDate startTime, LocalDate endTime) {
        ScreeningTaskQuery taskQuery = new ScreeningTaskQuery();
        taskQuery.setGovDeptId(govDeptId);
        taskQuery.setStartCreateTime(startTime).setEndCreateTime(endTime);
        return baseMapper.selectHasTaskInPeriod(null, taskQuery).stream().map(ScreeningTaskOrg::getScreeningOrgId).distinct().collect(Collectors.toList());
    }
}
