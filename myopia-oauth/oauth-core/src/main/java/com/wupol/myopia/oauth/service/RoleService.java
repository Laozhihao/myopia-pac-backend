package com.wupol.myopia.oauth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.RoleType;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.domain.dto.RoleDTO;
import com.wupol.myopia.oauth.domain.mapper.RoleMapper;
import com.wupol.myopia.oauth.domain.model.*;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class RoleService extends BaseService<RoleMapper, Role> {

    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RolePermissionService rolePermissionService;
    @Autowired
    private DistrictPermissionService districtPermissionService;
    @Autowired
    private OrganizationService organizationService;

    /**
     * 获取角色列表
     *
     * @param query 查询参数
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    public List<Role> getRoleList(RoleDTO query) {
        Assert.notNull(query.getSystemCode(), "系统编码不能为空");
        return baseMapper.selectRoleList(query);
    }

    /**
     * 获取角色列表 - 分页
     *
     * @param query 查询参数
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    public IPage<Role> getRoleListByPage(RoleDTO query) {
        Assert.notNull(query.getCurrent(), "页码为空");
        Assert.notNull(query.getSize(), "页数为空");
        Page<Role> page = new Page<>(query.getCurrent(), query.getSize());
        return baseMapper.selectRoleList(page, query);
    }

    /**
     * 赋予新角色权限
     *
     * @param roleId        角色id
     * @param permissionIds 权限资源ID
     * @return 角色权限列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<RolePermission> assignRolePermission(Integer roleId, List<Integer> permissionIds) {
        rolePermissionService.remove(new RolePermission().setRoleId(roleId));
        List<RolePermission> rolePermission = permissionIds.stream().map(id -> new RolePermission().setRoleId(roleId).setPermissionId(id)).collect(Collectors.toList());
        rolePermissionService.saveBatch(rolePermission);
        return rolePermission;
    }

    /**
     * 获取指定行政区下的角色权限树
     *
     * @param roleId       角色ID
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    public List<Permission> getRolePermissionTree(Integer roleId, Integer templateType) {
        Role role = getById(roleId);
        Assert.notNull(role, "不存在该角色");
        if (RoleType.SUPER_ADMIN.getType().equals(role.getRoleType())) {
            return permissionService.getAdminRolePermissionTree(0, roleId);
        }
        return permissionService.selectRoleAllTree(0, roleId, templateType);
    }

    /**
     * 通过ID集批量获取角色
     *
     * @param ids 角色ID集
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    public List<Role> getByIds(List<Integer> ids) {
        return baseMapper.selectBatchIds(ids);
    }

    /**
     * 获取指定用户的角色(全部)
     *
     * @param userId 用户ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    public List<Role> getRoleListByUserId(Integer userId) {
        return baseMapper.selectRoleListByUserId(userId);
    }

    /**
     * 获取指定用户的角色(可用的)
     *
     * @param userId 用户ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    public List<Role> getUsableRoleByUserId(Integer userId, Integer systemCode, Integer userType) {
        List<Role> roleList = getRoleListByUserId(userId);
        if (CollectionUtils.isEmpty(roleList)) {
            return roleList;
        }
        List<Role> roles = roleList.stream().filter(x -> x.getStatus() == 0).collect(Collectors.toList());
        // 医院信息，如若拥有筛查机构角色，需去除掉机构状态已停用的角色
        if (SystemCode.MANAGEMENT_CLIENT.getCode().equals(systemCode) && UserType.HOSPITAL_ADMIN.getType().equals(userType)) {
            // 只保存非筛查机构角色或筛查机构可用的角色
            roles = roles.stream().filter(role -> (!RoleType.SCREENING_ORGANIZATION.getType().equals(role.getRoleType()))
                    || organizationService.getOrgStatus(role.getOrgId(), SystemCode.MANAGEMENT_CLIENT.getCode(),
                    UserType.SCREENING_ORGANIZATION_ADMIN.getType())).collect(Collectors.toList());
        }
        return roles;
    }

    /**
     * 获取用户ID列表
     *
     * @param query 查询条件
     * @return java.util.List<java.lang.Integer>
     **/
    public List<Integer> getUserIdList(Role query) {
        List<Integer> userIds = baseMapper.selectUserIdList(query);
        if (CollectionUtils.isEmpty(userIds)) {
            return userIds;
        }
        return userIds.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 更新角色权限
     *
     * @param roleId        角色Id
     * @param templateType  类型
     * @param permissionIds 权限集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermission(Integer roleId, Integer templateType, List<Integer> permissionIds) {

        // 通过templateType获取原先的权限集合（只需要permissionId）
        List<Integer> originIds = districtPermissionService.getByTemplateType(templateType)
                .stream().map(DistrictPermission::getPermissionId).distinct().collect(Collectors.toList());

        // 取差集，新权限集合与原权限集合比较，结果便是新增的（List1(1,2,3)｜Lists2(3,4,5,6)=>(1,2)）
        List<Integer> addList = ListUtils.subtract(permissionIds, originIds);
        if (!CollectionUtils.isEmpty(addList)) {
            rolePermissionService.batchInsert(roleId, addList);
        }

        // 同理，原权限集合与新权限集合比较,结果便是删除的
        List<Integer> deletedLists = ListUtils.subtract(originIds, permissionIds);
        if (!CollectionUtils.isEmpty(deletedLists)) {
            rolePermissionService.batchDeleted(roleId, deletedLists);
        }
    }

    /**
     * 获取指定筛查机构的第一个角色
     *
     * @param screeningOrgId 筛查机构ID
     * @return com.wupol.myopia.oauth.domain.model.Role
     **/
    public Role getScreeningOrgFirstOneRole(Integer screeningOrgId) {
        Assert.notNull(screeningOrgId, "筛查机构ID不能为空");
        return getOrgFirstOneRole(screeningOrgId, SystemCode.MANAGEMENT_CLIENT.getCode(), RoleType.SCREENING_ORGANIZATION.getType());
    }

    /**
     * 获取指定机构的第一个角色
     *
     * @param orgId 筛查机构ID
     * @param systemCode 系统编号
     * @param roleType 角色类型
     * @return com.wupol.myopia.oauth.domain.model.Role
     **/
    public Role getOrgFirstOneRole(Integer orgId, Integer systemCode, Integer roleType) {
        Assert.notNull(orgId, "机构ID不能为空");
        Assert.notNull(systemCode, "系统编号systemCode不能为空");
        Assert.notNull(roleType, "角色类型roleType不能为空");
        return baseMapper.getOrgFirstOneRole(orgId, systemCode, roleType);
    }

    /**
     * 更新指定医院的角色权限
     * @param hospitalId
     * @param templateType
     */
    @Transactional
    public void updateRolePermissionByHospital(Integer hospitalId, Integer templateType) {
        RoleDTO query = new RoleDTO();
        query.setOrgId(hospitalId).setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode())
                .setRoleType(RoleType.HOSPITAL_ADMIN.getType());
        List<Role> roleList = getRoleList(query);
        roleList.forEach(role -> updateRolePermission(role.getId(), templateType));
    }

    /**
     * 更新角色的权限
     * @param roleId
     * @param templateType
     */
    @Transactional
    public void updateRolePermission(Integer roleId, Integer templateType) {
        // 删除改角色所有的所有权限
        rolePermissionService.remove(new RolePermission().setRoleId(roleId));
        // 查找模板的权限集合包
        List<Integer> permissionIds = districtPermissionService.getByTemplateType(templateType)
                .stream().map(DistrictPermission::getPermissionId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(permissionIds)) {
            this.assignRolePermission(roleId, permissionIds);
        }
    }

}
