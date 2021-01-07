package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.domain.dto.RoleDTO;
import com.wupol.myopia.business.management.validator.RoleAddValidatorGroup;
import com.wupol.myopia.business.management.validator.RoleQueryValidatorGroup;
import com.wupol.myopia.business.management.validator.RoleUpdateValidatorGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 获取指定部门的角色列表
     *
     * @param param 查询参数
     * @return java.lang.Object
     **/
    @GetMapping("/list")
    public Object getRoleListOfSpecifiedOrg(@Validated(value = RoleQueryValidatorGroup.class) RoleDTO param) {
        return oauthServiceClient.getRoleList(param);
    }

    /**
     * 新增角色
     *
     * @param param 角色数据
     * @return java.lang.Object
     **/
    @PostMapping()
    public Object addRole(@Validated(value = RoleAddValidatorGroup.class) @RequestBody RoleDTO param) {
        // TODO: 同部门角色名称不能重复、非admin用户不能创建admin用户、orgID不能为空且有效（需为登录用户名下的部门）
        // TODO: 非管理员或者admin用户不能创建角色、管理员不能修改自己所属部门的管理员类型的角色
        return oauthServiceClient.addRole(param);
    }

    /**
     * 编辑角色
     *
     * @param param 角色数据
     * @return java.lang.Object
     **/
    @PutMapping()
    public Object updateRole(@Validated(value = RoleUpdateValidatorGroup.class) @RequestBody RoleDTO param) {
        // TODO: 非admin用户不能修改admin用户、管理员不能修改自己所属部门的管理员类型的角色
        return oauthServiceClient.updateRole(param);
    }

    @PostMapping("/permission/{roleId}")
    public Object assignRolePermission(@PathVariable("roleId") Integer roleId, @RequestParam("permissionId") List<Integer> permissionIds) {
        return oauthServiceClient.assignRolePermission(roleId, permissionIds);
    }

    /**
     * 获取指定行政区下的角色权限树
     *
     * @param roleId 角色ID
     * @return java.lang.Object
     **/
    @GetMapping("/permission/structure/{roleId}")
    public Object getRolePermissionTree(@PathVariable("roleId") Integer roleId) {
        // TODO: 根据角色所属的部门所在的行政区获取行政区等级
        return oauthServiceClient.getRolePermissionTree(roleId, 1);
    }

}
