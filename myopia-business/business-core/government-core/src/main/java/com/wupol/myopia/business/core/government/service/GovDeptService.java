package com.wupol.myopia.business.core.government.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.government.domain.dto.GovDeptDTO;
import com.wupol.myopia.business.core.government.domain.dto.GovDistrictDTO;
import com.wupol.myopia.business.core.government.domain.mapper.GovDeptMapper;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
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
    public List<GovDeptDTO> selectGovDeptTreeByPid(Integer pid) {
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
        List<GovDept> govDeptLists = baseMapper.findByPidAndNeStatus(ids, CommonConst.STATUS_IS_DELETED);
        if (CollectionUtils.isNotEmpty(govDeptLists)) {
            List<Integer> govDeptIds = govDeptLists.stream().map(GovDept::getId).collect(Collectors.toList());
            resultIds.addAll(govDeptIds);
            getNextGov(resultIds, govDeptIds);
        }
        return resultIds;
    }

    /**
     * 所在部门的所有上级部门
     *
     * @param id 部门id
     * @return List<Integer>
     */
    public Set<Integer> getSuperiorGovIds(Integer id) {
        Set<Integer> resultIds = new HashSet<>();
        if (id == null) {
            return resultIds;
        }
        GovDept govDept = baseMapper.findByIdAndNeStatus(id, CommonConst.STATUS_IS_DELETED);
        if (govDept != null) {
            resultIds.add(govDept.getPid());
            resultIds.addAll(getSuperiorGovIds(govDept.getPid()));
        }
        return resultIds;
    }

    /**
     * 获取当前id下的所属部门（子孙）
     *
     * @param govOrgId 部门id
     * @return List<Integer>
     */
    public List<GovDept> getAllSubordinateWithDistrictId(Integer govOrgId) {
        return getNextGovWithDistrictId(new ArrayList<>(), Lists.newArrayList(govOrgId));
    }

    /**
     * 遍历获取部门id
     *
     * @param result 结果集合
     * @param ids    入参
     * @return List<Integer>
     */
    private List<GovDept> getNextGovWithDistrictId(List<GovDept> result, List<Integer> ids) {
        List<GovDept> govDeptLists = getUnDeletedByPid(ids);
        if (!govDeptLists.isEmpty()) {
            List<Integer> govDeptIds = govDeptLists.stream().map(GovDept::getId).collect(Collectors.toList());
            result.addAll(govDeptLists);
            getNextGovWithDistrictId(result, govDeptIds);
        }
        return result;
    }

    /**
     * 根据PID获取未删除的部门
     *
     * @param ids
     * @return
     */
    private List<GovDept> getUnDeletedByPid(List<Integer> ids) {
        return baseMapper.findByPidAndNeStatus(ids, CommonConst.STATUS_IS_DELETED);
    }

    /**
     * 通过ID获取实体
     *
     * @param id id
     * @return GovDept
     */
    public GovDept getGovDeptById(Integer id) {
        return baseMapper.selectById(id);
    }

    /**
     * 获取政府部门（带有行政区域）
     *
     * @param ids 部门ID集
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.GovDept>
     **/
    public List<GovDeptDTO> getGovDeptWithDistrictByIds(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return baseMapper.selectGovDeptWithDistrictByIds(ids);
    }

    /**
     * 获取政府部门（带有行政区域）
     *
     * @param govDeptIds 部门ID集
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.GovDept>
     **/
    public Map<Integer, GovDeptDTO> getGovDeptMapByIds(List<Integer> govDeptIds) {
        if (CollectionUtils.isEmpty(govDeptIds)) {
            return Collections.emptyMap();
        }
        List<GovDeptDTO> govDeptList = getGovDeptWithDistrictByIds(govDeptIds);
        return govDeptList.stream().collect(Collectors.toMap(GovDept::getId, Function.identity()));
    }

    /**
     * 获取部门列表
     *
     * @param govDept 查询参数
     * @return java.util.List<com.wupol.myopia.business.management.domain.vo.GovDeptDTO>
     **/
    public List<GovDept> getGovDeptList(GovDept govDept) {
        return baseMapper.selectGovDeptList(govDept);
    }

    /**
     * 根据ID列表获取部门
     *
     * @param govDeptIds
     * @return
     */
    public List<GovDept> getByIds(List<Integer> govDeptIds) {
        if (CollectionUtils.isEmpty(govDeptIds)) {
            return Collections.emptyList();
        }
        return baseMapper.selectBatchIds(govDeptIds);
    }

    /**
     * 获取所有的省级部门
     * @return
     */
    public List<GovDept> getProvinceGovDept(){
        LambdaQueryWrapper<GovDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GovDept::getPid,1);//省级部门
        queryWrapper.eq(GovDept::getStatus,0);//启用
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 获取所有的省级部门
     *
     * @return List<GovDistrictDTO>
     */
    public List<GovDistrictDTO> getProvinceGov() {
        return baseMapper.getByPid(Lists.newArrayList(1));
    }

    /**
     * 获取所有的市级部门
     *
     * @return List<GovDept>
     */
    public List<GovDistrictDTO> getCityGov() {
        List<GovDistrictDTO> provinceGov = getProvinceGov();
        return getGovList(provinceGov);
    }

    /**
     * 获取所有的区级部门
     *
     * @return List<GovDistrictDTO>
     */
    public List<GovDistrictDTO> getAreaGov() {
        List<GovDistrictDTO> cityGovDept = getCityGov();
        return getGovList(cityGovDept);
    }

    /**
     * 获取所有的镇级部门
     *
     * @return List<GovDistrictDTO>
     */
    public List<GovDistrictDTO> getTownGov() {
        List<GovDistrictDTO> areaGovDept = getAreaGov();
        return getGovList(areaGovDept);
    }

    /**
     * 获取政府部门
     *
     * @param govDeptList 政府部门
     * @return List<GovDept>
     */
    private List<GovDistrictDTO> getGovList(List<GovDistrictDTO> govDeptList) {
        if (CollectionUtils.isEmpty(govDeptList)) {
            return new ArrayList<>();
        }
        return baseMapper.getByPid(govDeptList.stream().map(GovDept::getId).collect(Collectors.toList()));
    }

    public String getNameById(Integer id) {
        GovDept govDept = getById(id);
        return Objects.nonNull(govDept) ? govDept.getName() : "";
    }

    public List<GovDistrictDTO> getAll() {
        return baseMapper.getAll();
    }

}
