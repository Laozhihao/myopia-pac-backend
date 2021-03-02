package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.constant.PermissionTemplateType;
import com.wupol.myopia.base.constant.RoleType;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.domain.dto.RoleDTO;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.vo.GovDeptVo;
import com.wupol.myopia.business.management.service.DistrictService;
import com.wupol.myopia.business.management.service.GovDeptService;
import com.wupol.myopia.business.management.validator.RoleAddValidatorGroup;
import com.wupol.myopia.business.management.validator.RoleUpdateValidatorGroup;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

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
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (!currentUser.isPlatformAdminUser()) {
            // 非平台管理员只能查看自己部门下的角色
            param.setOrgId(CurrentUserUtil.getCurrentUser().getOrgId());
        } else if (Objects.nonNull(param.getDistrictId()) || !StringUtils.isEmpty(param.getOrgName())) {
            // 平台管理员才支持根据行政区域、部门名称、角色类型搜索
            List<GovDept> govDeptList = govDeptService.getGovDeptList(new GovDept().setDistrictId(param.getDistrictId()).setName(param.getOrgName()));
            if (CollectionUtils.isEmpty(govDeptList)) {
                return Collections.emptyList();
            }
            param.setOrgIds(govDeptList.stream().map(GovDept::getId).collect(Collectors.toList()));
        }
        List<RoleDTO> roleList = oauthService.getRoleList(param.setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode()));
        if (CollectionUtils.isEmpty(roleList)) {
            return roleList;
        }
        // 获取部门信息和行政区信息
        List<Integer> govDeptIds = roleList.stream().map(RoleDTO::getOrgId).distinct().collect(Collectors.toList());
        Map<Integer, GovDeptVo> govDeptMap = govDeptService.getGovDeptMapByIds(govDeptIds);
        roleList.forEach(role -> {
            GovDeptVo govDeptVo = govDeptMap.get(role.getOrgId());
            if (Objects.isNull(govDeptVo)) {
                return;
            }
            role.setOrgName(govDeptVo.getName());
            role.setDistrictDetail(districtService.getDistrictPositionDetail(govDeptVo.getDistrict()));
            role.setDistrictId(govDeptVo.getDistrictId());
        });
        return roleList;
    }

    /**
     * 新增角色
     *
     * @param param 角色数据
     * @return java.lang.Object
     **/
    @PostMapping()
    public RoleDTO addRole(@Validated(value = RoleAddValidatorGroup.class) @RequestBody RoleDTO param) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (currentUser.isPlatformAdminUser()) {
            Assert.notNull(param.getRoleType(), "角色类型为空");
            if (!RoleType.SUPER_ADMIN.getType().equals(param.getRoleType())) {
                // 创建非平台角色
                Assert.notNull(param.getOrgId(), "所属部门ID为空");
                // 非平台角色的部门不能为运营中心部门
                Assert.isTrue(!param.getOrgId().equals(currentUser.getOrgId()), "无效部门ID");
            } else {
                // 创建平台角色
                param.setOrgId(currentUser.getOrgId());
            }
        } else {
            param.setOrgId(currentUser.getOrgId());
            param.setRoleType(RoleType.GOVERNMENT_DEPARTMENT.getType());
        }
        param.setSystemCode(currentUser.getSystemCode()).setCreateUserId(currentUser.getId());
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
        RoleDTO role = oauthService.getRoleById(param.getId());
        Assert.notNull(role, "该角色不存在");
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        // 平台管理员用户，创建非平台角色
        if (currentUser.isPlatformAdminUser() && !RoleType.SUPER_ADMIN.getType().equals(role.getRoleType())) {
            Assert.notNull(param.getOrgId(), "所属部门ID不能为空");
            role.setOrgId(param.getOrgId());
            // 非平台角色的部门不能为运营中心部门
            Assert.isTrue(!param.getOrgId().equals(currentUser.getOrgId()), "无效部门ID");
        }
        // 非平台管理员用户，只能修改自己部门的角色
        Assert.isTrue(currentUser.isPlatformAdminUser() || role.getOrgId().equals(currentUser.getOrgId()), "非法操作，只能修改自己部门的角色");
        role.setStatus(param.getStatus()).setRemark(param.getRemark()).setChName(param.getChName());
        RoleDTO roleDTO = oauthService.updateRole(role);
        GovDept govDept = govDeptService.getById(roleDTO.getOrgId());
        District district = districtService.getById(govDept.getDistrictId());
        return roleDTO.setOrgName(govDept.getName()).setDistrictDetail(districtService.getDistrictPositionDetail(district));
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
        validatePermission(roleId);
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
        validatePermission(roleId);
        // 判断权限全来自模板
        List<Integer> permissionTemplateIdList = oauthService.getPermissionTemplateIdList(getPermissionTemplateTypeByRoleId(roleId));
        Collection<Integer> intersection = CollectionUtils.intersection(permissionTemplateIdList, permissionIds);
        Assert.isTrue(Objects.nonNull(intersection) && intersection.size() == permissionIds.size(), "存在无效权限");
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
        validatePermission(roleId);
        // 根据角色所属部门所在行政区拿获取权限模板类型
        RoleDTO roleDTO = oauthService.getRoleById(roleId);
        // 平台角色
        if (RoleType.SUPER_ADMIN.getType().equals(roleDTO.getRoleType())) {
            return oauthService.getRolePermissionTree(roleId, PermissionTemplateType.ALL.getType());
        }
        // 非平台角色
        return oauthService.getRolePermissionTree(roleId, getPermissionTemplateTypeByGovDeptId(roleDTO.getOrgId()));
    }

    private void validatePermission(Integer roleId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (!currentUser.isPlatformAdminUser()) {
            RoleDTO roleDTO = oauthService.getRoleById(roleId);
            Assert.isTrue(Objects.isNull(roleDTO) || roleDTO.getOrgId().equals(currentUser.getOrgId()), "非法操作，只能修改自己部门的角色");
        }
    }

    private Integer getPermissionTemplateTypeByRoleId(Integer roleId) {
        Assert.notNull(roleId, "角色ID不能为空");
        RoleDTO role = oauthService.getRoleById(roleId);
        Assert.notNull(role, "角色不存在");
        if (RoleType.SUPER_ADMIN.getType().equals(role.getRoleType())) {
            return PermissionTemplateType.ALL.getType();
        }
        return getPermissionTemplateTypeByGovDeptId(role.getOrgId());
    }

    private Integer getPermissionTemplateTypeByGovDeptId(Integer govDeptId) {
        Assert.notNull(govDeptId, "部门ID不能为空");
        GovDept govDept = govDeptService.getById(govDeptId);
        District district = districtService.getById(govDept.getDistrictId());
        return PermissionTemplateType.getTypeByDistrictCode(district.getCode());
    }

}
