package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.PermissionTemplateType;
import com.wupol.myopia.base.constant.RoleType;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.constant.TemplateConfigType;
import com.wupol.myopia.business.api.management.domain.dto.RoleQueryDTO;
import com.wupol.myopia.business.api.management.domain.vo.RoleVO;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.dto.GovDeptDTO;
import com.wupol.myopia.business.core.government.domain.dto.GovDistrictDTO;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.RoleDTO;
import com.wupol.myopia.oauth.sdk.domain.response.Permission;
import com.wupol.myopia.oauth.sdk.domain.response.Role;
import com.wupol.myopia.oauth.sdk.domain.response.RolePermission;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
@Slf4j
public class RoleService {

    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    /**
     * 获取角色列表 - 分页
     *
     * @param param       查询参数
     * @param current     当前页码
     * @param size        当前页数
     * @param currentUser 当前用户
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.wupol.myopia.business.management.domain.dto.RoleDTO>
     **/
    public IPage<RoleVO> getRoleListByPage(RoleQueryDTO param, Integer current, Integer size, CurrentUser currentUser) {
        if (!currentUser.isPlatformAdminUser()) {
            // 非平台管理员只能查看自己部门下的角色
            param.setOrgId(CurrentUserUtil.getCurrentUser().getOrgId());
        } else if (Objects.nonNull(param.getDistrictId()) || !StringUtils.isEmpty(param.getOrgName())) {
            // 平台管理员才支持根据行政区域、部门名称、角色类型搜索
            List<GovDept> govDeptList = govDeptService.getGovDeptList(new GovDept().setDistrictId(param.getDistrictId()).setName(param.getOrgName()));
            if (CollectionUtils.isEmpty(govDeptList)) {
                return new Page<>(current, size);
            }
            param.setOrgIds(govDeptList.stream().map(GovDept::getId).collect(Collectors.toList()));
        }
        param.setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode()).setCurrent(current).setSize(size);
        // 调oauth服务，获取角色列表
        Page<Role> rolePage = oauthServiceClient.getRoleListByPage(param.convertToOauthRoleDTO());
        List<Role> roleList = JSON.parseArray(JSON.toJSONString(rolePage.getRecords()), Role.class);
        if (CollectionUtils.isEmpty(roleList)) {
            return new Page<>(current, size);
        }
        // 获取部门信息和行政区信息
        List<Integer> govDeptIds = roleList.stream().map(Role::getOrgId).distinct().collect(Collectors.toList());
        Map<Integer, GovDeptDTO> govDeptMap = govDeptService.getGovDeptMapByIds(govDeptIds);
        List<RoleVO> roleVOList = roleList.stream().map(role -> {
            RoleVO roleVO = new RoleVO(role);
            GovDeptDTO govDept = govDeptMap.get(role.getOrgId());
            if (Objects.isNull(govDept)) {
                return roleVO;
            }
            roleVO.setOrgName(govDept.getName());
            roleVO.setDistrictDetail(districtService.getDistrictPositionDetail(govDept.getDistrict()));
            roleVO.setDistrictId(govDept.getDistrictId());
            return roleVO;
        }).collect(Collectors.toList());
        return new Page<RoleVO>(rolePage.getCurrent(), rolePage.getSize(), rolePage.getTotal()).setRecords(roleVOList);

    }

    /**
     * 获取指定部门角色
     *
     * @param govDeptId 部门ID
     * @param currentUser 当前登录用户
     * @return java.util.List<com.wupol.myopia.business.management.domain.dto.RoleDTO>
     **/
    public List<Role> getGovDeptRole(Integer govDeptId, CurrentUser currentUser) {
        Assert.isTrue(currentUser.isPlatformAdminUser() || govDeptId.equals(currentUser.getOrgId()), "没有权限访问该部门角色");
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setOrgId(govDeptId);
        return oauthServiceClient.getRoleList(roleDTO);
    }

    /**
     * 新增角色
     *
     * @param param 角色数据
     * @param currentUser 当前用户
     * @return com.wupol.myopia.business.management.domain.dto.RoleDTO
     **/
    public Role addRole(RoleQueryDTO param, CurrentUser currentUser) {
        if (currentUser.isPlatformAdminUser()) {
            Assert.notNull(param.getRoleType(), "角色类型为空");
            if (RoleType.GOVERNMENT_DEPARTMENT.getType().equals(param.getRoleType()) || RoleType.SCREENING_ORGANIZATION.getType().equals(param.getRoleType())) {
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
        Role role = oauthServiceClient.addRole(param.convertToOauthRoleDTO());
        initRolePermission(role);
        return role;
    }

    /**
     * 初始化角色权限
     *
     * @param role 角色
     */
    private void initRolePermission(Role role) {
        Integer template = govDeptService.getTemplateTypeByOrgId(role.getOrgId());
        oauthServiceClient.assignRolePermission(role.getId(), oauthServiceClient.getListByTemplateType(template));
    }
    
    /**
     * 更新角色
     * 
     * @param param 角色数据
     * @param currentUser 当前用户
     * @return com.wupol.myopia.business.management.domain.dto.RoleDTO
     **/
    public RoleVO updateRole(RoleQueryDTO param, CurrentUser currentUser) {
        Role role = oauthServiceClient.getRoleById(param.getId());
        Assert.notNull(role, "该角色不存在");
        // 平台管理员用户，创建非平台角色
        if (currentUser.isPlatformAdminUser()
                && (RoleType.GOVERNMENT_DEPARTMENT.getType().equals(role.getRoleType()) || RoleType.SCREENING_ORGANIZATION.getType().equals(role.getRoleType()))) {
            Assert.notNull(param.getOrgId(), "所属部门ID不能为空");
            role.setOrgId(param.getOrgId());
            // 非平台角色的部门不能为运营中心部门
            Assert.isTrue(!param.getOrgId().equals(currentUser.getOrgId()), "无效部门ID");
        }
        // 非平台管理员用户，只能修改自己部门的角色
        Assert.isTrue(currentUser.isPlatformAdminUser() || role.getOrgId().equals(currentUser.getOrgId()), "非法操作，只能修改自己部门的角色");
        role.setStatus(param.getStatus()).setRemark(param.getRemark()).setChName(param.getChName());
        Role newRole = oauthServiceClient.updateRole(role.convertToRoleDTO());
        GovDept govDept = govDeptService.getById(newRole.getOrgId());
        District district = districtService.getById(govDept.getDistrictId());
        RoleVO roleVO = new RoleVO(newRole);
        return roleVO.setOrgName(govDept.getName()).setDistrictDetail(districtService.getDistrictPositionDetail(district));
    }

    /**
     * 更新角色状态
     *
     * @param roleId 角色ID
     * @param status 角色状态
     * @return com.wupol.myopia.business.management.domain.dto.RoleDTO
     **/
    public Role updateRoleStatus(Integer roleId, Integer status) {
        validatePermission(roleId);
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(roleId).setStatus(status);
        return oauthServiceClient.updateRole(roleDTO);
    }

    /**
     * 给角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID集
     * @return java.lang.Object
     **/
    public List<RolePermission> assignRolePermission(Integer roleId, List<Integer> permissionIds) {
        validatePermission(roleId);
        // 判断权限全来自模板
        List<Integer> permissionTemplateIdList = oauthServiceClient.getPermissionTemplateIdList(getPermissionTemplateTypeByRoleId(roleId));
        Collection<Integer> intersection = CollectionUtils.intersection(permissionTemplateIdList, permissionIds);
        Assert.isTrue(Objects.nonNull(intersection) && intersection.size() == permissionIds.size(), "存在无效权限");
        return oauthServiceClient.assignRolePermission(roleId, permissionIds);
    }

    /**
     * 获取角色权限 - 树结构
     *
     * @param roleId 角色ID
     * @return java.lang.Object
     **/
    public List<Permission> getRolePermissionTree(Integer roleId) {
        validatePermission(roleId);
        // 根据角色所属部门所在行政区拿获取权限模板类型
        Role role = oauthServiceClient.getRoleById(roleId);
        // 超级管理员角色
        if (RoleType.SUPER_ADMIN.getType().equals(role.getRoleType())) {
            return oauthServiceClient.getRolePermissionTree(roleId, PermissionTemplateType.ALL.getType());
        }
        // 平台角色
        if (RoleType.PLATFORM_ADMIN.getType().equals(role.getRoleType())) {
            return oauthServiceClient.getRolePermissionTree(roleId, PermissionTemplateType.PLATFORM_ADMIN.getType());
        }
        // 非平台角色
        return oauthServiceClient.getRolePermissionTree(roleId, getPermissionTemplateTypeByGovDeptId(role.getOrgId()));
    }

    /**
     * 校验权限
     *
     * @param roleId 角色ID
     * @return void
     **/
    private void validatePermission(Integer roleId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (!currentUser.isPlatformAdminUser()) {
            Role role = oauthServiceClient.getRoleById(roleId);
            Assert.isTrue(Objects.isNull(role) || role.getOrgId().equals(currentUser.getOrgId()), "非法操作，只能修改自己部门的角色");
        }
    }

    /**
     * 根据角色ID获取权限模板
     *
     * @param roleId 角色ID
     * @return java.lang.Integer
     **/
    private Integer getPermissionTemplateTypeByRoleId(Integer roleId) {
        Assert.notNull(roleId, "角色ID不能为空");
        Role role = oauthServiceClient.getRoleById(roleId);
        Assert.notNull(role, "角色不存在");
        if (RoleType.SUPER_ADMIN.getType().equals(role.getRoleType())) {
            return PermissionTemplateType.ALL.getType();
        }
        if (RoleType.PLATFORM_ADMIN.getType().equals(role.getRoleType())) {
            return PermissionTemplateType.PLATFORM_ADMIN.getType();
        }
        return getPermissionTemplateTypeByGovDeptId(role.getOrgId());
    }

    /**
     * 根据部门ID获取权限模板类型
     *
     * @param govDeptId 部门ID
     * @return java.lang.Integer
     **/
    private Integer getPermissionTemplateTypeByGovDeptId(Integer govDeptId) {
        Assert.notNull(govDeptId, "部门ID不能为空");
        GovDept govDept = govDeptService.getById(govDeptId);
        District district = districtService.getById(govDept.getDistrictId());
        return PermissionTemplateType.getTypeByDistrictCode(district.getCode());
    }

    /**
     * 获取指定templateType的政府人员部门Id
     *
     * @param templateType  类型
     */
    public List<Integer> getGovIds(Integer templateType) {

        if (!PermissionTemplateType.isGovUser(templateType)) {
            return new ArrayList<>();
        }
        // 只有角色为政府人员需要处理
        List<GovDistrictDTO> govList = govDeptService.getAllGovDept();
        // 获取属于当前templateType的行政部门（templateType=1，取全省的行政部门）。只需要部门Id
        return govList.stream()
                .filter(s -> PermissionTemplateType.getTypeByDistrictCode(s.getCode()).equals(templateType))
                .map(GovDept::getId).collect(Collectors.toList());

    }

    /**
     * 通过模板Id获取机构Ids
     *
     * @param templateType 模板
     * @return 机构Ids
     */
    public List<Integer> getOrgIds(Integer templateType) {
        Integer configType = TemplateConfigType.TEMPLATE_TO_ORG_CONFIG_TYPE.get(templateType);
        if (Objects.isNull(templateType)) {
            return new ArrayList<>();
        }
        List<ScreeningOrganization> orgList = screeningOrganizationService.getByConfigType(configType);
        if (CollectionUtils.isEmpty(orgList)) {
            return new ArrayList<>();
        }
        return orgList.stream().map(ScreeningOrganization::getId).collect(Collectors.toList());
    }
}
