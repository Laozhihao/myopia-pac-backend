package com.wupol.myopia.oauth.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.domain.model.Role;
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
    public Object getRoleList(Role param) {
        return roleService.selectRoleList(param);
    }

    @PostMapping()
    public Object addRole(@RequestBody Role param) {
        return roleService.save(param);
    }

    @PutMapping()
    public Object modifyRole(@RequestBody Role param) {
        if (!roleService.updateById(param)) return false;
        return roleService.getById(param.getId());
    }

    @PostMapping("/permission/{roleId}")
    public Object assignRolePermission(@PathVariable("roleId") Integer roleId, @RequestBody List<Integer> permissionId) {
        return roleService.assignRolePermission(roleId, permissionId);
    }

    /**
     * 获取指定行政区下的角色权限树
     *
     * @param roleId        角色ID
     * @param districtLevel 行政区等级
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    @GetMapping("/permission/structure/{roleId}/{districtLevel}")
    public List<Permission> getRolePermissionTree(@PathVariable("roleId") Integer roleId, @PathVariable("districtLevel") Integer districtLevel) {
        return roleService.getRolePermissionTree(roleId, districtLevel);
    }
}
