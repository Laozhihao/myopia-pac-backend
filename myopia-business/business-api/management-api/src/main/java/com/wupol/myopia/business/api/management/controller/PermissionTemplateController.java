package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.constant.PermissionTemplateType;
import com.wupol.myopia.base.constant.RoleType;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.service.RoleService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.RoleDTO;
import com.wupol.myopia.oauth.sdk.domain.response.Permission;
import com.wupol.myopia.oauth.sdk.domain.response.Role;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 权限模板接口
 *
 * @Author HaoHao
 * @Date 2021/1/25
 **/
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/permission/template")
public class PermissionTemplateController {

    @Resource
    private OauthServiceClient oauthServiceClient;

    @Resource
    private RoleService roleService;

    /**
     * 根据模板类型获取模板权限-树结构
     *
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    @GetMapping("/{templateType}")
    public List<Permission> getPermissionTemplate(@PathVariable Integer templateType) {
        Assert.notNull(templateType, "模板类型不能为空");
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        return oauthServiceClient.getPermissionTemplate(templateType);
    }

    /**
     * 更新模板权限
     *
     * @param templateType 模板类型
     * @param permissionIds 权限集
     * @return boolean
     **/
    @PutMapping("/{templateType}")
    public boolean updatePermissionTemplate(@PathVariable Integer templateType, @RequestBody List<Integer> permissionIds) {
        Assert.notNull(templateType, "模板类型不能为空");
        Assert.notNull(permissionIds, "模板权限不能为空");
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        roleService.updateRolePermission(templateType, permissionIds);
        return oauthServiceClient.updatePermissionTemplate(templateType, permissionIds);
    }
}
