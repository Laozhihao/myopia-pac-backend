package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.constant.PermissionTemplateType;
import com.wupol.myopia.base.constant.RoleType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.domain.dto.RoleDTO;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.service.DistrictService;
import com.wupol.myopia.business.management.service.GovDeptService;
import com.wupol.myopia.business.management.validator.RoleAddValidatorGroup;
import com.wupol.myopia.business.management.validator.RoleUpdateValidatorGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

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
    private OauthService oauthService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private GovDeptService govDeptService;

    /**
     * 获取指定部门的角色列表
     *
     * @param param 查询参数
     * @return java.lang.Object
     **/
    @GetMapping("/list")
    public List<RoleDTO> getRoleListOfSpecifiedOrg(RoleDTO param) {
        // 非平台管理员只能查看自己部门下的角色
        if (!CurrentUserUtil.getCurrentUser().isPlatformAdminUser()) {
            param.setOrgId(CurrentUserUtil.getCurrentUser().getOrgId());
        }
        return oauthService.getRoleList(param);
    }

    /**
     * 新增角色
     *
     * @param param 角色数据
     * @return java.lang.Object
     **/
    @PostMapping()
    public RoleDTO addRole(@Validated(value = RoleAddValidatorGroup.class) @RequestBody RoleDTO param) {
        // TODO: 同部门角色名称不能重复(通过唯一索引来拦截)
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (currentUser.isPlatformAdminUser()) {
            Assert.notNull(param.getRoleType(), "角色类型为空");
            if (!RoleType.SUPER_ADMIN.getType().equals(param.getRoleType())) {
                // 创建非平台角色
                Assert.notNull(param.getOrgId(), "所属部门ID为空");
            } else {
                // 创建平台角色
                param.setOrgId(currentUser.getOrgId());
            }
        } else {
            param.setOrgId(currentUser.getOrgId());
            param.setRoleType(RoleType.GOVERNMENT_DEPARTMENT.getType());
        }
        param.setSystemCode(currentUser.getSystemCode());
        return oauthService.addRole(param);
    }

    /**
     * 编辑角色
     *
     * @param param 角色数据
     * @return java.lang.Object
     **/
    @PutMapping()
    public RoleDTO updateRole(@Validated(value = RoleUpdateValidatorGroup.class) @RequestBody RoleDTO param) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (currentUser.isPlatformAdminUser()) {
            Assert.notNull(param.getRoleType(), "角色类型不能为空");
            if (!RoleType.SUPER_ADMIN.getType().equals(param.getRoleType())) {
                // 创建非平台角色
                Assert.notNull(param.getOrgId(), "所属部门ID不能为空");
            } else {
                // 创建平台角色
                param.setOrgId(currentUser.getOrgId());
            }
        } else {
            // TODO: 非平台管理员不能修改平台管理员为该部门创建的角色
            // 非平台管理员用户，只能修改自己部门的角色
            RoleDTO roleDTO = oauthService.getRoleById(param.getId());
            Assert.isTrue(Objects.nonNull(roleDTO) && roleDTO.getOrgId().equals(currentUser.getOrgId()), "非法操作，只能修改自己部门的角色");
            param.setOrgId(currentUser.getOrgId());
            param.setRoleType(RoleType.GOVERNMENT_DEPARTMENT.getType());
        }
        param.setSystemCode(currentUser.getSystemCode());
        return oauthService.updateRole(param);
    }

    /**
     * 更新角色状态
     *
     * @param roleId 角色ID
     * @param status 状态类型
     * @return com.wupol.myopia.business.management.domain.dto.RoleDTO
     **/
    @PutMapping("/{roleId}/{status}")
    public RoleDTO updateRoleStatus(@PathVariable @NotNull(message = "角色ID不能为空") Integer roleId, @PathVariable @NotNull(message = "角色状态不能为空") Integer status) {
        return oauthService.updateRole(new RoleDTO().setId(roleId).setStatus(status));
    }

    /**
     * 给角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限集
     * @return java.lang.Object
     **/
    @PostMapping("/permission/{roleId}")
    public Object assignRolePermission(@PathVariable("roleId") Integer roleId, @RequestBody List<Integer> permissionIds) {
        // TODO: 校验权限是否都为对应模板内的
        return oauthService.assignRolePermission(roleId, permissionIds);
    }

    /**
     * 获取指定行政区下的角色权限树
     *
     * @param roleId 角色ID
     * @return java.lang.Object
     **/
    @GetMapping("/permission/structure/{roleId}")
    public Object getRolePermissionTree(@PathVariable("roleId") Integer roleId) {
        // 根据角色所属部门所在行政区拿获取权限模板类型
        RoleDTO roleDTO = oauthService.getRoleById(roleId);
        GovDept govDept = govDeptService.getById(roleDTO.getOrgId());
        District district = districtService.getById(govDept.getDistrictId());
        return oauthService.getRolePermissionTree(roleId, PermissionTemplateType.getTypeByDistrictCode(district.getCode()));
    }

}
