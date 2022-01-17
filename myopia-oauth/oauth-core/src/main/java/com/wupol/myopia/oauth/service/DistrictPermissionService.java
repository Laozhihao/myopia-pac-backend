package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.constant.PermissionTemplateType;
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
        List<Integer> permissionIds = rolePermissionDTO.getPermissionIds();
        // 更新角色权限
        updateRolePermission(rolePermissionDTO.getOrgIds(), templateType, permissionIds);
        // 更新模板权限
        remove(new DistrictPermission().setDistrictLevel(templateType));
        List<DistrictPermission> permissions = permissionIds.stream().distinct().map(x -> new DistrictPermission().setDistrictLevel(templateType).setPermissionId(x)).collect(Collectors.toList());
        return saveBatch(permissions);
    }

    /**
     * 更新角色权限
     *
     * @param orgIds 机构ID集
     * @param templateType 模板类型
     * @param permissionIds 权限ID集
     * @return void
     **/
    private void updateRolePermission(List<Integer> orgIds, Integer templateType, List<Integer> permissionIds) {
        PermissionTemplateType  districtPermission = PermissionTemplateType.getByType(templateType);
        Assert.notNull(districtPermission, "权限模板类型不能为空");
        // 为政府人员和筛查机构管理员\医院管理员的权限模板时，需要传入指定的机构ID
        if ((CollectionUtils.isEmpty(orgIds)) && (PermissionTemplateType.isGovTemplate(templateType)
                || PermissionTemplateType.isScreeningOrgTemplate(templateType) || PermissionTemplateType.isHospitalAdminTemplate(templateType))) {
            return ;
        }
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setOrgIds(orgIds)
                .setRoleType(districtPermission.getRoleType())
                .setSystemCode(districtPermission.getSystemCode());
        List<Role> roleList = roleService.getRoleList(roleDTO);
        roleList.forEach(r -> roleService.updateRolePermission(r.getId(), templateType, permissionIds));
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
