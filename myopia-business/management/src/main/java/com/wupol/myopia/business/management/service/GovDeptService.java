package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.mapper.GovDeptMapper;
import com.wupol.myopia.business.management.domain.model.GovDept;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class GovDeptService extends BaseService<GovDeptMapper, GovDept> {

    /**
     * 获取当前id下的所属部门id
     *
     * @param govOrgId 部门id
     * @return List<Integer>
     */
    public List<Integer> getAllSubordinate(Integer govOrgId) {
        return getNextGov(Lists.newArrayList(govOrgId), Lists.newArrayList(govOrgId));
    }

    /**
     * 遍历获取部门id
     *
     * @param resultIds 结果集合
     * @param ids       入参
     * @return List<Integer>
     */
    private List<Integer> getNextGov(List<Integer> resultIds, List<Integer> ids) {
        QueryWrapper<GovDept> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("pid", ids).ne("status", Const.STATUS_IS_DELETED);
        List<GovDept> govDeptLists = baseMapper.selectList(queryWrapper);
        if (!govDeptLists.isEmpty()) {
            List<Integer> govDeptIds = govDeptLists.stream().map(GovDept::getId).collect(Collectors.toList());
            resultIds.addAll(govDeptIds);
            getNextGov(resultIds, govDeptIds);
        }
        return resultIds;
    }
}
