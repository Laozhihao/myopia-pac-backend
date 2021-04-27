package com.wupol.myopia.business.core.screening.flow.service;

import cn.hutool.core.lang.Assert;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskOrgDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.ScreeningTaskOrgMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTaskOrg;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
public class ScreeningTaskOrgService extends BaseService<ScreeningTaskOrgMapper, ScreeningTaskOrg> {

    /**
     * 通过筛查任务ID获取所有关联的筛查机构信息
     *
     * @param screeningTaskId 筛查任务ID
     * @return List<ScreeningTaskOrg>
     */
    public List<ScreeningTaskOrg> getOrgListsByTaskId(Integer screeningTaskId) {
        return baseMapper.getByTaskId(screeningTaskId);
    }

    /**
     * 判断筛查机构时间段是否已有发布的任务
     * 一个筛查机构在同一部门一个时间段内只能出现一次
     * @param orgId：机构ID
     * @param screeningTaskQuery：必须存在govDeptId、startCreateTime、endCreateTime。如果有要排除的任务可传id
     * @return
     */
    public List<ScreeningTaskOrgDTO> getHasTaskOrgVoInPeriod(Integer orgId, ScreeningTaskQueryDTO screeningTaskQuery) {
        return baseMapper.selectHasTaskInPeriod(orgId, screeningTaskQuery);
    }

    /**
     * 查询任务的筛查机构
     *
     * @param screeningTaskId 筛查任务ID
     * @param screeningOrgId  机构ID
     * @return ScreeningTaskOrg
     */
    public ScreeningTaskOrg getOne(Integer screeningTaskId, Integer screeningOrgId) {
        return baseMapper.getOneByTaskIdAndOrgId(screeningTaskId, screeningOrgId);
    }

    /**
     * 删除筛查任务中，除了指定筛查机构ID的其它筛查机构信息
     * @param screeningTaskId
     * @param excludeOrgIds
     */
    public void deleteByTaskIdAndExcludeOrgIds(Integer screeningTaskId, List<Integer> excludeOrgIds) {
        Assert.notNull(screeningTaskId);
        // TODO 待优化，需去除字符串数据库字段名
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
        ScreeningTaskQueryDTO taskQuery = new ScreeningTaskQueryDTO();
        taskQuery.setGovDeptId(govDeptId);
        taskQuery.setStartCreateTime(startTime).setEndCreateTime(endTime);
        return baseMapper.selectHasTaskInPeriod(null, taskQuery).stream().map(ScreeningTaskOrg::getScreeningOrgId).distinct().collect(Collectors.toList());
    }

}
