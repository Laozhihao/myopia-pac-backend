package com.wupol.myopia.business.management.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.GovDeptMapper;
import com.wupol.myopia.business.management.domain.model.GovDept;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public List<GovDept> selectGovDeptTreeByPid(Integer pid) {
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
}
