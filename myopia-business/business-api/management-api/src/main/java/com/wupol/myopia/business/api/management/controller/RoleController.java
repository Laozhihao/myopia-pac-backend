package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.domain.dto.RoleQueryDTO;
import com.wupol.myopia.business.api.management.domain.vo.RoleVO;
import com.wupol.myopia.business.api.management.service.RoleService;
import com.wupol.myopia.business.api.management.validator.RoleAddValidatorGroup;
import com.wupol.myopia.business.api.management.validator.RoleUpdateValidatorGroup;
import com.wupol.myopia.oauth.sdk.domain.response.Permission;
import com.wupol.myopia.oauth.sdk.domain.response.Role;
import com.wupol.myopia.oauth.sdk.domain.response.RolePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
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
    private RoleService roleService;

    /**
     * 获取指定部门的角色列表 - 分页
     *
     * @param param 查询参数
     * @param current   当前页码
     * @param size      每页条数
     * @return java.lang.Object
     **/
    @GetMapping("/list")
    public IPage<RoleVO> getRoleListByPage(RoleQueryDTO param,
                                           @RequestParam(defaultValue = "1") Integer current,
                                           @RequestParam(defaultValue = "10") Integer size) {
        return roleService.getRoleListByPage(param, current, size, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 获取指定部门角色
     *
     * @param govDeptId 部门ID
     * @return java.util.List<com.wupol.myopia.business.management.domain.dto.RoleDTO>
     **/
    @GetMapping("/{govDeptId}")
    public List<Role> getGovDeptRole(@PathVariable("govDeptId") Integer govDeptId) {
        Assert.notNull(govDeptId, "部门ID不能为空");
        return roleService.getGovDeptRole(govDeptId, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 新增角色
     *
     * @param param 角色数据
     * @return java.lang.Object
     **/
    @PostMapping()
    public Role addRole(@Validated(value = RoleAddValidatorGroup.class) @RequestBody RoleQueryDTO param) {
        return roleService.addRole(param, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 编辑角色
     *
     * @param param 角色数据
     * @return java.lang.Object
     **/
    @PutMapping()
    public RoleVO updateRole(@Validated(value = RoleUpdateValidatorGroup.class) @RequestBody RoleQueryDTO param) {
        return roleService.updateRole(param, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 更新角色状态
     *
     * @param roleId 角色ID
     * @param status 状态类型
     * @return com.wupol.myopia.business.management.domain.dto.RoleDTO
     **/
    @PutMapping("/{roleId}/{status}")
    public Role updateRoleStatus(@PathVariable @NotNull(message = "角色ID不能为空") Integer roleId, @PathVariable @NotNull(message = "角色状态不能为空") Integer status) {
        return roleService.updateRoleStatus(roleId, status);
    }

    /**
     * 给角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限集
     * @return java.lang.Object
     **/
    @PostMapping("/permission/{roleId}")
    public List<RolePermission> assignRolePermission(@PathVariable("roleId") Integer roleId, @RequestBody List<Integer> permissionIds) {
        return roleService.assignRolePermission(roleId, permissionIds);
    }

    /**
     * 获取指定行政区下的角色权限树
     *
     * @param roleId 角色ID
     * @return java.lang.Object
     **/
    @GetMapping("/permission/structure/{roleId}")
    public List<Permission> getRolePermissionTree(@PathVariable("roleId") Integer roleId) {
        return roleService.getRolePermissionTree(roleId);
    }
}
