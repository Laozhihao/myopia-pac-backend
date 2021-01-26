package com.wupol.myopia.oauth.controller;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.domain.model.Role;
import com.wupol.myopia.oauth.domain.model.RolePermission;
import com.wupol.myopia.oauth.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/oauth/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取角色列表
     *
     * @param param 查询参数
     * @return java.lang.Object
     **/
    @GetMapping("/list")
    public List<Role> getRoleList(Role param) {
        return roleService.selectRoleList(param);
    }

    @PostMapping()
    public Role addRole(@RequestBody Role param) {
        roleService.save(param);
        return param;
    }

    @PutMapping()
    public Role updateRole(@RequestBody Role param) {
        if (!roleService.updateById(param)) {
            throw new BusinessException("修改角色失败");
        }
        return param;
    }

    @PostMapping("/permission/{roleId}")
    public List<RolePermission> assignRolePermission(@PathVariable("roleId") Integer roleId, @RequestBody List<Integer> permissionId) {
        return roleService.assignRolePermission(roleId, permissionId);
    }

    /**
     * 获取指定行政区下的角色权限树
     *
     * @param roleId        角色ID
     * @param templateType  模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    @GetMapping("/permission/structure/{roleId}/{templateType}")
    public List<Permission> getRolePermissionTree(@PathVariable("roleId") Integer roleId, @PathVariable("templateType") Integer templateType) {
        return roleService.getRolePermissionTree(roleId, templateType);
    }

    /**
     * 根据角色ID获取角色明细
     *
     * @param roleId 角色ID
     * @return com.wupol.myopia.oauth.domain.model.Role
     **/
    @GetMapping("/{roleId}")
    public Role getRoleById(@PathVariable Integer roleId) {
        return roleService.getById(roleId);
    }
}
