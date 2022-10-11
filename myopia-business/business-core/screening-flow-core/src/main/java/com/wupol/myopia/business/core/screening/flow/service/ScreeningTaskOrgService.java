package com.wupol.myopia.business.core.screening.flow.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskOrgDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.ScreeningTaskOrgMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTaskOrg;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
     * 批量通过筛查任务ID获取所有关联的筛查机构信息
     *
     * @param screeningTaskIds 筛查任务ID集合
     * @return List<ScreeningTaskOrg>
     */
    public List<ScreeningTaskOrg> getOrgListsByTaskIds(List<Integer> screeningTaskIds) {
        return baseMapper.selectList(Wrappers.lambdaQuery(ScreeningTaskOrg.class)
                .in(ScreeningTaskOrg::getScreeningTaskId,screeningTaskIds));
    }

    /**
     * 通过机构id获得任务id
     *
     * @param orgId 机构id
     * @return List<ScreeningTaskOrg>
     */
    public List<ScreeningTaskOrg> getOrgListsByOrgId(Integer orgId) {
        return baseMapper.selectList(new LambdaQueryWrapper<ScreeningTaskOrg>().eq(ScreeningTaskOrg::getScreeningOrgId, orgId));
    }

    /**
     * 判断筛查机构时间段是否已有发布的任务
     * 一个筛查机构在同一部门一个时间段内只能出现一次
     * @param orgId：机构ID
     * @param screeningTaskQuery：必须存在govDeptId、startCreateTime、endCreateTime。如果有要排除的任务可传id
     * @return
     */
    public List<ScreeningTaskOrgDTO> getHasTaskOrgVoInPeriod(Integer orgId,Integer screeningOrgType, ScreeningTaskQueryDTO screeningTaskQuery) {
        return baseMapper.selectHasTaskInPeriod(orgId,screeningOrgType, screeningTaskQuery);
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
     */
    public void deleteByTaskIdAndExcludeOrgIds(Integer screeningTaskId, List<Integer> excludeOrgIds) {
        Assert.notNull(screeningTaskId);
        LambdaQueryWrapper<ScreeningTaskOrg> queryWrapper = Wrappers.lambdaQuery(ScreeningTaskOrg.class).eq(ScreeningTaskOrg::getScreeningTaskId, screeningTaskId);
        if (CollUtil.isNotEmpty(excludeOrgIds)) {
            queryWrapper.notIn(ScreeningTaskOrg::getScreeningOrgId, excludeOrgIds);
        }
        baseMapper.delete(queryWrapper);
    }

    /**
     * 删除筛查任务中，除了指定筛查机构ID的其它筛查机构信息
     * @param screeningTaskId
     * @param excludeTypeIdMap
     */
    public void deleteByTaskIdAndExcludeOrgIds(Integer screeningTaskId, Map<Integer,List<Integer>> excludeTypeIdMap) {
        Assert.notNull(screeningTaskId);

        List<ScreeningTaskOrg> screeningTaskOrgList = baseMapper.getByTaskId(screeningTaskId);

        if (CollUtil.isNotEmpty(excludeTypeIdMap)) {
            List<Integer> deleteIds=Lists.newArrayList();
            List<Integer> orgIds = excludeTypeIdMap.getOrDefault(ScreeningOrgTypeEnum.ORG.getType(), Lists.newArrayList());
            List<Integer> schoolIds = excludeTypeIdMap.getOrDefault(ScreeningOrgTypeEnum.SCHOOL.getType(), Lists.newArrayList());
            if (CollUtil.isNotEmpty(orgIds)){
                List<Integer> deleteTaskOrgIds = screeningTaskOrgList.stream()
                        .filter(screeningTaskOrg -> Objects.equals(screeningTaskOrg.getScreeningOrgType(), ScreeningOrgTypeEnum.ORG.getType()))
                        .filter(screeningTaskOrg -> !orgIds.contains(screeningTaskOrg.getScreeningOrgId()))
                        .map(ScreeningTaskOrg::getId)
                        .collect(Collectors.toList());
                deleteIds.addAll(deleteTaskOrgIds);
            }else {
                Set<Integer> deleteTaskOrgIds = screeningTaskOrgList.stream()
                        .filter(screeningTaskOrg -> Objects.equals(screeningTaskOrg.getScreeningOrgType(), ScreeningOrgTypeEnum.ORG.getType()))
                        .map(ScreeningTaskOrg::getId)
                        .collect(Collectors.toSet());
                deleteIds.addAll(deleteTaskOrgIds);
            }
            if (CollUtil.isNotEmpty(schoolIds)){
                List<Integer> deleteTaskOrgIds = screeningTaskOrgList.stream()
                        .filter(screeningTaskOrg -> Objects.equals(screeningTaskOrg.getScreeningOrgType(), ScreeningOrgTypeEnum.SCHOOL.getType()))
                        .filter(screeningTaskOrg -> !schoolIds.contains(screeningTaskOrg.getScreeningOrgId()))
                        .map(ScreeningTaskOrg::getId)
                        .collect(Collectors.toList());
                deleteIds.addAll(deleteTaskOrgIds);
            }else {
                Set<Integer> deleteTaskOrgIds = screeningTaskOrgList.stream()
                        .filter(screeningTaskOrg -> Objects.equals(screeningTaskOrg.getScreeningOrgType(), ScreeningOrgTypeEnum.SCHOOL.getType()))
                        .map(ScreeningTaskOrg::getId)
                        .collect(Collectors.toSet());
                deleteIds.addAll(deleteTaskOrgIds);
            }
            if (CollUtil.isNotEmpty(deleteIds)){
                baseMapper.deleteBatchIds(deleteIds);
            }
        }
    }

    /**
     * 查询已有任务的筛查机构
     * @param govDeptId 部门ID
     * @param startTime 查询任务的起始时间
     * @param endTime 查询任务的结束时间
     * @return
     */
    public List<ScreeningTaskOrgDTO> getHaveTaskOrgIds(Integer govDeptId, LocalDate startTime, LocalDate endTime) {
        ScreeningTaskQueryDTO taskQuery = new ScreeningTaskQueryDTO();
        taskQuery.setGovDeptId(govDeptId);
        taskQuery.setStartCreateTime(startTime).setEndCreateTime(endTime);
        return baseMapper.selectHasTaskInPeriod(null,null, taskQuery);
    }
}
