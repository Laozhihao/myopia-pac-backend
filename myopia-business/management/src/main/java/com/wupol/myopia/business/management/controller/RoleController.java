package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.domain.dto.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/role")
public class RoleController {

    @Autowired
    private OauthServiceClient oauthServiceClient;

    @GetMapping("/list")
    public Object getRoleList(Role param) {
        // TODO：联表查询获取创建人姓名
        return oauthServiceClient.getRoleList(param);
    }

    @PostMapping()
    public Object addRole(@RequestBody Role param) {
        return oauthServiceClient.addRole(param);
    }

    @PutMapping()
    public Object modifyRole(@RequestBody Role param) {
        return oauthServiceClient.modifyRole(param);
    }

    @PostMapping("/permission/{roleId}")
    public Object assignRolePermission(@PathVariable("roleId") Integer roleId) {
        return oauthServiceClient.assignRolePermission(roleId);
    }

    @GetMapping("/permission/structure/{roleId}")
    public Object getRolePermissionTree(@PathVariable("roleId") Integer roleId) {
        return oauthServiceClient.getRolePermissionTree(roleId);
    }

}
