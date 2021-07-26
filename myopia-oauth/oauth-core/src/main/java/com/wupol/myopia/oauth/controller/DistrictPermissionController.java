package com.wupol.myopia.oauth.controller;

import com.wupol.myopia.base.constant.PermissionTemplateType;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.dto.RolePermissionDTO;
import com.wupol.myopia.oauth.domain.model.DistrictPermission;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.service.DistrictPermissionService;
import com.wupol.myopia.oauth.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/oauth/districtPermission")
public class DistrictPermissionController {

    @Autowired
    private DistrictPermissionService districtPermissionService;
    @Autowired
    private PermissionService permissionService;

    private static final String TEMPLATE_TYPE_NOT_EMPTY = "模板类型不能为空";

    /**
     * 根据模板类型获取模板权限-树结构
     *
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    @GetMapping("/{templateType}")
    public List<Permission> getPermissionTemplate(@PathVariable Integer templateType) {
        Assert.notNull(templateType, TEMPLATE_TYPE_NOT_EMPTY);
        return districtPermissionService.selectTemplatePermissionTree(templateType);
    }

    /**
     * 根据模板类型获取模板权限的ID集
     *
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.DistrictPermission>
     **/
    @GetMapping("/list/{templateType}")
    public List<Integer> getPermissionTemplateList(@PathVariable Integer templateType) {
        Assert.notNull(templateType, TEMPLATE_TYPE_NOT_EMPTY);
        if (PermissionTemplateType.ALL.getType().equals(templateType)) {
            return permissionService.findByList(new Permission()).stream().map(Permission::getId).collect(Collectors.toList());
        }
        return districtPermissionService.findByList(new DistrictPermission().setDistrictLevel(templateType)).stream().map(DistrictPermission::getPermissionId).collect(Collectors.toList());
    }

    /**
     * 更新模板权限
     *
     * @param templateType 模板类型
     * @param rolePermissionDTO 角色权限
     * @return boolean
     **/
    @PutMapping("/{templateType}")
    public boolean updatePermissionTemplate(@PathVariable Integer templateType, @RequestBody RolePermissionDTO rolePermissionDTO) {
        Assert.notNull(rolePermissionDTO.getPermissionIds(), "模板的权限不能为null");
        Assert.notNull(templateType, "模板类型不能为空");
        return districtPermissionService.updatePermissionTemplate(templateType, rolePermissionDTO);
    }

    /**
     * 通过templateType获取权限集合
     *
     * @param templateType 模板类型
     * @return 权限集合
     */
    @GetMapping("/permissionIds/{templateType}")
    public List<Integer> getListByTemplateType(@PathVariable Integer templateType) {
        return districtPermissionService.getByTemplateType(templateType).stream().map(DistrictPermission::getPermissionId).collect(Collectors.toList());
    }

}
