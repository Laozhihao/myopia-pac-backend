package com.wupol.myopia.business.management.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.PermissionTemplateType;
import com.wupol.myopia.base.constant.RoleType;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.domain.dto.RoleDTO;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.vo.GovDeptVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class RoleService {

    @Autowired
    private OauthService oauthService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private GovDeptService govDeptService;

    /**
     * 获取角色列表 - 分页
     *
     * @param param 查询参数
     * @param current 当前页码
     * @param size 当前页数
     * @param currentUser 当前用户
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.wupol.myopia.business.management.domain.dto.RoleDTO>
     **/
    public IPage<RoleDTO> getRoleListByPage(RoleDTO param, Integer current, Integer size, CurrentUser currentUser) {
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
        // 调oauth服务，获取角色列表
        Page<RoleDTO> rolePage = oauthService.getRoleListByPage(param.setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode()).setCurrent(current).setSize(size));
        List<RoleDTO> roleList = JSONObject.parseArray(JSONObject.toJSONString(rolePage.getRecords()), RoleDTO.class);
        if (CollectionUtils.isEmpty(roleList)) {
            return rolePage;
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
        return rolePage.setRecords(roleList);
    }

    /**
     * 获取指定部门角色
     *
     * @param govDeptId 部门ID
     * @param currentUser 当前登录用户
     * @return java.util.List<com.wupol.myopia.business.management.domain.dto.RoleDTO>
     **/
    public List<RoleDTO> getGovDeptRole(Integer govDeptId, CurrentUser currentUser) {
        Assert.isTrue(currentUser.isPlatformAdminUser() || govDeptId.equals(currentUser.getOrgId()), "没有权限访问该部门角色");
        return oauthService.getRoleList(new RoleDTO().setOrgId(govDeptId));
    }

    /**
     * 新增角色
     *
     * @param param 角色数据
     * @param currentUser 当前用户
     * @return com.wupol.myopia.business.management.domain.dto.RoleDTO
     **/
    public RoleDTO addRole(RoleDTO param, CurrentUser currentUser) {
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
     * 更新角色
     * 
     * @param param 角色数据
     * @param currentUser 当前用户
     * @return com.wupol.myopia.business.management.domain.dto.RoleDTO
     **/
    public RoleDTO updateRole(RoleDTO param, CurrentUser currentUser) {
        RoleDTO role = oauthService.getRoleById(param.getId());
        Assert.notNull(role, "该角色不存在");
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
     * @param status 角色状态
     * @return com.wupol.myopia.business.management.domain.dto.RoleDTO
     **/
    public RoleDTO updateRoleStatus(Integer roleId, Integer status) {
        validatePermission(roleId);
        return oauthService.updateRole(new RoleDTO().setId(roleId).setStatus(status));
    }

    /**
     * 给角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID集
     * @return java.lang.Object
     **/
    public Object assignRolePermission(Integer roleId, List<Integer> permissionIds) {
        validatePermission(roleId);
        // 判断权限全来自模板
        List<Integer> permissionTemplateIdList = oauthService.getPermissionTemplateIdList(getPermissionTemplateTypeByRoleId(roleId));
        Collection<Integer> intersection = CollectionUtils.intersection(permissionTemplateIdList, permissionIds);
        Assert.isTrue(Objects.nonNull(intersection) && intersection.size() == permissionIds.size(), "存在无效权限");
        return oauthService.assignRolePermission(roleId, permissionIds);
    }

    /**
     * 获取角色权限 - 树结构
     *
     * @param roleId 角色ID
     * @return java.lang.Object
     **/
    public Object getRolePermissionTree(Integer roleId) {
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

    /**
     * 校验权限
     *
     * @param roleId 角色ID
     * @return void
     **/
    private void validatePermission(Integer roleId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (!currentUser.isPlatformAdminUser()) {
            RoleDTO roleDTO = oauthService.getRoleById(roleId);
            Assert.isTrue(Objects.isNull(roleDTO) || roleDTO.getOrgId().equals(currentUser.getOrgId()), "非法操作，只能修改自己部门的角色");
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
        RoleDTO role = oauthService.getRoleById(roleId);
        Assert.notNull(role, "角色不存在");
        if (RoleType.SUPER_ADMIN.getType().equals(role.getRoleType())) {
            return PermissionTemplateType.ALL.getType();
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
}
