package com.wupol.myopia.oauth.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.model.Role;
import com.wupol.myopia.oauth.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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

    @GetMapping("/list")
    public Object getRoleList(Role param) throws IOException {
        return roleService.findByList(param);
    }

    @PostMapping()
    public Object addRole(@RequestBody Role param) {
        return roleService.save(param);
    }

    @PutMapping()
    public Object modifyRole(@RequestBody Role param) {
        return roleService.updateById(param);
    }

    @PostMapping("/permission/{roleId}")
    public Object assignRolePermission(@PathVariable("roleId") Integer roleId) {
        return roleService.assignRolePermission(roleId);
    }

    @GetMapping("/permission/structure/{roleId}")
    public Object getRolePermissionTree(@PathVariable("roleId") Integer roleId) {
        return roleService.getRolePermissionTree(roleId);
    }
}
