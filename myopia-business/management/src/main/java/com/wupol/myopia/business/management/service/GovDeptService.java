package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.mapper.GovDeptMapper;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.vo.GovDeptVo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class GovDeptService extends BaseService<GovDeptMapper, GovDept> {

    /**
     * 获取指定部门及其下面的所有部门的数据树
     *
     * @param pid 部门ID
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.GovDept>
     **/
    public List<GovDeptVo> selectGovDeptTreeByPid(Integer pid) {
        if (Objects.isNull(pid)) {
            return new ArrayList<>();
        }
        return baseMapper.selectGovDeptTreeByPid(pid);
    }

    /**
     * 获取指定部门及其下面的所有部门的ID集合
     *
     * @return java.util.List<java.lang.Integer>
     **/
    public List<Integer> getAllSubordinateDepartmentIdByPid(Integer pid) {
        List<GovDept> govDeptTree = baseMapper.selectIdTreeByPid(pid);
        List<Integer> ids = treeListToSingleLayerList(govDeptTree);
        ids.add(pid);
        return ids;
    }

    /**
     * 把树结构list转为单层List
     *
     * @param idTree 部门ID树
     * @return java.util.List<java.lang.Integer>
     **/
    private List<Integer> treeListToSingleLayerList(List<GovDept> idTree) {
        ArrayList<Integer> ids = new ArrayList<>();
        if (CollectionUtils.isEmpty(idTree)) {
            return ids;
        }
        idTree.forEach(govDept -> {
            ids.add(govDept.getId());
            ids.addAll(treeListToSingleLayerList(govDept.getChild()));
        });
        return ids;
    }

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
