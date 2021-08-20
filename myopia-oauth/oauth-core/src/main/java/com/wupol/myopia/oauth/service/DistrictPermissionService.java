package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.constant.PermissionTemplateType;
import com.wupol.myopia.base.constant.RoleType;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.domain.dto.RoleDTO;
import com.wupol.myopia.oauth.domain.dto.RolePermissionDTO;
import com.wupol.myopia.oauth.domain.mapper.DistrictPermissionMapper;
import com.wupol.myopia.oauth.domain.model.DistrictPermission;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.domain.model.Role;
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
public class DistrictPermissionService extends BaseService<DistrictPermissionMapper, DistrictPermission> {

    @Autowired
    private RoleService roleService;

    /**
     * 根据模板类型获取模板权限-树结构
     *
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    public List<Permission> selectTemplatePermissionTree(Integer templateType) {
        Assert.notNull(templateType, "模板类型不能为空");
        return baseMapper.selectTemplatePermissionTree(0, templateType);
    }

    /**
     * 更新模板权限
     *
     * @param templateType      模板类型
     * @param rolePermissionDTO 角色权限
     * @return boolean
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermissionTemplate(Integer templateType, RolePermissionDTO rolePermissionDTO) {
        List<Integer> govIds = rolePermissionDTO.getGovIds();
        List<Integer> permissionIds = rolePermissionDTO.getPermissionIds();
        List<Integer> orgIds = rolePermissionDTO.getOrgIds();

        // 政府部门
        if (PermissionTemplateType.isGovUser(templateType)) {
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setOrgIds(govIds);
            // 通过部门Id获取角色
            List<Role> roleList = roleService.getRoleList(roleDTO);
            roleList.forEach(r -> roleService.updateRolePermission(r.getId(), templateType, permissionIds));
        }

        // 平台管理员
        if (PermissionTemplateType.PLATFORM_ADMIN.getType().equals(templateType)) {
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setRoleType(RoleType.PLATFORM_ADMIN.getType());
            // 平台管理员通过RoleType来获取角色列表
            List<Role> roleList = roleService.getRoleList(roleDTO);
            roleList.forEach(r -> roleService.updateRolePermission(r.getId(), PermissionTemplateType.PLATFORM_ADMIN.getType(), permissionIds));
        }

        // 筛查机构-配置
        if (PermissionTemplateType.isSpecialScreening(templateType) && !CollectionUtils.isEmpty(orgIds)) {
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setOrgIds(orgIds);
            roleDTO.setSystemCode(SystemCode.SCREENING_MANAGEMENT_CLIENT.getCode());
            // 通过部门Id获取角色
            List<Role> roleList = roleService.getRoleList(roleDTO);
            roleList.forEach(r -> roleService.updateRolePermission(r.getId(), templateType, permissionIds));
        }
        remove(new DistrictPermission().setDistrictLevel(templateType));
        List<DistrictPermission> permissions = permissionIds.stream().distinct().map(x -> new DistrictPermission().setDistrictLevel(templateType).setPermissionId(x)).collect(Collectors.toList());
        return saveBatch(permissions);
    }

    /**
     * 通过类型获取模版权限集合
     *
     * @param templateType 类型
     * @return List<DistrictPermission>
     */
    public List<DistrictPermission> getByTemplateType(Integer templateType) {
        return baseMapper.getByTemplateType(templateType);
    }
}
