package com.wupol.myopia.oauth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.dto.RoleDTO;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.domain.model.Role;
import com.wupol.myopia.oauth.domain.model.RolePermission;
import com.wupol.myopia.oauth.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    public List<Role> getRoleList(RoleDTO param) {
        return roleService.getRoleList(param);
    }

    /**
     * 获取角色列表 - 分页
     *
     * @param param 查询参数
     * @return java.lang.Object
     **/
    @GetMapping("/page")
    public IPage<Role> getRoleListByPage(RoleDTO param) {
        return roleService.getRoleListByPage(param);
    }

    /**
     * 新增角色
     *
     * @param param 角色参数
     * @return com.wupol.myopia.oauth.domain.model.Role
     **/
    @PostMapping()
    public Role addRole(@RequestBody Role param) {
        try {
            roleService.save(param);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("已经存在该角色名称");
        }
        return param;
    }

    /**
     * 编辑角色
     *
     * @param param 角色参数
     * @return com.wupol.myopia.oauth.domain.model.Role
     **/
    @PutMapping()
    public Role updateRole(@RequestBody Role param) {
        try {
            boolean success = roleService.updateById(param);
            Assert.isTrue(success, "修改角色失败");
        } catch (DuplicateKeyException e) {
            throw new BusinessException("已经存在该角色名称");
        }
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(param.getId());
        List<Role> roles = roleService.getRoleList(roleDTO);
        return roles.get(0);
    }

    /**
     * 给角色分配权限 TODO: 校验权限是否都为对应模板内的
     *
     * @param roleId 角色ID
     * @param permissionId 权限ID集合
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.RolePermission>
     **/
    @PostMapping("/permission/{roleId}")
    public List<RolePermission> assignRolePermission(@PathVariable("roleId") Integer roleId, @RequestBody List<Integer> permissionId) throws IOException {
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

    /**
     * 更新角色权限
     *
     * @param roleId       角色ID
     * @param permissionId 权限ID集合
     **/
    @PostMapping("/update/permission/{roleId}")
    public void updateRolePermission(@PathVariable("roleId") Integer roleId, @RequestBody List<Integer> permissionId) {
        roleService.updateRolePermission(roleId, permissionId);
    }
}
